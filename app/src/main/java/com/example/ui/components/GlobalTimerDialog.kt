package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceBlueLight
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.Obsidian800
import com.example.ui.theme.Obsidian900

@Composable
fun GlobalTimerDialog(
    isOpen: Boolean,
    timerSecondsRemaining: Int,
    timerTotalSeconds: Int,
    isTimerRunning: Boolean,
    stopwatchSeconds: Int,
    isStopwatchRunning: Boolean,
    onStartRestTimer: (Int) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onStartStopwatch: () -> Unit,
    onPauseStopwatch: () -> Unit,
    onResetStopwatch: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var selectedMode by remember { mutableIntStateOf(0) } // 0: Rest Countdown, 1: Stopwatch

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = IceBluePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تایمر و کرونومتر کالیستن",
                    style = MaterialTheme.typography.titleLarge,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabRow(
                    selectedTabIndex = selectedMode,
                    containerColor = Obsidian900,
                    contentColor = IceBluePrimary
                ) {
                    Tab(
                        selected = selectedMode == 0,
                        onClick = { selectedMode = 0 },
                        text = { Text("تایمر استراحت", color = if (selectedMode == 0) IceBlueLight else MutedSlate) }
                    )
                    Tab(
                        selected = selectedMode == 1,
                        onClick = { selectedMode = 1 },
                        text = { Text("کرونومتر رکورد", color = if (selectedMode == 1) IceBlueLight else MutedSlate) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedMode == 0) {
                    // Countdown Display
                    val min = timerSecondsRemaining / 60
                    val sec = timerSecondsRemaining % 60
                    Text(
                        text = String.format("%02d:%02d", min, sec),
                        style = MaterialTheme.typography.displayLarge,
                        color = if (isTimerRunning) IceBlueLight else FrostWhite,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onResetTimer,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Obsidian800)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "تنظیم مجدد", tint = FrostWhite)
                        }

                        IconButton(
                            onClick = if (isTimerRunning) onPauseTimer else onResumeTimer,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(IceBluePrimary)
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isTimerRunning) "مکث" else "شروع",
                                tint = Obsidian900,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(30, 60, 90, 120, 180).forEach { s ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (timerTotalSeconds == s) IceBlueContainer else Obsidian800)
                                    .clickable { onStartRestTimer(s) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${s}s",
                                    color = if (timerTotalSeconds == s) IceBluePrimary else MutedSlate,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                } else {
                    // Stopwatch Display
                    val min = stopwatchSeconds / 60
                    val sec = stopwatchSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", min, sec),
                        style = MaterialTheme.typography.displayLarge,
                        color = if (isStopwatchRunning) IceBlueLight else FrostWhite,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onResetStopwatch,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Obsidian800)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "صفر کردن", tint = FrostWhite)
                        }

                        IconButton(
                            onClick = if (isStopwatchRunning) onPauseStopwatch else onStartStopwatch,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(IceBluePrimary)
                        ) {
                            Icon(
                                imageVector = if (isStopwatchRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isStopwatchRunning) "مکث" else "شروع",
                                tint = Obsidian900,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            IceButton(
                text = "بستن",
                onClick = onDismiss,
                modifier = Modifier.padding(horizontal = 4.dp),
                testTag = "close_global_timer_btn"
            )
        },
        containerColor = Obsidian900
    )
}
