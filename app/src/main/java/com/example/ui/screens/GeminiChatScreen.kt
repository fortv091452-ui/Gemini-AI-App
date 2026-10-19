package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.ui.MainUiState
import com.example.ui.components.MemoryBanner
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GeminiChatScreen(
    uiState: MainUiState,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onTrimRam: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val quickPrompts = listOf(
        "Оптимизация Prestigio PMP5670C",
        "Скрипты Wolfius без root",
        "Легковесный браузер для 1ГБ ОЗУ",
        "Как ускорить Rockchip RK3066?",
        "Отключить лишние службы Android 4.1"
    )

    // Auto-scroll to bottom on new message
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(8.dp)
    ) {
        // RAM Monitor Banner
        MemoryBanner(
            memoryStats = uiState.memoryStats,
            onTrimClick = onTrimRam
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Suggestion Chips (Horizontal Scroll)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickPrompts.forEach { prompt ->
                SuggestionChip(
                    onClick = {
                        inputText = prompt
                    },
                    label = {
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            color = CyanSecondary
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = SurfaceCardDark
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = CyanPrimary.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("quick_prompt_chip_${prompt.hashCode()}")
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_messages_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            items(uiState.messages, key = { it.id }) { message ->
                ChatMessageBubble(
                    message = message,
                    onCopy = { textToCopy ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Wolfius Gemini", textToCopy))
                        Toast.makeText(context, "Скопировано в буфер", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Loading Indicator
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .testTag("chat_loading_indicator"),
                color = CyanPrimary,
                trackColor = SurfaceCardDark
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Input Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClearChat,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Очистить чат",
                    tint = TextMuted
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                placeholder = {
                    Text(
                        "Запрос к Gemini (Prestigio / Wolfius)...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                maxLines = 3,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary,
                    fontSize = 14.sp
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = SurfaceCardElevated,
                    focusedContainerColor = SurfaceCardDark,
                    unfocusedContainerColor = SurfaceCardDark
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank() && !uiState.isLoading) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (inputText.isNotBlank()) CyanPrimary else SurfaceCardElevated)
                    .testTag("send_message_button"),
                enabled = inputText.isNotBlank() && !uiState.isLoading
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Отправить",
                    tint = if (inputText.isNotBlank()) Color(0xFF031A24) else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    onCopy: (String) -> Unit
) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = when {
        message.isError -> DangerRed.copy(alpha = 0.2f)
        isUser -> Color(0xFF0F324D)
        else -> SurfaceCardDark
    }
    val borderColor = when {
        message.isError -> DangerRed.copy(alpha = 0.5f)
        isUser -> CyanPrimary.copy(alpha = 0.4f)
        else -> SurfaceCardElevated
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
            ),
            color = bubbleColor,
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor)),
            modifier = Modifier
                .widthIn(max = 560.dp)
                .padding(horizontal = 4.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Wolfius Gemini (Low RAM)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyanSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        IconButton(
                            onClick = { onCopy(message.text) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Копировать",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontFamily = if (message.text.contains("adb shell") || message.text.contains("pm trim")) FontFamily.Monospace else FontFamily.Default
                    )
                )
            }
        }
    }
}
