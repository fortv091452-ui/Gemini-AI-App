package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.screens.FossBrowserScreen
import com.example.ui.screens.GeminiChatScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WolfiusToolsScreen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscapeTablet = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = SurfaceCardDark,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_wolfius_gemini),
                                    contentDescription = "Wolfius Crest",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Wolfius Gemini",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = CyanPrimary,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = if (uiState.isPrestigioOptimization) "Prestigio MultiPad 2 • Без Root" else "Android Low-RAM • Без Root",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        uiState.memoryStats?.let { mem ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceCardDark,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (mem.isLowMemory) DangerRed else SuccessGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${mem.availMemMb}M",
                                        color = TextPrimary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceDark
                    ),
                    modifier = Modifier.testTag("app_top_bar")
                )
            },
            bottomBar = {
                if (!isLandscapeTablet) {
                    NavigationBar(
                        containerColor = SurfaceDark,
                        contentColor = CyanPrimary,
                        modifier = Modifier.testTag("app_bottom_nav")
                    ) {
                        AppTab.entries.forEach { tab ->
                            val icon = when (tab) {
                                AppTab.CHAT -> Icons.AutoMirrored.Filled.Chat
                                AppTab.BROWSER -> Icons.Default.Language
                                AppTab.WOLFIUS -> Icons.Default.Tune
                                AppTab.SETTINGS -> Icons.Default.Settings
                            }
                            NavigationBarItem(
                                selected = uiState.selectedTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                icon = { Icon(icon, contentDescription = tab.title, modifier = Modifier.size(20.dp)) },
                                label = { Text(tab.title, fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyanPrimary,
                                    selectedTextColor = CyanPrimary,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                    indicatorColor = SurfaceCardDark
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BackgroundDark)
            ) {
                // In Tablet Landscape Mode: Show Navigation Rail on Left
                if (isLandscapeTablet) {
                    NavigationRail(
                        containerColor = SurfaceDark,
                        contentColor = CyanPrimary,
                        modifier = Modifier.testTag("app_nav_rail")
                    ) {
                        AppTab.entries.forEach { tab ->
                            val icon = when (tab) {
                                AppTab.CHAT -> Icons.AutoMirrored.Filled.Chat
                                AppTab.BROWSER -> Icons.Default.Language
                                AppTab.WOLFIUS -> Icons.Default.Tune
                                AppTab.SETTINGS -> Icons.Default.Settings
                            }
                            NavigationRailItem(
                                selected = uiState.selectedTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                icon = { Icon(icon, contentDescription = tab.title) },
                                label = { Text(tab.title, fontSize = 11.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = CyanPrimary,
                                    selectedTextColor = CyanPrimary,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                    indicatorColor = SurfaceCardDark
                                ),
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                // Main Content View
                Box(modifier = Modifier.fillMaxSize()) {
                    when (uiState.selectedTab) {
                        AppTab.CHAT -> GeminiChatScreen(
                            uiState = uiState,
                            onSendMessage = { viewModel.sendMessage(it) },
                            onClearChat = { viewModel.clearChat() },
                            onTrimRam = { viewModel.triggerRootlessTrim() }
                        )
                        AppTab.BROWSER -> FossBrowserScreen(
                            uiState = uiState,
                            onUrlChange = { viewModel.setInAppBrowserUrl(it) },
                            onAskGemini = { viewModel.askGeminiAboutUrl(it) },
                            onToggleJs = { viewModel.setBrowserJsEnabled(it) },
                            onToggleImages = { viewModel.setBrowserImagesEnabled(it) },
                            onSearchEngineChange = { viewModel.setSearchEngine(it) }
                        )
                        AppTab.WOLFIUS -> WolfiusToolsScreen(
                            uiState = uiState,
                            onTrimRam = { viewModel.triggerRootlessTrim() },
                            onRunPingTest = { viewModel.runNetworkTest() },
                            onAskGeminiForScript = { prompt ->
                                viewModel.selectTab(AppTab.CHAT)
                                viewModel.sendMessage(prompt)
                            }
                        )
                        AppTab.SETTINGS -> SettingsScreen(
                            uiState = uiState,
                            onSaveApiKey = { viewModel.setCustomApiKey(it) },
                            onSelectModel = { viewModel.setSelectedModel(it) },
                            onToggleLowRam = { viewModel.setLowRamMode(it) },
                            onTogglePrestigio = { viewModel.setPrestigioOptimization(it) },
                            onToggleBrowserJs = { viewModel.setBrowserJsEnabled(it) },
                            onToggleBrowserImages = { viewModel.setBrowserImagesEnabled(it) },
                            onSelectSearchEngine = { viewModel.setSearchEngine(it) }
                        )
                    }
                }
            }
        }
    }
}
