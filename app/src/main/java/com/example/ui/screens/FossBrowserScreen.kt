package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.BrowserManager
import com.example.data.FossBrowserInfo
import com.example.ui.MainUiState
import com.example.ui.theme.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FossBrowserScreen(
    uiState: MainUiState,
    onUrlChange: (String) -> Unit,
    onAskGemini: (String) -> Unit,
    onToggleJs: (Boolean) -> Unit,
    onToggleImages: (Boolean) -> Unit,
    onSearchEngineChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf(uiState.inAppBrowserUrl) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var webTitle by remember { mutableStateOf("Wolfius Lite Reader") }
    var isPageLoading by remember { mutableStateOf(false) }
    var selectedBrowserSection by remember { mutableIntStateOf(0) } // 0 = Lite Web Reader, 1 = FOSS Browsers List

    val quickBookmarks = listOf(
        "DuckDuckGo Lite" to "https://duckduckgo.com/lite/",
        "Wikipedia Mobile" to "https://ru.m.wikipedia.org",
        "F-Droid" to "https://f-droid.org",
        "Hacker News" to "https://news.ycombinator.com"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(8.dp)
    ) {
        // Mode Selector: Web Reader vs Open Source Browsers Catalog
        TabRow(
            selectedTabIndex = selectedBrowserSection,
            containerColor = SurfaceCardDark,
            contentColor = CyanPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = selectedBrowserSection == 0,
                onClick = { selectedBrowserSection = 0 },
                text = { Text("Lite Веб-Ридер", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_web_reader")
            )
            Tab(
                selected = selectedBrowserSection == 1,
                onClick = { selectedBrowserSection = 1 },
                text = { Text("Каталог FOSS Браузеров", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_foss_catalog")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedBrowserSection == 0) {
            // LITE WEB READER SECTION
            // URL / Search Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("browser_url_input"),
                    placeholder = { Text("URL или поиск (DuckDuckGo Lite)...", color = TextMuted, fontSize = 12.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = SurfaceCardElevated,
                        focusedContainerColor = SurfaceCardDark,
                        unfocusedContainerColor = SurfaceCardDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = {
                        val target = if (urlInput.contains(".") && !urlInput.contains(" ")) {
                            if (!urlInput.startsWith("http://") && !urlInput.startsWith("https://")) "https://$urlInput" else urlInput
                        } else {
                            BrowserManager.getSearchUrl(urlInput, uiState.searchEngine)
                        }
                        onUrlChange(target)
                        webViewRef?.loadUrl(target)
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("browser_go_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF031A24)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Перейти", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Toggles Row: JavaScript & Images (crucial for 1GB RAM)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCardDark, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = uiState.isBrowserJsEnabled,
                        onCheckedChange = {
                            onToggleJs(it)
                            webViewRef?.settings?.javaScriptEnabled = it
                            webViewRef?.reload()
                        },
                        modifier = Modifier
                            .scale(0.8f)
                            .testTag("browser_js_switch")
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (uiState.isBrowserJsEnabled) "JS: Вкл" else "JS: Выкл (ОЗУ -85%)",
                        fontSize = 11.sp,
                        color = if (uiState.isBrowserJsEnabled) WarningAmber else SuccessGreen
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = uiState.isBrowserImagesEnabled,
                        onCheckedChange = {
                            onToggleImages(it)
                            webViewRef?.settings?.loadsImagesAutomatically = it
                            webViewRef?.reload()
                        },
                        modifier = Modifier
                            .scale(0.8f)
                            .testTag("browser_images_switch")
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (uiState.isBrowserImagesEnabled) "Картинки: Вкл" else "Текст (Low RAM)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = {
                        val currentUrl = webViewRef?.url ?: urlInput
                        onAskGemini(currentUrl)
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("ask_gemini_url_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Анализ Gemini",
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val currentUrl = webViewRef?.url ?: urlInput
                        BrowserManager.openUrlInBrowser(context, currentUrl)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Открыть во внешнем браузере",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quick Bookmarks Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickBookmarks.forEach { (name, url) ->
                    AssistChip(
                        onClick = {
                            urlInput = url
                            onUrlChange(url)
                            webViewRef?.loadUrl(url)
                        },
                        label = { Text(name, fontSize = 11.sp, color = CyanSecondary) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = SurfaceCardDark),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = SurfaceCardElevated),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (isPageLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = CyanPrimary,
                    trackColor = SurfaceCardDark
                )
            }

            // Embedded Ultra-Low RAM WebView
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = uiState.isBrowserJsEnabled
                            loadsImagesAutomatically = uiState.isBrowserImagesEnabled
                            cacheMode = WebSettings.LOAD_NO_CACHE
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            domStorageEnabled = false
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isPageLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isPageLoading = false
                                if (url != null) {
                                    urlInput = url
                                    onUrlChange(url)
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                if (title != null) webTitle = title
                            }
                        }

                        loadUrl(uiState.inAppBrowserUrl)
                        webViewRef = this
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .testTag("in_app_webview")
            )

        } else {
            // FOSS BROWSERS CATALOG
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("foss_browsers_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Опенсорс Браузеры для слабых планшетов (Prestigio / Android 4.1):",
                        style = MaterialTheme.typography.titleSmall.copy(color = CyanPrimary, fontSize = 13.sp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(uiState.fossBrowsers) { browser ->
                    FossBrowserCard(
                        browser = browser,
                        onOpen = {
                            val opened = BrowserManager.openUrlInBrowser(context, "https://duckduckgo.com/lite/", browser.packageName)
                            if (!opened) {
                                Toast.makeText(context, "Браузер не найден, открываем ссылку для загрузки", Toast.LENGTH_SHORT).show()
                                BrowserManager.openUrlInBrowser(context, browser.downloadUrl)
                            }
                        },
                        onDownload = {
                            BrowserManager.openUrlInBrowser(context, browser.downloadUrl)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FossBrowserCard(
    browser: FossBrowserInfo,
    onOpen: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("browser_card_${browser.name.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = browser.name,
                        style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontSize = 15.sp)
                    )
                    Text(
                        text = browser.ramProfile,
                        style = MaterialTheme.typography.labelSmall.copy(color = CyanSecondary, fontSize = 11.sp)
                    )
                }

                if (browser.isInstalled) {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Установлен",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = browser.description,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDownload,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("F-Droid / APK", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onOpen,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF031A24)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (browser.isInstalled) "Запустить" else "Открыть", fontSize = 11.sp)
                }
            }
        }
    }
}
