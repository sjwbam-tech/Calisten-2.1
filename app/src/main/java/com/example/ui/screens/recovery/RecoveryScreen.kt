package com.example.ui.screens.recovery

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PainEntryEntity
import com.example.data.local.entity.RecoveryEntryEntity
import com.example.ui.components.IceButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.PersianTopBar
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceBlueLight
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.Obsidian700
import com.example.ui.theme.Obsidian800
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950
import com.example.ui.theme.SafeGreen

@Composable
fun RecoveryScreen(
    recoveryEntries: List<RecoveryEntryEntity>,
    painEntries: List<PainEntryEntity>,
    onLogRecovery: (
        sleepHours: Float,
        sleepQuality: Int,
        energyLevel: Int,
        fatigueLevel: Int,
        muscleSoreness: Int,
        stressLevel: Int,
        notes: String
    ) -> Unit,
    onLogPain: (
        location: String,
        severity: Int,
        timing: String,
        duringWorkout: Boolean,
        afterWorkout: Boolean,
        nextDay: Boolean,
        effectOnForm: Boolean,
        notes: String
    ) -> Unit,
    onBack: () -> Unit
) {
    var showCheckInDialog by remember { mutableStateOf(false) }
    var showPainLogDialog by remember { mutableStateOf(false) }

    val latestRecovery = recoveryEntries.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
            .padding(bottom = 80.dp)
    ) {
        PersianTopBar(
            title = "ریکاوری و مدیریت درد",
            subtitle = "راهنمای فیزیولوژیک آمادگی تمرین و مراقبت از مفاصل",
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Readiness Status Card
            LuxuryCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = GlassCardBorder
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val readinessColor = when (latestRecovery?.readiness) {
                                "GREEN" -> SafeGreen
                                "YELLOW" -> CautionYellow
                                "RED" -> AlertRed
                                else -> IceBluePrimary
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(readinessColor.copy(alpha = 0.2f))
                                    .border(2.dp, readinessColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Nightlight,
                                    contentDescription = null,
                                    tint = readinessColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "شاخص آمادگی بدنی (Readiness)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FrostWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when (latestRecovery?.readiness) {
                                        "GREEN" -> "وضعیت سبز: عالی برای بارگذاری پرتوان"
                                        "YELLOW" -> "وضعیت زرد: خستگی متوسط؛ کنترل RPE الزامی است"
                                        "RED" -> "وضعیت قرمز: خستگی یا درد بالا؛ تمرین سبک یا ریکاوری"
                                        else -> "امروز هنوز ارزیابی نشده است"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = readinessColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "خواب دیشب:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                            Text(
                                text = if (latestRecovery != null) "${latestRecovery.sleepDurationHours} ساعت" else "--",
                                style = MaterialTheme.typography.titleMedium,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(text = "سطح انرژی:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                            Text(
                                text = if (latestRecovery != null) "${latestRecovery.energyLevel} از ۵" else "--",
                                style = MaterialTheme.typography.titleMedium,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(text = "خستگی عمومی:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                            Text(
                                text = if (latestRecovery != null) "${latestRecovery.fatigueLevel} از ۵" else "--",
                                style = MaterialTheme.typography.titleMedium,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    IceButton(
                        text = "ثبت چک‌این ریکاوری امروز",
                        onClick = { showCheckInDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "log_recovery_btn"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Medical Disclaimer Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Obsidian900)
                    .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = IceBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "کالیستن تشخیص پزشکی یا ادعای درمانی صادر نمی‌کند. در صورت احساس درد شدید، تورم، بی‌حسی، ناپایداری مفصل یا علائم قرمز، تمرین را بلافاصله متوقف کرده و به پزشک متخصص ارتوپد یا فیزیوتراپیست مراجعه فرمایید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pain Tracking Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ثبت و پایش درد و ناراحتی مفاصل",
                    style = MaterialTheme.typography.titleLarge,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { showPainLogDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(IceBlueContainer)
                        .testTag("log_pain_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "ثبت درد", tint = IceBluePrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (painEntries.isEmpty()) {
                LuxuryCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Obsidian900
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "خوشبختانه هیچ سابقه دردی ثبت نشده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FrostWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "در صورت احساس هرگونه درد غیرعضلانی در شانه، آرنج یا مچ، با لمس دکمه + آن را ثبت کنید تا سیستم برنامه را تعدیل کند.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                painEntries.forEach { pe ->
                    val statusColor = when (pe.status) {
                        "RED" -> AlertRed
                        "YELLOW" -> CautionYellow
                        else -> SafeGreen
                    }
                    LuxuryCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        borderColor = statusColor.copy(alpha = 0.4f)
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
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "محل: ${pe.location} (شدت ${pe.severity} از ۱۰)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FrostWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "زمان: ${pe.timing} • تاریخ: ${pe.date}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedSlate
                                    )
                                }
                            }

                            Text(
                                text = when (pe.status) {
                                    "RED" -> "وضعیت قرمز (توقف)"
                                    "YELLOW" -> "وضعیت زرد (تعدیل)"
                                    else -> "خفیف"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    // Daily Recovery Check-in Dialog
    if (showCheckInDialog) {
        var sleepHoursInput by remember { mutableFloatStateOf(7.5f) }
        var sleepQualityInput by remember { mutableIntStateOf(4) }
        var energyInput by remember { mutableIntStateOf(4) }
        var fatigueInput by remember { mutableIntStateOf(2) }
        var sorenessInput by remember { mutableIntStateOf(2) }
        var stressInput by remember { mutableIntStateOf(2) }
        var recoveryNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCheckInDialog = false },
            title = {
                Text(
                    text = "ثبت وضعیت روزانه و خواب",
                    style = MaterialTheme.typography.titleLarge,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = "مدت خواب دیشب: ${String.format("%.1f", sleepHoursInput)} ساعت", color = FrostWhite)
                    Slider(
                        value = sleepHoursInput,
                        onValueChange = { sleepHoursInput = it },
                        valueRange = 3f..12f,
                        steps = 17,
                        colors = SliderDefaults.colors(thumbColor = IceBluePrimary, activeTrackColor = IceBluePrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "کیفیت خواب: $sleepQualityInput از ۵", color = FrostWhite)
                    Slider(
                        value = sleepQualityInput.toFloat(),
                        onValueChange = { sleepQualityInput = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = IceBluePrimary, activeTrackColor = IceBluePrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "میزان انرژی امروز: $energyInput از ۵", color = FrostWhite)
                    Slider(
                        value = energyInput.toFloat(),
                        onValueChange = { energyInput = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = IceBluePrimary, activeTrackColor = IceBluePrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "میزان خستگی: $fatigueInput از ۵", color = FrostWhite)
                    Slider(
                        value = fatigueInput.toFloat(),
                        onValueChange = { fatigueInput = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = IceBluePrimary, activeTrackColor = IceBluePrimary)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = recoveryNotes,
                        onValueChange = { recoveryNotes = it },
                        label = { Text("یادداشت روزانه (اختیاری)") },
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
                    text = "ذخیره وضعیت",
                    onClick = {
                        onLogRecovery(
                            sleepHoursInput,
                            sleepQualityInput,
                            energyInput,
                            fatigueInput,
                            sorenessInput,
                            stressInput,
                            recoveryNotes
                        )
                        showCheckInDialog = false
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    testTag = "confirm_log_recovery_btn"
                )
            },
            dismissButton = {
                TextButton(onClick = { showCheckInDialog = false }) {
                    Text("انصراف", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }

    // Pain Log Dialog
    if (showPainLogDialog) {
        var location by remember { mutableStateOf("کتف") }
        var severity by remember { mutableIntStateOf(3) }
        var timing by remember { mutableStateOf("حین ست تمرین") }
        var effectOnForm by remember { mutableStateOf(false) }
        var painNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPainLogDialog = false },
            title = {
                Text(
                    text = "ثبت گزارش ناراحتی یا درد",
                    style = MaterialTheme.typography.titleLarge,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = "محل ناراحتی:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("کتف", "آرنج", "مچ دست", "کمر", "زانو").forEach { loc ->
                            FilterChip(
                                selected = location == loc,
                                onClick = { location = loc },
                                label = { Text(loc) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "شدت درد: $severity از ۱۰", color = FrostWhite)
                    Slider(
                        value = severity.toFloat(),
                        onValueChange = { severity = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = if (severity >= 7) AlertRed else if (severity >= 4) CautionYellow else SafeGreen,
                            activeTrackColor = if (severity >= 7) AlertRed else IceBluePrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "زمان بروز درد:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("قبل تمرین", "حین ست", "بعد تمرین", "روز بعد").forEach { t ->
                            FilterChip(
                                selected = timing == t,
                                onClick = { timing = t },
                                label = { Text(t) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    FilterChip(
                        selected = effectOnForm,
                        onClick = { effectOnForm = !effectOnForm },
                        label = { Text("باعث افت یا تغییر در فرم حرکت شد") }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = painNotes,
                        onValueChange = { painNotes = it },
                        label = { Text("توضیحات تکمیلی") },
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
                    text = "ثبت در سامانه ایمنی",
                    onClick = {
                        onLogPain(
                            location,
                            severity,
                            timing,
                            timing == "حین ست",
                            timing == "بعد تمرین",
                            timing == "روز بعد",
                            effectOnForm,
                            painNotes
                        )
                        showPainLogDialog = false
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    testTag = "confirm_log_pain_btn"
                )
            },
            dismissButton = {
                TextButton(onClick = { showPainLogDialog = false }) {
                    Text("انصراف", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }
}
