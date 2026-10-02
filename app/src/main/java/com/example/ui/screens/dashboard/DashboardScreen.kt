package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.PREntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ProgramEntity
import com.example.data.local.entity.RecoveryEntryEntity
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.ui.components.AtmosphereLayer
import com.example.ui.components.EditorialNumber
import com.example.ui.components.GlassPanel
import com.example.ui.components.IceButton
import com.example.ui.components.IceOutlineButton
import com.example.ui.components.SceneIndicator
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceBlueGlow
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
fun DashboardScreen(
    activeProfile: ProfileEntity,
    activeProgram: ProgramEntity?,
    todaySession: WorkoutSessionEntity?,
    recentRecovery: RecoveryEntryEntity?,
    latestPR: PREntity?,
    todayHabits: List<String> = emptyList(),
    reduceMotion: Boolean = false,
    onToggleHabit: (String) -> Unit = {},
    onStartWorkout: (String) -> Unit,
    onNavigateToSection: (String) -> Unit,
    onOpenCheckInDialog: () -> Unit,
    onOpenProfileSwitcher: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showBrandPhilosophyDialog by remember { mutableStateOf(false) }

    // Real scroll progress computation based on visible items
    val currentSceneIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex.coerceIn(0, 5)
        }
    }

    val scrollOffsetFraction by remember {
        derivedStateOf {
            val index = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            (index * 1000f + offset) / 5000f
        }
    }

    val sceneTitlesFa = listOf(
        "۰۱ ورود به کالیستن",
        "۰۲ فاز و ساختار برنامه",
        "۰۳ اولویت تمرین امروز",
        "۰۴ وضعیت فیزیولوژیک و ریکاوری",
        "۰۵ پیش‌نمایش تمرین",
        "۰۶ آمادگی و اقدام"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
    ) {
        // =========================================================================
        // STICKY CINEMATIC ENVIRONMENT (Crossfades & Parallax Layers on GPU)
        // =========================================================================

        // Layer A: Towering Granite Cliffs & Mountain Sunrise (Scene 01, fades out as user progresses)
        val landscapeAlpha = if (reduceMotion) {
            if (currentSceneIndex <= 1) 1.0f else 0.0f
        } else {
            (1.0f - scrollOffsetFraction * 2.2f).coerceIn(0f, 1f)
        }

        val landscapeScale = if (reduceMotion) 1.0f else 1.0f + (scrollOffsetFraction * 0.15f)
        val landscapeTranslationY = if (reduceMotion) 0f else -scrollOffsetFraction * 120f

        if (landscapeAlpha > 0.01f) {
            Image(
                painter = painterResource(id = R.drawable.rocky_granite_cliff_1790772687837),
                contentDescription = "دیواره‌های صخره‌ای گرانیتی در درخشش سپیده‌دم کوهستان",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = landscapeAlpha
                        this.scaleX = landscapeScale
                        this.scaleY = landscapeScale
                        this.translationY = landscapeTranslationY
                    }
            )
        }

        // Layer B: Jagged Mountain Peaks & Mist (Scenes 02-06, crossfades in)
        val mountainAlpha = if (reduceMotion) {
            if (currentSceneIndex > 1) 0.85f else 0.0f
        } else {
            ((scrollOffsetFraction - 0.2f) * 2.0f).coerceIn(0f, 0.85f)
        }

        val mountainScale = if (reduceMotion) 1.0f else 1.08f - (scrollOffsetFraction * 0.08f)
        val mountainTranslationY = if (reduceMotion) 0f else -(scrollOffsetFraction - 0.2f) * 80f

        if (mountainAlpha > 0.01f) {
            Image(
                painter = painterResource(id = R.drawable.img_mountain_summit_mist_1790632774116),
                contentDescription = "قله‌های مه‌آلود کوهستانی کالیستن",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = mountainAlpha
                        this.scaleX = mountainScale
                        this.scaleY = mountainScale
                        this.translationY = mountainTranslationY
                    }
            )
        }

        // Atmospheric Depth & Vignette
        AtmosphereLayer(
            vignetteIntensity = 0.85f,
            iceGlowIntensity = 0.35f
        )

        // =========================================================================
        // SCROLLABLE CINEMATIC SCENE CONTENT
        // =========================================================================
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // ---------------------------------------------------------------------
            // SCENE 01 — ARRIVAL
            // ---------------------------------------------------------------------
            item(key = "scene_01_arrival") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(680.dp)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(100.dp))

                        // Luxury Emblem
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Obsidian900.copy(alpha = 0.8f))
                                .border(1.dp, GlassCardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = IceBluePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        var titleFontSize by remember { mutableStateOf(34.sp) }

                        Text(
                            text = "کالیستن",
                            modifier = Modifier
                                .testTag("app-title-text")
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Black,
                                color = FrostWhite,
                                letterSpacing = 0.sp,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            textAlign = TextAlign.Center,
                            onTextLayout = { textLayoutResult ->
                                if (textLayoutResult.hasVisualOverflow && titleFontSize > 18.sp) {
                                    titleFontSize = (titleFontSize.value - 2f).sp
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "سیستم هوشمند تسلط بر وزن بدن",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IceBlueLight,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "بر پایه بیومکانیک حرکتی، ظرفیت بافت‌های اتصالی و استدلال عینی داده‌ها",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp),
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(IceBlueContainer.copy(alpha = 0.55f))
                                .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
                                .clickable { showBrandPhilosophyDialog = true }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("brand_philosophy_quote_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "«قدرت در حجم نیست؛ در تراکم، کنترل و توانایی نهفته است.»",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IceBlueLight,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "درباره فلسفه کالیستن",
                                    tint = IceBluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(70.dp))

                        // Cinematic Scroll Prompt
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Obsidian900.copy(alpha = 0.6f))
                                .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(20.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(1)
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "برای ورود به برنامه اسکرول کنید",
                                style = MaterialTheme.typography.labelSmall,
                                color = IceBlueLight,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "اسکرول به پایین",
                                tint = IceBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // ---------------------------------------------------------------------
            // SCENE 02 — CURRENT PHASE & PROGRAM STRUCTURE
            // ---------------------------------------------------------------------
            item(key = "scene_02_phase") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    GlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Obsidian900.copy(alpha = 0.88f),
                        borderColor = GlassCardBorderSubtle,
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "فاز و ساختار تمرینی",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = IceBlueLight,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeProgram?.title ?: "برنامه فعال ثبت نشده",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val weekStr = if (activeProgram != null) {
                                    String.format("%02d", (activeProgram.currentVersionNumber * 2).coerceIn(1, 26))
                                } else "۰۱"

                                EditorialNumber(
                                    number = weekStr,
                                    fontSize = 72.sp,
                                    color = FrostWhite,
                                    subLabel = "هفته جاری"
                                )

                                Box(
                                    modifier = Modifier
                                        .height(60.dp)
                                        .width(1.dp)
                                        .background(GlassCardBorderSubtle)
                                )

                                EditorialNumber(
                                    number = "${activeProgram?.daysPerWeek ?: activeProfile.trainingDaysPerWeek}",
                                    fontSize = 72.sp,
                                    color = IceBluePrimary,
                                    subLabel = "جلسه در هفته"
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = activeProgram?.description ?: "برنامه شخصی‌سازی‌شده بر پایه مشخصات بیومکانیک و سوابق توانایی کاربر",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                IceOutlineButton(
                                    text = "مدیریت برنامه و نسخه‌ها",
                                    onClick = { onNavigateToSection("programs") },
                                    modifier = Modifier.height(44.dp),
                                    testTag = "btn_manage_program"
                                )
                            }
                        }
                    }
                }
            }

            // ---------------------------------------------------------------------
            // SCENE 03 — TODAY'S PRIORITY WORKOUT
            // ---------------------------------------------------------------------
            item(key = "scene_03_today") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    GlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Obsidian900.copy(alpha = 0.90f),
                        borderColor = if (todaySession != null) GlassCardBorder else GlassCardBorderSubtle,
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (todaySession != null) IceBluePrimary else SafeGreen)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "اولویت امروز",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = IceBlueLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = todaySession?.status ?: "REST_OR_RECOVERY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = todaySession?.title ?: "روز بازیابی فعال و تحرک‌پذیری مفاصل",
                                style = MaterialTheme.typography.titleLarge,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (todaySession != null) {
                                    "تمرکز بر الگوهای فشاری و کششی اصلی با هدف حفظ تعادل تاندونی و پیشرفت تکنیکال"
                                } else {
                                    "امروز تمرین اصلی برنامه‌ریزی نشده است؛ روی خواب، پروتئین و ریکاوری تاندون‌ها تمرکز کنید."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("مدت تقریبی", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                                    Text("~۵۵ دقیقه", style = MaterialTheme.typography.titleMedium, color = FrostWhite, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("دامنه RPE هدف", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                                    Text("۷ الی ۸.۵", style = MaterialTheme.typography.titleMedium, color = IceBluePrimary, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("استراحت بین ست", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                                    Text("۹۰-۱۲۰ ثانیه", style = MaterialTheme.typography.titleMedium, color = FrostWhite, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            if (todaySession != null) {
                                IceButton(
                                    text = "شروع تمرین امروز (HUD)",
                                    onClick = { onStartWorkout(todaySession.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "btn_start_today_workout"
                                )
                            } else {
                                IceOutlineButton(
                                    text = "ورود به بخش برنامه‌ها و انتخاب جلسه",
                                    onClick = { onNavigateToSection("programs") },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "btn_pick_session"
                                )
                            }
                        }
                    }
                }
            }

            // ---------------------------------------------------------------------
            // SCENE 04 — BODY & RECOVERY PHYSIOLOGICAL STATUS
            // ---------------------------------------------------------------------
            item(key = "scene_04_body_recovery") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    GlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Obsidian900.copy(alpha = 0.88f),
                        borderColor = GlassCardBorderSubtle,
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "وضعیت فیزیولوژیک و ریکاوری",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = IceBlueLight,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "داده‌های واقعی کاربر",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (recentRecovery != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    EditorialNumber(
                                        number = "${recentRecovery.sleepDurationHours}h",
                                        fontSize = 38.sp,
                                        color = FrostWhite,
                                        subLabel = "مدت خواب"
                                    )
                                    EditorialNumber(
                                        number = "${recentRecovery.energyLevel}/10",
                                        fontSize = 38.sp,
                                        color = IceBluePrimary,
                                        subLabel = "سطح انرژی"
                                    )
                                    EditorialNumber(
                                        number = "${recentRecovery.fatigueLevel}/10",
                                        fontSize = 38.sp,
                                        color = if (recentRecovery.fatigueLevel >= 7) AlertRed else CautionYellow,
                                        subLabel = "خستگی تجمعی"
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "برای نمایش وضعیت بدنی امروز، چک‌این روزانه را ثبت کنید.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSlate,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    IceOutlineButton(
                                        text = "ثبت چک‌این خواب و ریکاوری",
                                        onClick = onOpenCheckInDialog,
                                        modifier = Modifier.height(42.dp),
                                        testTag = "btn_log_checkin"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Honest Pain & Safety Note
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Obsidian800.copy(alpha = 0.7f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = IceBlueLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "عدم ثبت علائم درد شدید مفصلی؛ سیستم در شرایط بارگذاری نرمال قرار دارد.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FrostWhite,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // ---------------------------------------------------------------------
            // SCENE 05 — TRAINING PREVIEW & REAL HABITS
            // ---------------------------------------------------------------------
            item(key = "scene_05_preview") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    GlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Obsidian900.copy(alpha = 0.88f),
                        borderColor = GlassCardBorderSubtle,
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "عادت‌ها و آمادگی بنیادین روز",
                                style = MaterialTheme.typography.labelMedium,
                                color = IceBlueLight,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            val habits = listOf(
                                "sleep_8h" to "خواب کافی (حداقل ۷ تا ۸ ساعت)",
                                "hydration_2l" to "نوشیدن ۲.۵ لیتر آب",
                                "mobility_10m" to "۱۰ دقیقه تحرک‌پذیری مچ و شانه",
                                "protein_intake" to "تغذیه متوازن و مصرف پروتئین کافی"
                            )

                            habits.forEach { (hKey, hTitle) ->
                                val isDone = todayHabits.contains(hKey)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isDone) IceBlueContainer.copy(alpha = 0.4f) else Obsidian800)
                                        .clickable { onToggleHabit(hKey) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = hTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDone) IceBlueLight else FrostWhite,
                                        fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    Icon(
                                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isDone) IceBluePrimary else DarkSlate,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (latestPR != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(IceBlueContainer.copy(alpha = 0.5f))
                                        .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = IceBluePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "آخرین رکورد ثبت‌شده: ${latestPR.title}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = IceBlueLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "رکورد: ${latestPR.value} ${latestPR.unit} (افزایش توانایی واقعی)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FrostWhite,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ---------------------------------------------------------------------
            // SCENE 06 — ACTION & TRANSITION CTA
            // ---------------------------------------------------------------------
            item(key = "scene_06_action") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    GlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Obsidian900.copy(alpha = 0.94f),
                        borderColor = GlassCardBorder,
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "آماده اجرای جلسه تمرینی؟",
                                style = MaterialTheme.typography.titleMedium,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "ثبت سریع ست‌ها، نظارت بر استراحت و تطبیق هوشمند بار تمرینی بدون معطلی",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            if (todaySession != null) {
                                IceButton(
                                    text = "ورود به محیط اجرای تمرین (HUD)",
                                    onClick = { onStartWorkout(todaySession.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "btn_launch_workout_hud"
                                )
                            } else {
                                IceOutlineButton(
                                    text = "انتخاب و ساخت جلسه از کتابخانه برنامه",
                                    onClick = { onNavigateToSection("programs") },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "btn_pick_custom_session"
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Text(
                                    text = "کتابخانه حرکات",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IceBlueLight,
                                    modifier = Modifier.clickable { onNavigateToSection("exercises") }
                                )
                                Text(
                                    text = "مسیر مهارت‌ها",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IceBlueLight,
                                    modifier = Modifier.clickable { onNavigateToSection("skills") }
                                )
                                Text(
                                    text = "نمودار پیشرفت",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IceBlueLight,
                                    modifier = Modifier.clickable { onNavigateToSection("progress") }
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TOP STICKY BAR: Profile Switcher & Scene Indicator
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 34.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile switcher pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Obsidian950.copy(alpha = 0.75f))
                    .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(24.dp))
                    .clickable(onClick = onOpenProfileSwitcher)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("dashboard_profile_switcher")
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(IceBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "پروفایل",
                        tint = IceBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = activeProfile.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            // Live Scene Indicator
            SceneIndicator(
                currentSceneIndex = currentSceneIndex,
                totalScenes = sceneTitlesFa.size,
                sceneTitlesFa = sceneTitlesFa,
                onSceneClick = { targetIndex ->
                    coroutineScope.launch {
                        listState.animateScrollToItem(targetIndex)
                    }
                }
            )
        }
    }

    if (showBrandPhilosophyDialog) {
        AlertDialog(
            onDismissRequest = { showBrandPhilosophyDialog = false },
            title = {
                Text(
                    text = "فلسفه و نمادشناسی کالیستن",
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
                    Text(
                        text = "«قدرت در حجم نیست؛ در تراکم، کنترل و توانایی نهفته است.»",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IceBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "۱. معنای کالیستن (Calisten):",
                        style = MaterialTheme.typography.labelLarge,
                        color = FrostWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ریشه در واژگان یونانی Kalos (زیبایی و تعادل) و Sthenos (قدرت و استواری) دارد. در این سنت، زیبایی حرکت مستقیماً از تسلط بر وزن بدن سرچشمه می‌گیرد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "۲. طبیعت کوهستان و صخره‌های گرانیتی:",
                        style = MaterialTheme.typography.labelLarge,
                        color = FrostWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "صخره‌های استوار و قله‌های مه‌آلود نماد استقامت بی‌صدا، تعادل پایدار و سازگاری با نیروهای طبیعی است. در فلسفه کالیستن، تمرین پیوندی با استواری کوه‌ها و روانی آبشارهاست؛ غلبه بر نیروی جاذبه زمین با انضباط درونی.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "۳. فلسفه بنیادین برند:",
                        style = MaterialTheme.typography.labelLarge,
                        color = FrostWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "تمرینات کالیستن بر توسعه مفاصل پایدار، تاندون‌های انعطاف‌پذیر و توانایی حرکت آزادانه در فضا استوار است؛ سیستمی خودکفا و متکی بر اراده و انضباط فردی.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                IceButton(
                    text = "متوجه شدم",
                    onClick = { showBrandPhilosophyDialog = false }
                )
            },
            containerColor = Obsidian900
        )
    }
}
