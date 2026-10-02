package com.example.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.BackupValidationResult
import com.example.domain.equipment.EquipmentType
import com.example.domain.equipment.UserEquipmentProfile
import com.example.domain.offline.OfflinePackageManager
import com.example.domain.offline.model.OfflinePackageStatus
import com.example.ui.components.IceButton
import com.example.ui.components.IceOutlineButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.PersianTopBar
import com.example.ui.parallax.ParallaxConfig
import com.example.ui.parallax.parallaxCardDepth
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.GlacierTeal
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
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    equipmentProfile: UserEquipmentProfile = UserEquipmentProfile(),
    onSaveEquipmentProfile: (UserEquipmentProfile) -> Unit = {},
    onRegenerateSmartProgram: () -> Unit = {},
    reduceMotionEnabled: Boolean,
    onToggleReduceMotion: (Boolean) -> Unit,
    onExportBackup: suspend () -> String,
    onValidateBackup: (String) -> BackupValidationResult,
    onRestoreBackup: suspend (String) -> Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var showHelpScreen by remember { mutableStateOf(false) }

    if (showHelpScreen) {
        BackHandler { showHelpScreen = false }
        HelpScreen(onBack = { showHelpScreen = false })
        return
    }

    val offlinePackageManager = remember { OfflinePackageManager(context) }
    val packageStatus by offlinePackageManager.status.collectAsState()
    val storageMetrics = remember(packageStatus) { offlinePackageManager.calculateStorageMetrics() }

    var exportedJson by remember { mutableStateOf<String?>(null) }
    var restoreInputJson by remember { mutableStateOf("") }
    var restoreValidationResult by remember { mutableStateOf<BackupValidationResult?>(null) }
    var showRestorePreviewDialog by remember { mutableStateOf(false) }
    var restoreStatusText by remember { mutableStateOf<String?>(null) }

    var eqProfile by remember(equipmentProfile) { mutableStateOf(equipmentProfile) }
    var equipmentSavedMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
            .padding(bottom = 80.dp)
            .testTag("settings_screen")
    ) {
        PersianTopBar(
            title = "تنظیمات و راهنما",
            subtitle = "مدیریت داده‌ها، بسته آفلاین و مستندات علمی",
            onBackClick = onBack
        )

        val settingsScrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(settingsScrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            // 1. HELP & DOCUMENTATION CARD (Prominent navigation)
            LuxuryCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showHelpScreen = true }
                    .parallaxCardDepth(settingsScrollState, ParallaxConfig.Minimal, reduceMotionEnabled)
                    .testTag("open_help_screen_card"),
                backgroundColor = Obsidian900
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(IceBlueContainer.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = IceBluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "راهنمای جامع کالیستن",
                                style = MaterialTheme.typography.titleMedium,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "آموزش گام‌به‌گام مهارت‌ها، اصول برنامه‌ریزی و کارکرد آفلاین",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "مشاهده راهنما",
                        tint = IceBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 2. ADAPTIVE EQUIPMENT TRAINING CONFIGURATION (سیستم تمرین هوشمند با تجهیزات)
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = IceBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "سیستم تمرین هوشمند با تجهیزات",
                            style = MaterialTheme.typography.titleMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تطبیق خودکار حجم، تمپو، تکرارها و جایگزینی حرکات بر اساس وزنه‌ها",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LuxuryCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("equipment_settings_card"),
                backgroundColor = Obsidian900
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Active Equipment Summary Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (eqProfile.isPureBodyweight) IceBlueContainer.copy(alpha = 0.35f) else GlacierTeal.copy(alpha = 0.15f))
                            .border(1.dp, if (eqProfile.isPureBodyweight) IceBluePrimary.copy(alpha = 0.4f) else GlacierTeal.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (eqProfile.isPureBodyweight)
                                    "پیکربندی فعال: فقط وزن بدن (کالیستنیکس خالص)"
                                else
                                    "تجهیزات فعال: ${eqProfile.toEquipmentIdList().size} قلم ابزار ورزشی ثبت‌شده",
                                style = MaterialTheme.typography.labelMedium,
                                color = FrostWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = if (eqProfile.isPureBodyweight) Icons.Default.CheckCircle else Icons.Default.Tune,
                                contentDescription = null,
                                tint = if (eqProfile.isPureBodyweight) SafeGreen else IceBlueLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Pure Bodyweight Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (eqProfile.hasBodyweightOnly) Obsidian800 else Obsidian950)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "فقط وزن بدن (بدون هیچ وسیله)",
                                style = MaterialTheme.typography.titleSmall,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تمرکز ۱۰۰٪ روی کالیستنیکس اصیل و تسلط بر اهرم‌های حرکتی زمین",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                        }
                        Switch(
                            checked = eqProfile.hasBodyweightOnly,
                            onCheckedChange = { checked ->
                                eqProfile = eqProfile.copy(hasBodyweightOnly = checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FrostWhite,
                                checkedTrackColor = IceBluePrimary,
                                uncheckedThumbColor = MutedSlate,
                                uncheckedTrackColor = Obsidian700
                            ),
                            modifier = Modifier.testTag("toggle_bodyweight_only")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "تجهیزات وزنی و ابزارهای تمرینی:",
                        style = MaterialTheme.typography.labelLarge,
                        color = IceBlueLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Single Dumbbell
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Obsidian800)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "یک عدد دمبل (Single Dumbbell)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = FrostWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "حرکات یک‌طرفه (Unilateral)، تقویت تعادل و هسته مرکزی",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate
                                )
                            }
                            Switch(
                                checked = eqProfile.hasSingleDumbbell,
                                onCheckedChange = { checked ->
                                    eqProfile = eqProfile.copy(
                                        hasSingleDumbbell = checked,
                                        hasBodyweightOnly = false
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FrostWhite,
                                    checkedTrackColor = IceBluePrimary
                                ),
                                modifier = Modifier.testTag("toggle_single_dumbbell")
                            )
                        }

                        if (eqProfile.hasSingleDumbbell) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "وزن دقیق دمبل:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FrostWhite
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Obsidian700)
                                            .clickable {
                                                val newW = (eqProfile.singleDumbbellWeightKg - 1f).coerceAtLeast(1f)
                                                eqProfile = eqProfile.copy(singleDumbbellWeightKg = newW)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-1", color = FrostWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(IceBlueContainer.copy(alpha = 0.5f))
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.1f", eqProfile.singleDumbbellWeightKg)} kg",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = FrostWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Obsidian700)
                                            .clickable {
                                                val newW = (eqProfile.singleDumbbellWeightKg + 1f).coerceAtMost(80f)
                                                eqProfile = eqProfile.copy(singleDumbbellWeightKg = newW)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+1", color = FrostWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            // Presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(5f, 7.5f, 10f, 15f, 20f).forEach { preset ->
                                    val isSelected = eqProfile.singleDumbbellWeightKg == preset
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) IceBluePrimary else Obsidian700)
                                            .clickable {
                                                eqProfile = eqProfile.copy(singleDumbbellWeightKg = preset)
                                            }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${preset.toInt()}k",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Obsidian950 else FrostWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Pair of Dumbbells
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Obsidian800)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "یک جفت دمبل (Pair of Dumbbells)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = FrostWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تمرینات متقارن دوطرفه با بار آزاد و ساخت توازن عضلانی",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate
                                )
                            }
                            Switch(
                                checked = eqProfile.hasPairDumbbells,
                                onCheckedChange = { checked ->
                                    eqProfile = eqProfile.copy(
                                        hasPairDumbbells = checked,
                                        hasBodyweightOnly = false
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FrostWhite,
                                    checkedTrackColor = IceBluePrimary
                                ),
                                modifier = Modifier.testTag("toggle_pair_dumbbells")
                            )
                        }

                        if (eqProfile.hasPairDumbbells) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "وزن هر دمبل:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FrostWhite
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Obsidian700)
                                            .clickable {
                                                val newW = (eqProfile.pairDumbbellWeightKg - 1f).coerceAtLeast(1f)
                                                eqProfile = eqProfile.copy(pairDumbbellWeightKg = newW)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-1", color = FrostWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(IceBlueContainer.copy(alpha = 0.5f))
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.1f", eqProfile.pairDumbbellWeightKg)} kg",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = FrostWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Obsidian700)
                                            .clickable {
                                                val newW = (eqProfile.pairDumbbellWeightKg + 1f).coerceAtMost(80f)
                                                eqProfile = eqProfile.copy(pairDumbbellWeightKg = newW)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+1", color = FrostWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Adjustable Dumbbells
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Obsidian800)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "دمبل متغیر / قابل تنظیم (Adjustable)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = FrostWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تغییر گام‌به‌گام بار از حداقل تا حداکثر برای حرکات گوناگون",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate
                                )
                            }
                            Switch(
                                checked = eqProfile.hasAdjustableDumbbells,
                                onCheckedChange = { checked ->
                                    eqProfile = eqProfile.copy(
                                        hasAdjustableDumbbells = checked,
                                        hasBodyweightOnly = false
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FrostWhite,
                                    checkedTrackColor = IceBluePrimary
                                ),
                                modifier = Modifier.testTag("toggle_adjustable_dumbbells")
                            )
                        }

                        if (eqProfile.hasAdjustableDumbbells) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Min Weight
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("حداقل وزن:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Obsidian700)
                                                .clickable {
                                                    val n = (eqProfile.adjustableMinWeightKg - 0.5f).coerceAtLeast(1f)
                                                    eqProfile = eqProfile.copy(adjustableMinWeightKg = n)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) { Text("-", color = FrostWhite) }
                                        Text(
                                            "${eqProfile.adjustableMinWeightKg}k",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = FrostWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Obsidian700)
                                                .clickable {
                                                    val n = (eqProfile.adjustableMinWeightKg + 0.5f).coerceAtMost(eqProfile.adjustableMaxWeightKg - 1f)
                                                    eqProfile = eqProfile.copy(adjustableMinWeightKg = n)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) { Text("+", color = FrostWhite) }
                                    }
                                }

                                // Max Weight
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("حداکثر وزن:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Obsidian700)
                                                .clickable {
                                                    val n = (eqProfile.adjustableMaxWeightKg - 1f).coerceAtLeast(eqProfile.adjustableMinWeightKg + 1f)
                                                    eqProfile = eqProfile.copy(adjustableMaxWeightKg = n)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) { Text("-", color = FrostWhite) }
                                        Text(
                                            "${eqProfile.adjustableMaxWeightKg}k",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = FrostWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Obsidian700)
                                                .clickable {
                                                    val n = (eqProfile.adjustableMaxWeightKg + 1f).coerceAtMost(60f)
                                                    eqProfile = eqProfile.copy(adjustableMaxWeightKg = n)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) { Text("+", color = FrostWhite) }
                                    }
                                }

                                // Increment
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("گام تغییر:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Obsidian700)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "${eqProfile.adjustableIncrementKg} kg",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = GlacierTeal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. Barbell
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Obsidian800)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "هالتر و صفحات وزنه (Barbell)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = FrostWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "بارگذاری افزایشی سیستمیک برای پاها و حرکات سنگین ترکیبی",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate
                                )
                            }
                            Switch(
                                checked = eqProfile.hasBarbell,
                                onCheckedChange = { checked ->
                                    eqProfile = eqProfile.copy(
                                        hasBarbell = checked,
                                        hasBodyweightOnly = false
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FrostWhite,
                                    checkedTrackColor = IceBluePrimary
                                ),
                                modifier = Modifier.testTag("toggle_barbell")
                            )
                        }

                        if (eqProfile.hasBarbell) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "مجموع وزن هالتر و صفحات:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FrostWhite
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Obsidian700)
                                            .clickable {
                                                val n = (eqProfile.barbellTotalWeightKg - 5f).coerceAtLeast(10f)
                                                eqProfile = eqProfile.copy(barbellTotalWeightKg = n)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) { Text("-5", color = FrostWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp) }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(IceBlueContainer.copy(alpha = 0.5f))
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${eqProfile.barbellTotalWeightKg.toInt()} kg",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = FrostWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Obsidian700)
                                            .clickable {
                                                val n = (eqProfile.barbellTotalWeightKg + 5f).coerceAtMost(200f)
                                                eqProfile = eqProfile.copy(barbellTotalWeightKg = n)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) { Text("+5", color = FrostWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. Resistance Bands
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Obsidian800)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "کش‌های مقاومتی (Resistance Bands)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = FrostWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تعدیل بار حرکات سخت (کمکی) یا ایجاد مقاومت متغیر صعودی",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate
                                )
                            }
                            Switch(
                                checked = eqProfile.hasResistanceBands,
                                onCheckedChange = { checked ->
                                    eqProfile = eqProfile.copy(
                                        hasResistanceBands = checked,
                                        hasBodyweightOnly = false
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FrostWhite,
                                    checkedTrackColor = IceBluePrimary
                                ),
                                modifier = Modifier.testTag("toggle_resistance_bands")
                            )
                        }

                        if (eqProfile.hasResistanceBands) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("سطح تنش کش‌های موجود:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "ALL" to "ست کامل",
                                    "LIGHT" to "سبک",
                                    "MEDIUM" to "متوسط",
                                    "HEAVY" to "سنگین"
                                ).forEach { (key, label) ->
                                    val isSelected = eqProfile.resistanceBandLevel == key
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) GlacierTeal else Obsidian700)
                                            .clickable {
                                                eqProfile = eqProfile.copy(resistanceBandLevel = key)
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Obsidian950 else FrostWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 7. Pull-up Bar & Calisthenics Gear
                    Text(
                        text = "ابزارهای مهارتی کالیستنیکس و تعلیق:",
                        style = MaterialTheme.typography.labelLarge,
                        color = IceBlueLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Pull-up bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Obsidian800)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "میله بارفیکس (Pull-up Bar)",
                                style = MaterialTheme.typography.titleSmall,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "کشش عمودی، بارفیکس، هالو آویزان و بالابردن پاها",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                        }
                        Switch(
                            checked = eqProfile.hasPullupBar,
                            onCheckedChange = { checked ->
                                eqProfile = eqProfile.copy(hasPullupBar = checked, hasBodyweightOnly = false)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FrostWhite,
                                checkedTrackColor = IceBluePrimary
                            ),
                            modifier = Modifier.testTag("toggle_pullup_bar")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Parallettes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Obsidian800)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "میله پارالل / پارالت (Parallettes)",
                                style = MaterialTheme.typography.titleSmall,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "دیپ موازی، ال‌سیت و ایمنی مچ دست با گریپ خنثی",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                        }
                        Switch(
                            checked = eqProfile.hasParallettes,
                            onCheckedChange = { checked ->
                                eqProfile = eqProfile.copy(hasParallettes = checked, hasBodyweightOnly = false)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FrostWhite,
                                checkedTrackColor = IceBluePrimary
                            ),
                            modifier = Modifier.testTag("toggle_parallettes")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Gymnastic Rings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Obsidian800)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "حلقه‌های ژیمناستیک (Gymnastic Rings)",
                                style = MaterialTheme.typography.titleSmall,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تمرینات بی‌ثبات معلق و درگیری حداکثری عضلات تثبیت‌کننده",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                        }
                        Switch(
                            checked = eqProfile.hasRings,
                            onCheckedChange = { checked ->
                                eqProfile = eqProfile.copy(hasRings = checked, hasBodyweightOnly = false)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FrostWhite,
                                checkedTrackColor = IceBluePrimary
                            ),
                            modifier = Modifier.testTag("toggle_rings")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Weight Vest
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Obsidian800)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "جلیقه وزنی / کمربند وزنه (Weight Vest)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = FrostWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "افزایش ایمن بار کالیستنیکس بدون برهم خوردن بیومکانیک بدن",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate
                                )
                            }
                            Switch(
                                checked = eqProfile.hasWeightVest,
                                onCheckedChange = { checked ->
                                    eqProfile = eqProfile.copy(hasWeightVest = checked, hasBodyweightOnly = false)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FrostWhite,
                                    checkedTrackColor = IceBluePrimary
                                ),
                                modifier = Modifier.testTag("toggle_weight_vest")
                            )
                        }

                        if (eqProfile.hasWeightVest) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("وزن جلیقه:", style = MaterialTheme.typography.bodyMedium, color = FrostWhite)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Obsidian700)
                                            .clickable {
                                                val n = (eqProfile.weightVestWeightKg - 2f).coerceAtLeast(2f)
                                                eqProfile = eqProfile.copy(weightVestWeightKg = n)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) { Text("-2", color = FrostWhite) }
                                    Text(
                                        "${eqProfile.weightVestWeightKg.toInt()} kg",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FrostWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Obsidian700)
                                            .clickable {
                                                val n = (eqProfile.weightVestWeightKg + 2f).coerceAtMost(40f)
                                                eqProfile = eqProfile.copy(weightVestWeightKg = n)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) { Text("+2", color = FrostWhite) }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bench
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Obsidian800)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "نیمکت یا صندلی تمرینی (Bench)",
                                style = MaterialTheme.typography.titleSmall,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تکیه‌گاه برای حرکات تک‌پا (بلگارین)، شیب‌دار و دامنه‌های عمیق",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                        }
                        Switch(
                            checked = eqProfile.hasBench,
                            onCheckedChange = { checked ->
                                eqProfile = eqProfile.copy(hasBench = checked, hasBodyweightOnly = false)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FrostWhite,
                                checkedTrackColor = IceBluePrimary
                            ),
                            modifier = Modifier.testTag("toggle_bench")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Feedback Message
                    if (equipmentSavedMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SafeGreen.copy(alpha = 0.2f))
                                .border(1.dp, SafeGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = equipmentSavedMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = SafeGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IceButton(
                            text = "ذخیره تنظیمات تجهیزات",
                            onClick = {
                                onSaveEquipmentProfile(eqProfile)
                                equipmentSavedMessage = "تنظیمات تجهیزات و وزنه‌ها به صورت آفلاین ذخیره شد."
                                Toast.makeText(context, "تجهیزات با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_equipment_button")
                        )

                        IceOutlineButton(
                            text = "تولید مجدد برنامه هوشمند",
                            onClick = {
                                onSaveEquipmentProfile(eqProfile)
                                onRegenerateSmartProgram()
                                equipmentSavedMessage = "برنامه هوشمند بر اساس تجهیزات جدید بازتولید شد."
                                Toast.makeText(context, "برنامه با تجهیزات جدید بازتولید شد", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("regenerate_program_with_equipment_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Biomechanical Adaptation Principles Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Obsidian950)
                            .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = GlacierTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "اصول تطبیق بیومکانیکی بار در کالیستن:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GlacierTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• وزنه‌های سبک: افزایش هوشمند تکرارها تا ۱۶ و تمپوی کنترل‌شده ۳-۱-۲-۰ جهت ارتقای زمان تحت تنش (TUT).\n" +
                                        "• وزنه‌های سنگین: تعدیل به ۵ تکرار قدرتی بیشینه، استراحت ۱۲۰ ثانیه و RPE 8 متناسب با توان عضلانی.\n" +
                                        "• وزنه‌های نامتناسب: جایگزینی خودکار با حرکات پیشرفته وزن بدن جهت پیشگیری از آسیب تاندون‌ها و مفاصل.\n" +
                                        "• تمام تنظیمات به صورت آفلاین در پایگاه داده داخلی ذخیره و بلافاصله اعمال می‌شوند.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. COMPLETE OFFLINE PACKAGE SECTION
            Text(
                text = "بسته کامل آفلاین (Offline Data Package)",
                style = MaterialTheme.typography.titleMedium,
                color = FrostWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LuxuryCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_package_card"),
                backgroundColor = Obsidian900
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = GlacierTeal,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "بسته آفلاین",
                                style = MaterialTheme.typography.titleMedium,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Status Badge
                        val (statusText, statusColor) = when (packageStatus) {
                            is OfflinePackageStatus.ReadyOffline -> "آماده آفلاین" to SafeGreen
                            is OfflinePackageStatus.Downloading -> "در حال دانلود..." to GlacierTeal
                            is OfflinePackageStatus.Paused -> "متوقف شده" to CautionYellow
                            is OfflinePackageStatus.Verifying -> "بررسی یکپارچگی" to GlacierTeal
                            is OfflinePackageStatus.UpdateAvailable -> "بروزرسانی موجود" to IceBlueLight
                            is OfflinePackageStatus.Error -> "خطا" to AlertRed
                            is OfflinePackageStatus.NotDownloaded -> "دانلود نشده" to MutedSlate
                        }

                        Box(
                            modifier = Modifier
                                .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Storage Metrics (Calculated dynamically, not hardcoded)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Obsidian950, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("حجم دانلود:", style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                            Text(storageMetrics.formattedDownloadSize, style = MaterialTheme.typography.bodySmall, color = FrostWhite, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("فضای موردنیاز:", style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                            Text(storageMetrics.formattedRequiredSpace, style = MaterialTheme.typography.bodySmall, color = FrostWhite, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("فضای آزاد دستگاه:", style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                            Text(
                                storageMetrics.formattedAvailableSpace,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (storageMetrics.hasEnoughSpace) SafeGreen else AlertRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نسخه بسته:", style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                            Text(storageMetrics.packageVersion, style = MaterialTheme.typography.bodySmall, color = IceBlueLight)
                        }
                    }

                    // Download Progress Indicator
                    if (packageStatus is OfflinePackageStatus.Downloading) {
                        val d = packageStatus as OfflinePackageStatus.Downloading
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { d.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .testTag("download_progress_bar"),
                            color = GlacierTeal,
                            trackColor = Obsidian700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "در حال انتقال: ${d.currentFileName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${d.progressPercent.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = GlacierTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Status Messages
                    when (val s = packageStatus) {
                        is OfflinePackageStatus.ReadyOffline -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SafeGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "✓ برنامه آماده استفاده آفلاین است",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SafeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        is OfflinePackageStatus.Error -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AlertRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = s.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AlertRed
                                )
                            }
                        }
                        is OfflinePackageStatus.Verifying -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "در حال بررسی یکپارچگی فایل‌های بسته (${s.currentFileName})...",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlacierTeal
                            )
                        }
                        else -> {}
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (packageStatus) {
                            is OfflinePackageStatus.NotDownloaded, is OfflinePackageStatus.ReadyOffline -> {
                                IceButton(
                                    text = if (packageStatus is OfflinePackageStatus.ReadyOffline) "بروزرسانی مجدد بسته" else "دانلود بسته کامل آفلاین",
                                    onClick = {
                                        coroutineScope.launch {
                                            offlinePackageManager.startDownload()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "download_offline_pkg_btn"
                                )
                            }
                            is OfflinePackageStatus.Downloading -> {
                                IceButton(
                                    text = "توقف",
                                    onClick = { offlinePackageManager.pauseDownload() },
                                    modifier = Modifier.weight(1f),
                                    testTag = "pause_download_btn"
                                )
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            offlinePackageManager.cancelDownload()
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                                    modifier = Modifier.testTag("cancel_download_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Stop, contentDescription = "انصراف")
                                }
                            }
                            is OfflinePackageStatus.Paused -> {
                                IceButton(
                                    text = "ادامه دانلود",
                                    onClick = {
                                        coroutineScope.launch {
                                            offlinePackageManager.startDownload()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "resume_download_btn"
                                )
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            offlinePackageManager.cancelDownload()
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                                    modifier = Modifier.testTag("cancel_download_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Stop, contentDescription = "انصراف")
                                }
                            }
                            is OfflinePackageStatus.Error -> {
                                IceButton(
                                    text = "تلاش مجدد",
                                    onClick = {
                                        coroutineScope.launch {
                                            offlinePackageManager.retryDownload()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "retry_download_btn"
                                )
                            }
                            is OfflinePackageStatus.Verifying -> {
                                Text(
                                    text = "لطفاً صبور باشید...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. BACKUP & RESTORE SECTION
            Text(
                text = "پشتیبان‌گیری و بازیابی داده‌ها (Backup / Restore)",
                style = MaterialTheme.typography.titleMedium,
                color = FrostWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LuxuryCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "خروجی کامل تمام پروفایل‌ها، جلسات تمرینی، ست‌ها، رکوردهای شخصی، مهارت‌ها و وضعیت ریکاوری با فرمت استاندارد kalisten-backup-v1.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IceButton(
                            text = "تولید فایل پشتیبان",
                            onClick = {
                                coroutineScope.launch {
                                    val json = onExportBackup()
                                    exportedJson = json
                                    clipboardManager.setText(AnnotatedString(json))
                                    Toast.makeText(context, "فایل پشتیبان در کلیپ‌بورد کپی شد", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "export_backup_btn"
                        )

                        IceButton(
                            text = "بازیابی از کلیپ‌بورد",
                            onClick = {
                                val clip = clipboardManager.getText()?.text ?: ""
                                if (clip.isNotBlank()) {
                                    restoreInputJson = clip
                                    val validation = onValidateBackup(clip)
                                    restoreValidationResult = validation
                                    if (validation.isValid) {
                                        showRestorePreviewDialog = true
                                    } else {
                                        restoreStatusText = "فایل نامعتبر است: ${validation.errorMessage}"
                                    }
                                } else {
                                    Toast.makeText(context, "کلیپ‌بورد خالی است", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "restore_backup_btn"
                        )
                    }

                    if (restoreStatusText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = restoreStatusText!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (restoreStatusText!!.contains("موفقیت")) SafeGreen else CautionYellow
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. UI & PERFORMANCE SETTINGS
            Text(
                text = "رابط کاربری و عملکرد دستگاه",
                style = MaterialTheme.typography.titleMedium,
                color = FrostWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LuxuryCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "کاهش جلوه‌های حرکتی (Reduce Motion)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "غیرفعال کردن ترنزیشن‌های انیمیشنی سنگین جهت مصرف کمتر باتری و سازگاری با دستگاه‌های ضعیف‌تر.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                    }

                    Switch(
                        checked = reduceMotionEnabled,
                        onCheckedChange = onToggleReduceMotion,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Obsidian950,
                            checkedTrackColor = IceBluePrimary
                        ),
                        modifier = Modifier.testTag("reduce_motion_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. OFFLINE ARCHITECTURE INFO CARD
            LuxuryCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Obsidian900
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = IceBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "معماری آفلاین و استقلال داده‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• پایگاه داده کاملاً محلی Room در دستگاه شما\n• عدم نیاز به هوش مصنوعی سرورمحور یا اتصال اینترنت برای محاسبه بار تمرین\n• موتور اضافه‌بار تدریجی و تفکیک پلاتو بر پایه متغیرهای بیومکانیکی قطعی\n• حفظ کامل تاریخچه نسخه‌ها با قابلیت Rollback",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }

    // Restore Preview Dialog
    if (showRestorePreviewDialog && restoreValidationResult != null) {
        val result = restoreValidationResult!!
        AlertDialog(
            onDismissRequest = { showRestorePreviewDialog = false },
            title = {
                Text(
                    text = "پیش‌نمایش و تایید بازیابی",
                    style = MaterialTheme.typography.titleLarge,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "فایل پشتیبان با موفقیت اعتبارسنجی شد:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FrostWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.previewSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = IceBlueLight
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "آیا مایل به درج و تلفیق داده‌های این پشتیبان در پایگاه داده هستید؟",
                        style = MaterialTheme.typography.bodySmall,
                        color = CautionYellow
                    )
                }
            },
            confirmButton = {
                IceButton(
                    text = "تایید و بازیابی اطلاعات",
                    onClick = {
                        coroutineScope.launch {
                            val success = onRestoreBackup(restoreInputJson)
                            if (success) {
                                restoreStatusText = "بازیابی اطلاعات با موفقیت انجام شد."
                                Toast.makeText(context, "اطلاعات بازیابی شد", Toast.LENGTH_SHORT).show()
                                showRestorePreviewDialog = false
                            } else {
                                restoreStatusText = "خطا در فرآیند بازیابی داده‌ها."
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    testTag = "confirm_restore_btn"
                )
            },
            dismissButton = {
                TextButton(onClick = { showRestorePreviewDialog = false }) {
                    Text("انصراف", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }
}
