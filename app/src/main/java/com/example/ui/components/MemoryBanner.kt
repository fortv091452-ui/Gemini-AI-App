package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MemoryStats
import com.example.ui.theme.*

@Composable
fun MemoryBanner(
    memoryStats: MemoryStats?,
    onTrimClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (memoryStats == null) return

    val isCritical = memoryStats.isLowMemory || memoryStats.usedPercent > 80
    val barColor = when {
        isCritical -> DangerRed
        memoryStats.usedPercent > 65 -> WarningAmber
        else -> CyanPrimary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("memory_banner_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "ОЗУ",
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ОЗУ: ${memoryStats.usedMemMb} / ${memoryStats.totalMemMb} МБ (${memoryStats.usedPercent}%)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${memoryStats.availMemMb} МБ свободно",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isCritical) DangerRed else SuccessGreen,
                            fontSize = 12.sp
                        )
                    )
                }

                Button(
                    onClick = onTrimClick,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("trim_ram_button"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary.copy(alpha = 0.2f),
                        contentColor = CyanPrimary
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Очистить ОЗУ",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Сброс ОЗУ", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Memory percentage bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF0F172A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = (memoryStats.usedPercent / 100f).coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(barColor)
                )
            }
        }
    }
}
