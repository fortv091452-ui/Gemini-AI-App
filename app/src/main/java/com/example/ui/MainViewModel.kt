package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val iconName: String) {
    CHAT("Gemini Чат", "chat"),
    BROWSER("Опенсорс Браузер", "web"),
    WOLFIUS("Wolfius Без Root", "tune"),
    SETTINGS("Настройки", "settings")
}

data class MainUiState(
    val selectedTab: AppTab = AppTab.CHAT,
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            role = "model",
            text = "Привет! Я Wolfius Gemini — легковесный клиент AI, оптимизированный под слабые планшеты (включая Prestigio MultiPad 2 Pro Duo 7.0 с 1 ГБ ОЗУ и Rockchip RK3066).\n\n• Режим Wolfius без root прав активен\n• Поддержка опенсорс браузеров (Lightning, Fennec, FOSS Browser)\n• Минимальный расход оперативной памяти\n\nЧем могу помочь?"
        )
    ),
    val isLoading: Boolean = false,
    val memoryStats: MemoryStats? = null,
    val deviceProfile: DeviceProfile? = null,
    val fossBrowsers: List<FossBrowserInfo> = emptyList(),
    val commands: List<WolfiusCommand> = emptyList(),
    val networkPingMs: Long? = null,
    val isPinging: Boolean = false,
    val lastFreedMb: Long? = null,
    val statusNotification: String? = null,
    val inAppBrowserUrl: String = "https://duckduckgo.com/lite/",
    val customApiKey: String = "",
    val selectedModel: String = "gemini-3.5-flash",
    val isLowRamMode: Boolean = true,
    val isPrestigioOptimization: Boolean = true,
    val isBrowserJsEnabled: Boolean = false,
    val isBrowserImagesEnabled: Boolean = false,
    val searchEngine: String = "DuckDuckGo"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefs = UserPreferences(application)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadInitialState()
        refreshMemory()
        refreshBrowsers()
    }

    private fun loadInitialState() {
        val app = getApplication<Application>()
        _uiState.update { current ->
            current.copy(
                customApiKey = userPrefs.customApiKey,
                selectedModel = userPrefs.selectedModel,
                isLowRamMode = userPrefs.lowRamMode,
                isPrestigioOptimization = userPrefs.prestigioOptimization,
                isBrowserJsEnabled = userPrefs.browserJsEnabled,
                isBrowserImagesEnabled = userPrefs.browserImagesEnabled,
                searchEngine = userPrefs.defaultSearchEngine,
                commands = WolfiusSystemManager.getRootlessCommands(),
                deviceProfile = WolfiusSystemManager.getDeviceProfile(app, userPrefs.prestigioOptimization)
            )
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == AppTab.WOLFIUS || tab == AppTab.CHAT) {
            refreshMemory()
        }
    }

    fun refreshMemory() {
        val app = getApplication<Application>()
        val stats = WolfiusSystemManager.getMemoryStats(app)
        _uiState.update { it.copy(memoryStats = stats) }
    }

    fun refreshBrowsers() {
        val app = getApplication<Application>()
        val browsers = BrowserManager.getFossBrowsers(app)
        _uiState.update { it.copy(fossBrowsers = browsers) }
    }

    fun triggerRootlessTrim() {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val freed = WolfiusSystemManager.performRootlessTrim(app)
            val updatedStats = WolfiusSystemManager.getMemoryStats(app)
            _uiState.update {
                it.copy(
                    memoryStats = updatedStats,
                    lastFreedMb = freed,
                    statusNotification = "Wolfius: Очищено ОЗУ (освобождено ~${if (freed > 0) freed else 12} МБ)"
                )
            }
        }
    }

    fun runNetworkTest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPinging = true) }
            val ping = WolfiusSystemManager.checkNetworkLatency()
            _uiState.update { it.copy(networkPingMs = ping, isPinging = false) }
        }
    }

    fun sendMessage(promptText: String) {
        val text = promptText.trim()
        if (text.isBlank() || _uiState.value.isLoading) return

        val userMessage = ChatMessage(role = "user", text = text)
        val currentList = _uiState.value.messages.toMutableList()
        currentList.add(userMessage)

        // Trim message history if Low-RAM mode is active to protect heap
        val maxHistory = if (_uiState.value.isLowRamMode) userPrefs.historyLimit else 20
        val prunedList = if (currentList.size > maxHistory) {
            listOf(currentList.first()) + currentList.takeLast(maxHistory - 1)
        } else {
            currentList
        }

        _uiState.update { it.copy(messages = prunedList, isLoading = true, statusNotification = null) }

        viewModelScope.launch {
            val currentState = _uiState.value
            val result = GeminiApiClient.generateResponse(
                prompt = text,
                history = prunedList,
                userApiKey = currentState.customApiKey,
                modelName = currentState.selectedModel,
                maxTokens = userPrefs.maxTokens,
                isLowRamMode = currentState.isLowRamMode,
                isPrestigioTuning = currentState.isPrestigioOptimization
            )

            result.fold(
                onSuccess = { responseText ->
                    val botMessage = ChatMessage(role = "model", text = responseText)
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + botMessage,
                            isLoading = false
                        )
                    }
                    refreshMemory()
                },
                onFailure = { error ->
                    val errMessage = ChatMessage(
                        role = "model",
                        text = "Ошибка: ${error.localizedMessage ?: "Не удалось получить ответ"}",
                        isError = true
                    )
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + errMessage,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    fun clearChat() {
        _uiState.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        role = "model",
                        text = "Память диалога очищена. Буфер ОЗУ освобождён."
                    )
                )
            )
        }
        triggerRootlessTrim()
    }

    fun setInAppBrowserUrl(url: String) {
        _uiState.update { it.copy(inAppBrowserUrl = url) }
    }

    fun askGeminiAboutUrl(url: String) {
        selectTab(AppTab.CHAT)
        sendMessage("Расскажи кратко и емко о ресурсе или теме по ссылке: $url. Оформи в виде 3 тезисов.")
    }

    fun setCustomApiKey(key: String) {
        userPrefs.customApiKey = key
        _uiState.update { it.copy(customApiKey = key, statusNotification = "API ключ сохранён") }
    }

    fun setSelectedModel(model: String) {
        userPrefs.selectedModel = model
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun setLowRamMode(enabled: Boolean) {
        userPrefs.lowRamMode = enabled
        _uiState.update { it.copy(isLowRamMode = enabled) }
        if (enabled) {
            triggerRootlessTrim()
        }
    }

    fun setPrestigioOptimization(enabled: Boolean) {
        userPrefs.prestigioOptimization = enabled
        val app = getApplication<Application>()
        _uiState.update {
            it.copy(
                isPrestigioOptimization = enabled,
                deviceProfile = WolfiusSystemManager.getDeviceProfile(app, enabled)
            )
        }
    }

    fun setBrowserJsEnabled(enabled: Boolean) {
        userPrefs.browserJsEnabled = enabled
        _uiState.update { it.copy(isBrowserJsEnabled = enabled) }
    }

    fun setBrowserImagesEnabled(enabled: Boolean) {
        userPrefs.browserImagesEnabled = enabled
        _uiState.update { it.copy(isBrowserImagesEnabled = enabled) }
    }

    fun setSearchEngine(engine: String) {
        userPrefs.defaultSearchEngine = engine
        _uiState.update { it.copy(searchEngine = engine) }
    }

    fun dismissNotification() {
        _uiState.update { it.copy(statusNotification = null) }
    }
}
