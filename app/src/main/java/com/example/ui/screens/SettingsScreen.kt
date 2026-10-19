package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.MainUiState
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onSaveApiKey: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onToggleLowRam: (Boolean) -> Unit,
    onTogglePrestigio: (Boolean) -> Unit,
    onToggleBrowserJs: (Boolean) -> Unit,
    onToggleBrowserImages: (Boolean) -> Unit,
    onSelectSearchEngine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var apiKeyInput by remember { mutableStateOf(uiState.customApiKey) }
    val context = LocalContext.current

    val isBuiltInKeyAvailable = BuildConfig.GEMINI_API_KEY.isNotBlank() &&
            BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(8.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // API Key Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_key_settings_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gemini API Ключ",
                            style = MaterialTheme.typography.titleMedium.copy(color = CyanPrimary, fontSize = 15.sp)
                        )

                        Surface(
                            color = if (isBuiltInKeyAvailable || uiState.customApiKey.isNotBlank())
                                SuccessGreen.copy(alpha = 0.2f)
                            else WarningAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isBuiltInKeyAvailable || uiState.customApiKey.isNotBlank()) "Готов к работе" else "Требуется ключ",
                                color = if (isBuiltInKeyAvailable || uiState.customApiKey.isNotBlank()) SuccessGreen else WarningAmber,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isBuiltInKeyAvailable)
                            "Используется системный ключ из Secrets панели AI Studio. Вы также можете переопределить его собственным ключом ниже."
                        else
                            "Для работы клиента укажите ключ Gemini API (бесплатно в Google AI Studio) или настройте секрет GEMINI_API_KEY.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input_field"),
                        placeholder = { Text("Вставьте ключ AIzaSy...", color = TextMuted, fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SurfaceCardElevated,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (apiKeyInput.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    apiKeyInput = ""
                                    onSaveApiKey("")
                                    Toast.makeText(context, "Ключ сброшен", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.height(34.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("Очистить", fontSize = 11.sp, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Button(
                            onClick = {
                                onSaveApiKey(apiKeyInput)
                                Toast.makeText(context, "API Ключ сохранён", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("save_api_key_button"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary,
                                contentColor = Color(0xFF031A24)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text("Сохранить", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // AI Model Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Модель Gemini (Поддержка Low-RAM)",
                        style = MaterialTheme.typography.titleMedium.copy(color = CyanPrimary, fontSize = 15.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val models = listOf(
                        "gemini-3.5-flash" to "Быстрая, мультифункциональная (по умолчанию)",
                        "gemini-3.1-flash-lite-preview" to "Flash Lite: минимальная задержка и нагрузка на память"
                    )

                    models.forEach { (modelId, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.selectedModel == modelId,
                                onClick = { onSelectModel(modelId) },
                                colors = RadioButtonDefaults.colors(selectedColor = CyanPrimary),
                                modifier = Modifier.testTag("radio_model_$modelId")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(text = modelId, color = TextPrimary, fontSize = 13.sp)
                                Text(text = desc, color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Hardware & Low RAM Tuning
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Оптимизация под слабые планшеты и Prestigio",
                        style = MaterialTheme.typography.titleMedium.copy(color = CyanPrimary, fontSize = 15.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingToggleRow(
                        title = "Экстремальная экономия ОЗУ",
                        description = "Ограничивает буфер истории диалога и токенов для исключения OutOfMemory",
                        checked = uiState.isLowRamMode,
                        onCheckedChange = onToggleLowRam,
                        testTag = "toggle_low_ram"
                    )

                    HorizontalDivider(color = SurfaceCardElevated, modifier = Modifier.padding(vertical = 6.dp))

                    SettingToggleRow(
                        title = "Профиль Prestigio MultiPad 2 Pro Duo 7.0",
                        description = "Калибровка под 2-ядерный Rockchip RK3066, Mali-400 MP4 и экран 1024x600",
                        checked = uiState.isPrestigioOptimization,
                        onCheckedChange = onTogglePrestigio,
                        testTag = "toggle_prestigio"
                    )

                    HorizontalDivider(color = SurfaceCardElevated, modifier = Modifier.padding(vertical = 6.dp))

                    SettingToggleRow(
                        title = "Отключить JavaScript в веб-ридере",
                        description = "Снижает потребление оперативной памяти веб-страницами более чем на 85%",
                        checked = !uiState.isBrowserJsEnabled,
                        onCheckedChange = { onToggleBrowserJs(!it) },
                        testTag = "toggle_disable_js"
                    )

                    HorizontalDivider(color = SurfaceCardElevated, modifier = Modifier.padding(vertical = 6.dp))

                    SettingToggleRow(
                        title = "Текстовый режим без картинок",
                        description = "Мгновенная загрузка страниц и защита от расхода ОЗУ на декодирование графики",
                        checked = !uiState.isBrowserImagesEnabled,
                        onCheckedChange = { onToggleBrowserImages(!it) },
                        testTag = "toggle_disable_images"
                    )
                }
            }
        }

        // Search Engine Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Поисковая система для FOSS браузеров",
                        style = MaterialTheme.typography.titleMedium.copy(color = CyanPrimary, fontSize = 15.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val engines = listOf("DuckDuckGo", "SearXNG", "StartPage")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        engines.forEach { engine ->
                            FilterChip(
                                selected = uiState.searchEngine == engine,
                                onClick = { onSelectSearchEngine(engine) },
                                label = { Text(engine, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = CyanPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("filter_chip_$engine")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 13.sp)
            Text(text = description, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyanPrimary,
                checkedTrackColor = CyanPrimary.copy(alpha = 0.3f)
            )
        )
    }
}
