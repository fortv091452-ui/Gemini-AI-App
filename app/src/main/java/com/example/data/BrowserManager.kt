package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

data class FossBrowserInfo(
    val name: String,
    val packageName: String,
    val description: String,
    val ramProfile: String,
    val downloadUrl: String,
    val isInstalled: Boolean = false
)

object BrowserManager {

    private val KNOWN_FOSS_BROWSERS = listOf(
        FossBrowserInfo(
            name = "Lightning Browser",
            packageName = "acr.browser.lightning",
            description = "Легендарный легковесный open-source браузер (потребляет ~25 МБ ОЗУ). Идеален для Android 4.1.",
            ramProfile = "25 МБ ОЗУ (Экстремально лёгкий)",
            downloadUrl = "https://f-droid.org/packages/acr.browser.lightning/"
        ),
        FossBrowserInfo(
            name = "FOSS Browser",
            packageName = "de.baumann.browser",
            description = "Минималистичный браузер без трекеров, разработанный специально для старых и слабых устройств.",
            ramProfile = "20 МБ ОЗУ (Минималистичный)",
            downloadUrl = "https://f-droid.org/packages/de.baumann.browser/"
        ),
        FossBrowserInfo(
            name = "DuckDuckGo Privacy",
            packageName = "com.duckduckgo.mobile.android",
            description = "Браузер с защитой приватности, быстрым сбросом вкладок и низким расходом трафика.",
            ramProfile = "45 МБ ОЗУ (Средний)",
            downloadUrl = "https://duckduckgo.com/app"
        ),
        FossBrowserInfo(
            name = "Fennec F-Droid",
            packageName = "org.mozilla.fennec_fdroid",
            description = "Полностью открытая сборка Firefox от F-Droid без проприетарных сервисов Google.",
            ramProfile = "70 МБ ОЗУ (Функциональный)",
            downloadUrl = "https://f-droid.org/packages/org.mozilla.fennec_fdroid/"
        ),
        FossBrowserInfo(
            name = "Firefox Focus",
            packageName = "org.mozilla.focus",
            description = "Браузер для одной задачи с мгновенной очисткой ОЗУ по кнопке мусорной корзины.",
            ramProfile = "35 МБ ОЗУ (Быстрый сброс)",
            downloadUrl = "https://f-droid.org/packages/org.mozilla.focus/"
        ),
        FossBrowserInfo(
            name = "Bromite / Chromium FOSS",
            packageName = "org.bromite.bromite",
            description = "Форк Chromium со встроенным блокировщиком рекламы и патчами производительности.",
            ramProfile = "80 МБ ОЗУ (Полноценный web)",
            downloadUrl = "https://www.bromite.org/"
        )
    )

    fun getFossBrowsers(context: Context): List<FossBrowserInfo> {
        val pm = context.packageManager
        return KNOWN_FOSS_BROWSERS.map { browser ->
            val installed = try {
                pm.getPackageInfo(browser.packageName, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
            browser.copy(isInstalled = installed)
        }
    }

    fun openUrlInBrowser(context: Context, url: String, preferredPackage: String? = null): Boolean {
        return try {
            val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (!preferredPackage.isNullOrBlank()) {
                    setPackage(preferredPackage)
                }
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            // Fallback to chooser without explicit package
            try {
                val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(genericIntent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun getSearchUrl(query: String, engine: String): String {
        val encoded = Uri.encode(query)
        return when (engine.lowercase()) {
            "duckduckgo", "ddg lite" -> "https://duckduckgo.com/lite/?q=$encoded"
            "searxng" -> "https://searx.be/search?q=$encoded"
            "startpage" -> "https://www.startpage.com/sp/search?query=$encoded"
            else -> "https://duckduckgo.com/lite/?q=$encoded"
        }
    }
}
