package com.example.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("wolfius_prefs", Context.MODE_PRIVATE)

    var customApiKey: String
        get() = prefs.getString("custom_api_key", "") ?: ""
        set(value) = prefs.edit().putString("custom_api_key", value.trim()).apply()

    var selectedModel: String
        get() = prefs.getString("selected_model", "gemini-3.5-flash") ?: "gemini-3.5-flash"
        set(value) = prefs.edit().putString("selected_model", value).apply()

    var lowRamMode: Boolean
        get() = prefs.getBoolean("low_ram_mode", true)
        set(value) = prefs.edit().putBoolean("low_ram_mode", value).apply()

    var maxTokens: Int
        get() = prefs.getInt("max_tokens", 1024)
        set(value) = prefs.edit().putInt("max_tokens", value).apply()

    var historyLimit: Int
        get() = prefs.getInt("history_limit", 6)
        set(value) = prefs.edit().putInt("history_limit", value).apply()

    var browserJsEnabled: Boolean
        get() = prefs.getBoolean("browser_js_enabled", false)
        set(value) = prefs.edit().putBoolean("browser_js_enabled", value).apply()

    var browserImagesEnabled: Boolean
        get() = prefs.getBoolean("browser_images_enabled", false)
        set(value) = prefs.edit().putBoolean("browser_images_enabled", value).apply()

    var prestigioOptimization: Boolean
        get() = prefs.getBoolean("prestigio_optimization", true)
        set(value) = prefs.edit().putBoolean("prestigio_optimization", value).apply()

    var defaultSearchEngine: String
        get() = prefs.getString("default_search_engine", "DuckDuckGo") ?: "DuckDuckGo"
        set(value) = prefs.edit().putString("default_search_engine", value).apply()
}
