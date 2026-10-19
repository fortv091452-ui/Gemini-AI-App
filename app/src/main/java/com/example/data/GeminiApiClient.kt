package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString() + "_" + (0..999).random(),
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    // Configured with 60-second timeouts per Gemini API guidelines
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun resolveApiKey(userKey: String): String {
        return when {
            userKey.isNotBlank() -> userKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }
    }

    suspend fun generateResponse(
        prompt: String,
        history: List<ChatMessage>,
        userApiKey: String,
        modelName: String = "gemini-3.5-flash",
        maxTokens: Int = 1024,
        isLowRamMode: Boolean = true,
        isPrestigioTuning: Boolean = true
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(userApiKey)
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("API ключ Gemini не задан. Укажите ключ в настройках приложения или в Secrets панели AI Studio.")
            )
        }

        val effectiveModel = if (modelName.isBlank()) "gemini-3.5-flash" else modelName
        val url = "$BASE_URL$effectiveModel:generateContent?key=$apiKey"

        try {
            val rootJson = JSONObject()

            // System Instruction optimized for low-RAM devices & Prestigio Multipad 2 / Wolfius
            val systemInstructionJson = JSONObject()
            val sysParts = JSONArray()
            val sysText = StringBuilder("Ты — ультра-быстрый AI-клиент Wolfius Gemini для Android. ")
            if (isPrestigioTuning) {
                sysText.append("Клиент оптимизирован для слабого планшета Prestigio MultiPad 2 Pro Duo 7.0 (Dual-Core Rockchip RK3066, 1GB RAM, Android 4.1). ")
            }
            if (isLowRamMode) {
                sysText.append("Форматируй ответы предельно емко, структурировано и без лишней воды для экономии памяти и быстрого чтения. ")
            }
            sysText.append("Помогай пользователю с вопросами по Android, консольным командам Wolfius без root, опенсорс браузерам и оптимизации системы.")
            sysParts.put(JSONObject().put("text", sysText.toString()))
            systemInstructionJson.put("parts", sysParts)
            rootJson.put("systemInstruction", systemInstructionJson)

            // Contents array
            val contentsArray = JSONArray()

            // Include limited recent history to prevent memory explosion on 1GB RAM
            val historySlice = if (history.size > 4) history.takeLast(4) else history
            for (msg in historySlice) {
                if (msg.text.isNotBlank() && !msg.isError) {
                    val contentObj = JSONObject()
                    contentObj.put("role", if (msg.role == "user") "user" else "model")
                    val parts = JSONArray()
                    parts.put(JSONObject().put("text", msg.text))
                    contentObj.put("parts", parts)
                    contentsArray.put(contentObj)
                }
            }

            // Current prompt
            val currentContent = JSONObject()
            currentContent.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", prompt))
            currentContent.put("parts", currentParts)
            contentsArray.put(currentContent)

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("maxOutputTokens", maxTokens)
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "Код ошибки: ${response.code}"
                } catch (_: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            // Parse response candidates
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val textBuilder = StringBuilder()
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val text = part.optString("text", "")
                        textBuilder.append(text)
                    }
                    val reply = textBuilder.toString().trim()
                    if (reply.isNotEmpty()) {
                        return@withContext Result.success(reply)
                    }
                }
            }

            Result.failure(Exception("Пустой ответ от модели Gemini"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
