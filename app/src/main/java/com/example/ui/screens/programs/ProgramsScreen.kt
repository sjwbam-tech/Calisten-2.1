package com.example.ui.screens.programs

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.ProgramEntity
import com.example.data.local.entity.ProgramVersionEntity
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.ui.components.IceButton
import com.example.ui.components.IceOutlineButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.PersianTopBar
import com.example.ui.parallax.LocalReduceMotion
import com.example.ui.parallax.ParallaxConfig
import com.example.ui.parallax.parallaxCardDepth
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgramsScreen(
    activeProgram: ProgramEntity?,
    programVersions: List<ProgramVersionEntity>,
    sessions: List<WorkoutSessionEntity>,
    allExercises: List<ExerciseEntity> = emptyList(),
    onCreateCustomProgram: (title: String, description: String, level: String, daysPerWeek: Int, exerciseIds: List<String>) -> Unit = { _, _, _, _, _ -> },
    onRollbackVersion: (Int) -> Unit,
    onStartSession: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sessions & Adaptive, 1: 26-Week Periodization, 2: Versions & Rollback
    var rollbackCandidateVersion by remember { mutableStateOf<ProgramVersionEntity?>(null) }
    var showProgramBuilderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
            .padding(bottom = 80.dp)
    ) {
        PersianTopBar(
            title = "برنامه تمرینی و دوره‌بندی",
            subtitle = if (activeProgram != null) "نسخه فعلی: v${activeProgram.currentVersionNumber}" else "بدون برنامه فعال",
            onBackClick = onBack,
            actions = {
                IconButton(
                    onClick = { showProgramBuilderDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(IceBlueContainer)
                        .testTag("open_program_builder_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "ساخت برنامه",
                        tint = IceBluePrimary
                    )
                }
            }
        )

        val programScrollState = rememberScrollState()
        val reduceMotion = LocalReduceMotion.current

        // Program Header Card
        LuxuryCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .parallaxCardDepth(programScrollState, ParallaxConfig.BuilderSubtle, reduceMotion),
            borderColor = GlassCardBorder
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeProgram?.title ?: "برنامه اختصاصی کالیستنیکس",
                            style = MaterialTheme.typography.titleLarge,
                            color = FrostWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "سطح: ${activeProgram?.level ?: "مبتدی"} • ${activeProgram?.daysPerWeek ?: 3} روز در هفته",
                            style = MaterialTheme.typography.labelSmall,
                            color = IceBlueLight
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(IceBlueContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "v${activeProgram?.currentVersionNumber ?: 1}",
                            style = MaterialTheme.typography.titleMedium,
                            color = IceBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!activeProgram?.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activeProgram?.description ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate
                    )
                }
            }
        }

        // 3 Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Obsidian950,
            contentColor = IceBluePrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = IceBluePrimary
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("جلسات و تطبیق", color = if (selectedTab == 0) IceBlueLight else MutedSlate, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("نقشه‌راه ۲۶ هفته‌ای", color = if (selectedTab == 1) IceBlueLight else MutedSlate, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("نسخه‌ها و Rollback", color = if (selectedTab == 2) IceBlueLight else MutedSlate, fontSize = 13.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(programScrollState)
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Adaptive Engine 6-Pillar Analysis Card (Specification Phase 3 Requirement)
                    LuxuryCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = IceBluePrimary.copy(alpha = 0.4f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = IceBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "گزارش ۶ رکن موتور هوشمند تطبیق برنامه",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FrostWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            AdaptivePillarRow(number = "۱", title = "برنامه پایه (Baseline Plan)", desc = "شنا ۳ ست × ۸ تکرار | پلانک ۳×۳۰ ثانیه")
                            AdaptivePillarRow(number = "۲", title = "هدف جلسه (Session Target)", desc = "رسیدن به ۸ تکرار در تمام ست‌ها با RPE کمتر از ۸")
                            AdaptivePillarRow(number = "۳", title = "عملکرد واقعی (Actual Performance)", desc = "بر مبنای ست‌های ثبت‌شده با دامنه کامل حرکتی")
                            AdaptivePillarRow(number = "۴", title = "پاسخ بدنی و ریکاوری (Body Response)", desc = "پایش شاخص خواب، خستگی عمومی و عدم وجود درد مفصلی")
                            AdaptivePillarRow(number = "۵", title = "تصمیم موتور (Decision)", desc = "اضافه‌بار تک‌متغیره در تکرار یا زمان مکث بدون جهش ناگهانی")
                            AdaptivePillarRow(number = "۶", title = "دلیل فیزیولوژیک (Reason)", desc = "پیشرفت دوگانه تدریجی جهت سازگاری تاندون و جلوگیری از پلاتو")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "جلسات تمرینی برنامه:",
                        style = MaterialTheme.typography.titleMedium,
                        color = FrostWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (sessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هنوز جلسه‌ای برای این برنامه برنامه‌ریزی نشده است.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSlate
                            )
                        }
                    } else {
                        sessions.forEach { s ->
                            LuxuryCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                onClick = { onStartSession(s.id) }
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
                                                .size(40.dp)
                                                .clip(CircleShape)
                                            .background(if (s.status == "COMPLETED") SafeGreen.copy(alpha = 0.2f) else Obsidian800),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (s.status == "COMPLETED") Icons.Default.CheckCircle else Icons.Default.FitnessCenter,
                                                contentDescription = null,
                                                tint = if (s.status == "COMPLETED") SafeGreen else IceBluePrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = s.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = FrostWhite,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "وضعیت: ${if (s.status == "COMPLETED") "تکمیل‌شده" else if (s.status == "IN_PROGRESS") "در حال اجرا" else "برنامه‌ریزی‌شده"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (s.status == "COMPLETED") SafeGreen else MutedSlate
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = IceBlueLight,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // 26-Week Periodization Roadmap (Specification Item 9)
                    Text(
                        text = "نقشه‌راه دوره‌بندی ۲۶ هفته‌ای کالیستنیکس (Meso-cycles):",
                        style = MaterialTheme.typography.titleMedium,
                        color = FrostWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PeriodizationPhaseCard(
                        phaseNumber = "فاز ۱ (هفته‌های ۱ تا ۶)",
                        phaseTitle = "سازگاری ساختاری و پایه‌ای (Anatomic Adaptation)",
                        description = "تقویت مفاصل، تاندون‌ها و الگوهای حرکتی اولیه (شنا، بارفیکس، اسکات و هالو بادی). انتهای هفته ۶ دی‌لود استراتژیک جهت سازگاری بافت همبند."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PeriodizationPhaseCard(
                        phaseNumber = "فاز ۲ (هفته‌های ۷ تا ۱۲)",
                        phaseTitle = "هایپرتروفی و حجم عضلانی (Calisthenics Hypertrophy)",
                        description = "افزایش حجم ست‌های هفتگی با دامنه تکرار ۸ تا ۱۲ و اضافه‌بار تدریجی دوگانه. بهبود ظرفیت کار عضلانی."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PeriodizationPhaseCard(
                        phaseNumber = "فاز ۳ (هفته‌های ۱۳ تا ۱۸)",
                        phaseTitle = "قدرت بیشینه و ورود به مهارت‌ها (Max Strength & Skills)",
                        description = "انتقال قدرت به مهارت‌های استاتیک (تاک فرانت لور، تاک پلانچ، دیپ پارالل). تمرین با RPE کنترل‌شده و استراحت‌های ۲ دقیقه‌ای."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PeriodizationPhaseCard(
                        phaseNumber = "فاز ۴ (هفته‌های ۱۹ تا ۲۶)",
                        phaseTitle = "اوج عملکرد و تلفیق مهارت‌های پیشرفته (Peak & Mastery)",
                        description = "تسلط بر فرم‌های پیشرفته، ارتقای رکوردهای استاتیک و دینامیک (ماسل‌آپ و بالانس) و فاز نهایی بازبینی پیشرفت ۲۶ هفته‌ای."
                    )
                }

                2 -> {
                    // Program Versions & Rollback Tab
                    Text(
                        text = "تاریخچه ماندگار نسخه‌های برنامه تمرینی:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedSlate
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (programVersions.isEmpty()) {
                        Text(
                            text = "اطلاعات نسخه‌ای یافت نشد.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSlate
                        )
                    } else {
                        programVersions.forEach { ver ->
                            val isCurrent = ver.versionNumber == activeProgram?.currentVersionNumber
                            LuxuryCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                borderColor = if (isCurrent) IceBluePrimary else GlassCardBorderSubtle
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = if (isCurrent) IceBluePrimary else MutedSlate,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "نسخه v${ver.versionNumber}",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = FrostWhite,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isCurrent) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(IceBlueContainer)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "نسخه فعال",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = IceBluePrimary
                                                    )
                                                }
                                            }
                                        }

                                        val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(ver.createdAt))
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedSlate
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "دلیل نسخه: ${ver.reason}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FrostWhite
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "خلاصه تغییرات: ${ver.changesSummary}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSlate
                                    )

                                    if (!isCurrent) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        IceOutlineButton(
                                            text = "بازگشت به این نسخه (Rollback)",
                                            onClick = { rollbackCandidateVersion = ver },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Program Builder Dialog (Specification Requirement 14)
    if (showProgramBuilderDialog) {
        var builderMode by remember { mutableStateOf("SMART") } // SMART, MANUAL, FROM_SCRATCH
        var programTitle by remember { mutableStateOf("برنامه تخصصی کالیستنیکس") }
        var programLevel by remember { mutableStateOf("INTERMEDIATE") }
        var daysPerWeek by remember { mutableIntStateOf(3) }
        val selectedExercises = remember { mutableStateListOf<String>() }

        AlertDialog(
            onDismissRequest = { showProgramBuilderDialog = false },
            title = {
                Text(
                    text = "سازنده برنامه تمرینی (Program Builder)",
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
                    Text(text = "حالت ساخت برنامه:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("SMART" to "هوشمند", "MANUAL" to "دستی", "FROM_SCRATCH" to "از پایه").forEach { (code, label) ->
                            FilterChip(
                                selected = builderMode == code,
                                onClick = { builderMode = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = programTitle,
                        onValueChange = { programTitle = it },
                        label = { Text("عنوان برنامه") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite,
                            focusedBorderColor = IceBluePrimary,
                            unfocusedBorderColor = Obsidian700
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "تعداد روزهای تمرین در هفته: $daysPerWeek روز", style = MaterialTheme.typography.labelSmall, color = FrostWhite)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (2..5).forEach { d ->
                            FilterChip(
                                selected = daysPerWeek == d,
                                onClick = { daysPerWeek = d },
                                label = { Text("$d روز") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "انتخاب حرکات اصلی و کمکی جلسه:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                    Spacer(modifier = Modifier.height(6.dp))
                    allExercises.take(10).forEach { ex ->
                        val isChecked = selectedExercises.contains(ex.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedExercises.remove(ex.id) else selectedExercises.add(ex.id)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isChecked) IceBluePrimary else Obsidian700),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChecked) Icon(Icons.Default.Check, contentDescription = null, tint = Obsidian950, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${ex.persianName} (${ex.category})", color = FrostWhite, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                IceButton(
                    text = "ایجاد برنامه و ثبت نسخه ۱",
                    onClick = {
                        val finalExercises = if (selectedExercises.isNotEmpty()) selectedExercises else listOf("pushup_std", "pullup_std", "squat_bodyweight", "plank_std")
                        onCreateCustomProgram(
                            programTitle.trim().ifEmpty { "برنامه کالیستنیکس" },
                            "برنامه ایجادشده در حالت $builderMode با تفکیک ساختار هفتگی",
                            programLevel,
                            daysPerWeek,
                            finalExercises
                        )
                        showProgramBuilderDialog = false
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    testTag = "confirm_create_program_btn"
                )
            },
            dismissButton = {
                TextButton(onClick = { showProgramBuilderDialog = false }) {
                    Text("انصراف", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }

    // Rollback Confirmation Dialog
    if (rollbackCandidateVersion != null) {
        val targetVer = rollbackCandidateVersion!!
        AlertDialog(
            onDismissRequest = { rollbackCandidateVersion = null },
            title = {
                Text(
                    text = "تایید بازگشت به نسخه v${targetVer.versionNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "آیا مایلید ساختار تمرینی برنامه به تنظیمات نسخه v${targetVer.versionNumber} بازگردد؟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FrostWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "نکته ایمنی: نسخه فعلی نیز به طور خودکار در تاریخچه نگهداری خواهد شد و هیچ داده‌ای از بین نمی‌رود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CautionYellow
                    )
                }
            },
            confirmButton = {
                IceButton(
                    text = "تایید بازگشت (Rollback)",
                    onClick = {
                        onRollbackVersion(targetVer.versionNumber)
                        rollbackCandidateVersion = null
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { rollbackCandidateVersion = null }) {
                    Text("انصراف", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }
}

@Composable
fun AdaptivePillarRow(number: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$number. ",
            style = MaterialTheme.typography.labelSmall,
            color = IceBluePrimary,
            fontWeight = FontWeight.Bold
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = FrostWhite,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MutedSlate,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun PeriodizationPhaseCard(phaseNumber: String, phaseTitle: String, description: String) {
    LuxuryCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Obsidian900
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phaseNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = IceBlueLight,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = IceBluePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = phaseTitle,
                style = MaterialTheme.typography.titleMedium,
                color = FrostWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MutedSlate,
                lineHeight = 20.sp
            )
        }
    }
}
