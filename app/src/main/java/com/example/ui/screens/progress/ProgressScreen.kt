package com.example.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.MetricEntity
import com.example.data.local.entity.PREntity
import com.example.data.local.entity.RecoveryEntryEntity
import com.example.data.local.entity.SetLogEntity
import com.example.ui.components.IceButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.PersianTopBar
import com.example.ui.components.RealDataLineChart
import com.example.ui.parallax.LocalReduceMotion
import com.example.ui.parallax.ParallaxConfig
import com.example.ui.parallax.parallaxCardDepth
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceBlueLight
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.Obsidian700
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    metrics: List<MetricEntity>,
    prs: List<PREntity>,
    recoveryEntries: List<RecoveryEntryEntity>,
    setLogs: List<SetLogEntity>,
    onRecordMetric: (type: String, value: Float, unit: String, notes: String) -> Unit,
    onBack: () -> Unit
) {
    var showRecordMetricDialog by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf("ALL") } // 7D, 30D, 90D, ALL

    val cutoffTimestamp = when (selectedRange) {
        "7D" -> System.currentTimeMillis() - 7L * 24 * 3600 * 1000
        "30D" -> System.currentTimeMillis() - 30L * 24 * 3600 * 1000
        "90D" -> System.currentTimeMillis() - 90L * 24 * 3600 * 1000
        else -> 0L
    }

    // Real data extraction for charts
    val filteredMetrics = if (cutoffTimestamp == 0L) metrics else metrics.filter { it.recordedAt >= cutoffTimestamp }
    val filteredRecovery = if (cutoffTimestamp == 0L) recoveryEntries else recoveryEntries.filter { it.recordedAt >= cutoffTimestamp }

    val weightMetrics = filteredMetrics.filter { it.metricType == "WEIGHT" }.sortedBy { it.recordedAt }
    val weightPoints = weightMetrics.map { it.value }
    val weightLabels = weightMetrics.map {
        SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(it.recordedAt))
    }

    val pushupMetrics = filteredMetrics.filter { it.metricType == "PUSHUPS" }.sortedBy { it.recordedAt }
    val pushupPoints = pushupMetrics.map { it.value }
    val pushupLabels = pushupMetrics.map {
        SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(it.recordedAt))
    }

    val sleepPoints = filteredRecovery.sortedBy { it.recordedAt }.takeLast(10).map { it.sleepDurationHours }
    val sleepLabels = filteredRecovery.sortedBy { it.recordedAt }.takeLast(10).map { it.date.takeLast(5) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
            .padding(bottom = 80.dp)
    ) {
        PersianTopBar(
            title = "تحلیل و رکوردهای واقعی",
            subtitle = "نمودارها فقط بر اساس داده‌های ثبت‌شده نمایش داده می‌شوند",
            onBackClick = onBack,
            actions = {
                IconButton(
                    onClick = { showRecordMetricDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IceBlueContainer)
                        .testTag("record_metric_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "ثبت شاخص",
                        tint = IceBluePrimary
                    )
                }
            }
        )

        val scrollState = rememberScrollState()
        val reduceMotion = LocalReduceMotion.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Range Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "بازه زمانی:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                listOf("7D" to "۷ روز", "30D" to "۳۰ روز", "90D" to "۹۰ روز", "ALL" to "همه").forEach { (code, label) ->
                    FilterChip(
                        selected = selectedRange == code,
                        onClick = { selectedRange = code },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart 1: Bodyweight trend
            Text(
                text = "روند تغییرات وزن بدن (کیلوگرم):",
                style = MaterialTheme.typography.titleMedium,
                color = FrostWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.parallaxCardDepth(scrollState, ParallaxConfig.ProgressSubtle, reduceMotion)) {
                RealDataLineChart(
                    dataPoints = weightPoints,
                    labels = weightLabels,
                    unitLabel = "kg",
                    lineColor = IceBluePrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chart 2: Push-up reps trend
            Text(
                text = "روند رکورد شنا (تکرار):",
                style = MaterialTheme.typography.titleMedium,
                color = FrostWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.parallaxCardDepth(scrollState, ParallaxConfig.ProgressSubtle, reduceMotion)) {
                RealDataLineChart(
                    dataPoints = pushupPoints,
                    labels = pushupLabels,
                    unitLabel = "تکرار",
                    lineColor = IceBlueLight
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chart 3: Sleep Duration trend
            Text(
                text = "روند ساعات خواب شبانه:",
                style = MaterialTheme.typography.titleMedium,
                color = FrostWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.parallaxCardDepth(scrollState, ParallaxConfig.ProgressSubtle, reduceMotion)) {
                RealDataLineChart(
                    dataPoints = sleepPoints,
                    labels = sleepLabels,
                    unitLabel = "ساعت",
                    lineColor = IceBluePrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Personal Records (PRs) Showcase
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "رکوردهای شخصی ثبت‌شده (PRs)",
                    style = MaterialTheme.typography.titleLarge,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${prs.size} رکورد",
                    style = MaterialTheme.typography.labelSmall,
                    color = IceBlueLight
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (prs.isEmpty()) {
                LuxuryCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Obsidian900
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MutedSlate,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "هنوز رکورد شخصی ثبت نشده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "با تکمیل ست‌های تمرینی در بخش تمرین، رکوردهای تکرار یا زمان مکث شما به طور خودکار شناسایی و ذخیره می‌گردند.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate
                        )
                    }
                }
            } else {
                prs.forEach { pr ->
                    LuxuryCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        backgroundColor = Obsidian900
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(IceBlueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = IceBluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = pr.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FrostWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(pr.date))
                                    Text(
                                        text = "تاریخ: $dateStr • ${pr.conditionsNote}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedSlate
                                    )
                                }
                            }

                            Text(
                                text = "${pr.value.toInt()} ${pr.unit}",
                                style = MaterialTheme.typography.titleMedium,
                                color = IceBlueLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Record Metric Dialog
    if (showRecordMetricDialog) {
        var metricType by remember { mutableStateOf("WEIGHT") }
        var valueStr by remember { mutableStateOf("") }
        var notesStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRecordMetricDialog = false },
            title = {
                Text(
                    text = "ثبت شاخص و اندازه جدید",
                    style = MaterialTheme.typography.titleLarge,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "نوع شاخص:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "WEIGHT" to "وزن",
                            "PUSHUPS" to "شنا",
                            "PULLUPS" to "بارفیکس",
                            "PLANK" to "پلانک"
                        ).forEach { (type, label) ->
                            FilterChip(
                                selected = metricType == type,
                                onClick = { metricType = type },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { valueStr = it },
                        label = {
                            Text(
                                when (metricType) {
                                    "WEIGHT" -> "مقدار وزن (کیلوگرم)"
                                    "PLANK" -> "زمان مکث (ثانیه)"
                                    else -> "تعداد تکرار"
                                }
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite,
                            focusedBorderColor = IceBluePrimary,
                            unfocusedBorderColor = Obsidian700
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notesStr,
                        onValueChange = { notesStr = it },
                        label = { Text("یادداشت شرایط (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite,
                            focusedBorderColor = IceBluePrimary,
                            unfocusedBorderColor = Obsidian700
                        )
                    )
                }
            },
            confirmButton = {
                IceButton(
                    text = "ثبت شاخص",
                    onClick = {
                        val v = valueStr.toFloatOrNull()
                        if (v != null && v > 0) {
                            val unit = when (metricType) {
                                "WEIGHT" -> "کیلوگرم"
                                "PLANK" -> "ثانیه"
                                else -> "تکرار"
                            }
                            onRecordMetric(metricType, v, unit, notesStr)
                            showRecordMetricDialog = false
                        }
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    testTag = "confirm_record_metric_btn"
                )
            },
            dismissButton = {
                TextButton(onClick = { showRecordMetricDialog = false }) {
                    Text("انصراف", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }
}
