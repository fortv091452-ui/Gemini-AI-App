package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WolfiusCommand
import com.example.ui.MainUiState
import com.example.ui.components.MemoryBanner
import com.example.ui.theme.*

@Composable
fun WolfiusToolsScreen(
    uiState: MainUiState,
    onTrimRam: () -> Unit,
    onRunPingTest: () -> Unit,
    onAskGeminiForScript: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(8.dp)
            .testTag("wolfius_tools_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Status Alert if present
        if (!uiState.statusNotification.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.statusNotification,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Target Device Profile Card (Prestigio MultiPad 2 Pro Duo 7.0)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_profile_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TabletAndroid,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Профиль: Prestigio MultiPad 2",
                                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontSize = 16.sp)
                            )
                        }

                        Surface(
                            color = CyanPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Android 4.1 Target",
                                color = CyanPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Модель: Prestigio MultiPad 2 Pro Duo 7.0 (PMP5670C)\n" +
                                "• Процессор: Rockchip RK3066 (2x ARM Cortex-A9 @ 1.5 GHz)\n" +
                                "• Видеоускоритель: ARM Mali-400 MP4 (Quad-Core)\n" +
                                "• ОЗУ: 1024 МБ DDR3 (Выделенный лимит: ~48-64 МБ на процесс)\n" +
                                "• Экран: 7.0\" IPS 1024x600 px (170 ppi)\n" +
                                "• Режим Wolfius: Без Root прав (Rootless ADB / Shell)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // Live Memory & Deep Trim Action
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Менеджер ОЗУ Wolfius (Без Root)",
                        style = MaterialTheme.typography.titleMedium.copy(color = CyanPrimary, fontSize = 15.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    MemoryBanner(
                        memoryStats = uiState.memoryStats,
                        onTrimClick = onTrimRam
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onTrimRam,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("deep_trim_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = Color(0xFF031A24)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Глубокая очистка ОЗУ (Rootless Trim)", fontSize = 13.sp)
                    }
                }
            }
        }

        // Network Socket Latency (Rootless)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Сетевой тест (DNS Socket)",
                            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontSize = 14.sp)
                        )
                        Text(
                            text = when {
                                uiState.isPinging -> "Проверка задержки..."
                                uiState.networkPingMs != null && uiState.networkPingMs >= 0 -> "Пинг: ${uiState.networkPingMs} мс (8.8.8.8)"
                                uiState.networkPingMs != null -> "Ошибка сети"
                                else -> "Нажмите для проверки связи без root"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = CyanSecondary, fontSize = 12.sp)
                        )
                    }

                    Button(
                        onClick = onRunPingTest,
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("ping_test_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceCardElevated,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !uiState.isPinging
                    ) {
                        Text("Пинг", fontSize = 12.sp)
                    }
                }
            }
        }

        // Rootless Commands Library
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Команды Wolfius без root (ADB / Shell):",
                    style = MaterialTheme.typography.titleSmall.copy(color = CyanPrimary, fontSize = 14.sp)
                )

                TextButton(
                    onClick = {
                        onAskGeminiForScript("Напиши rootless bash/adb скрипт для глубокой оптимизации памяти и кэша на слабом планшете Android 4.1.")
                    }
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CyanSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Сгенерировать в AI", fontSize = 11.sp, color = CyanSecondary)
                }
            }
        }

        items(uiState.commands) { cmd ->
            WolfiusCommandCard(
                command = cmd,
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Wolfius Command", cmd.command))
                    Toast.makeText(context, "Команда скопирована!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun WolfiusCommandCard(
    command: WolfiusCommand,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("command_card_${command.title.hashCode()}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = command.title,
                    style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontSize = 13.sp)
                )

                Surface(
                    color = WolfiusPurple.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = command.category,
                        color = WolfiusPurple,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = command.explanation,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Code block
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF070B16),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = command.command,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyanSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Скопировать команду",
                            tint = CyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
