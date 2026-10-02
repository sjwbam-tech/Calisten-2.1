package com.example.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.PREntity
import com.example.data.local.entity.SetLogEntity
import com.example.data.local.entity.WorkoutExerciseEntity
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.ui.components.IceButton
import com.example.ui.components.IceOutlineButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.PersianTopBar
import com.example.ui.parallax.LocalReduceMotion
import com.example.ui.parallax.ParallaxConfig
import com.example.ui.parallax.parallaxCardDepth
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.GlacierTeal
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

@Composable
fun WorkoutExecutionScreen(
    session: WorkoutSessionEntity,
    workoutExercises: List<WorkoutExerciseEntity>,
    allExercises: List<ExerciseEntity>,
    setLogs: List<SetLogEntity>,
    latestPrEarned: PREntity?,
    timerSecondsRemaining: Int,
    timerTotalSeconds: Int,
    isTimerRunning: Boolean,
    onStartRestTimer: (Int) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onLogSet: (
        workoutExerciseId: String,
        sessionId: String,
        exerciseId: String,
        exerciseName: String,
        setNumber: Int,
        reps: Int,
        holdSeconds: Int,
        addedWeightKg: Float,
        rpe: Int,
        rir: Int,
        status: String,
        painSeverity: Int,
        painLocation: String,
        notes: String
    ) -> Unit,
    onDismissPrAlert: () -> Unit,
    onFinishWorkout: (overallRpe: Int, notes: String) -> Unit,
    onAdjustDifficulty: ((workoutExerciseId: String, increase: Boolean) -> Unit)? = null,
    onBack: () -> Unit
) {
    var selectedExerciseIndex by remember { mutableIntStateOf(0) }
    var showFinishDialog by remember { mutableStateOf(false) }

    val currentWorkoutExercise = workoutExercises.getOrNull(selectedExerciseIndex)
    val exerciseDetail = allExercises.firstOrNull { it.id == currentWorkoutExercise?.exerciseId }

    // Existing logs for current exercise
    val currentExerciseLogs = setLogs.filter { it.workoutExerciseId == currentWorkoutExercise?.id }
    val nextSetNumber = currentExerciseLogs.size + 1

    // Input States for the set being logged
    var currentReps by remember(selectedExerciseIndex, nextSetNumber) {
        mutableIntStateOf(currentWorkoutExercise?.targetReps ?: 8)
    }
    var currentHoldSec by remember(selectedExerciseIndex, nextSetNumber) {
        mutableIntStateOf(currentWorkoutExercise?.targetHoldSec ?: 0)
    }
    var currentWeightKg by remember(selectedExerciseIndex, nextSetNumber) {
        mutableFloatStateOf(0f)
    }
    var currentRpe by remember(selectedExerciseIndex, nextSetNumber) {
        mutableIntStateOf(currentWorkoutExercise?.targetRpe ?: 7)
    }
    var currentRir by remember(selectedExerciseIndex, nextSetNumber) {
        mutableIntStateOf(2)
    }
    var setStatus by remember(selectedExerciseIndex, nextSetNumber) {
        mutableStateOf("COMPLETED") // COMPLETED, PARTIAL, PAIN_STOP
    }
    var painSeverity by remember(selectedExerciseIndex, nextSetNumber) {
        mutableIntStateOf(0)
    }
    var painLocation by remember(selectedExerciseIndex, nextSetNumber) {
        mutableStateOf("کتف")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            PersianTopBar(
                title = session.title,
                subtitle = "حرکت ${selectedExerciseIndex + 1} از ${workoutExercises.size}",
                onBackClick = onBack,
                actions = {
                    IceButton(
                        text = "اتمام تمرین",
                        onClick = { showFinishDialog = true },
                        modifier = Modifier.height(38.dp),
                        testTag = "finish_workout_top_btn"
                    )
                }
            )

            // PR Celebration Banner if user just broke a real PR!
            AnimatedVisibility(visible = latestPrEarned != null) {
                if (latestPrEarned != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(IceBlueContainer)
                            .border(1.dp, IceBluePrimary, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = IceBluePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "رکورد شخصی جدید! (PR)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FrostWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${latestPrEarned.title} با مقدار ${latestPrEarned.value.toInt()} ${latestPrEarned.unit}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = IceBlueLight
                                    )
                                }
                            }
                            IconButton(onClick = onDismissPrAlert) {
                                Icon(Icons.Default.Check, contentDescription = "تایید", tint = FrostWhite)
                            }
                        }
                    }
                }
            }

            val workoutScrollState = rememberScrollState()
            val reduceMotion = LocalReduceMotion.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(workoutScrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Exercise Switcher Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    workoutExercises.forEachIndexed { index, we ->
                        val ex = allExercises.firstOrNull { it.id == we.exerciseId }
                        val isDone = setLogs.count { it.workoutExerciseId == we.id } >= we.targetSets
                        val isSelected = index == selectedExerciseIndex

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) IceBluePrimary else if (isDone) SafeGreen.copy(alpha = 0.2f) else Obsidian800)
                                .border(1.dp, if (isSelected) IceBlueLight else GlassCardBorderSubtle, RoundedCornerShape(10.dp))
                                .clickable { selectedExerciseIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "حرکت ${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Obsidian950 else if (isDone) SafeGreen else FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active Exercise HUD Card with subtle depth
                LuxuryCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .parallaxCardDepth(workoutScrollState, ParallaxConfig.WorkoutSubtle, reduceMotion),
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
                                    text = exerciseDetail?.persianName ?: "حرکت کالیستنیکس",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = FrostWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = exerciseDetail?.englishName ?: "",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = IceBlueLight
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Obsidian700)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "هدف: ${currentWorkoutExercise?.targetSets} ست × " +
                                            if ((currentWorkoutExercise?.targetHoldSec ?: 0) > 0)
                                                "${currentWorkoutExercise?.targetHoldSec} ثانیه"
                                            else
                                                "${currentWorkoutExercise?.targetReps} تکرار",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FrostWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (!exerciseDetail?.startingPosition.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "نحوه استقرار: ${exerciseDetail?.startingPosition}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                        }

                        // Tempo & Movement Pattern
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Obsidian800)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "تمپو: ${exerciseDetail?.tempo ?: "2-0-1-0"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IceBlueLight
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Obsidian800)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "الگو: ${exerciseDetail?.movementPattern ?: "اصلی"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate
                                )
                            }
                        }

                        // Adaptive Equipment & Weight Adaptation HUD Note
                        val exerciseNotes = currentWorkoutExercise?.notes
                        if (!exerciseNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IceBlueContainer.copy(alpha = 0.25f))
                                    .border(1.dp, GlacierTeal.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .testTag("exercise_equipment_adaptation_hud")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = "تجهیزات تمرین",
                                        tint = GlacierTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = exerciseNotes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FrostWhite,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        if (onAdjustDifficulty != null && currentWorkoutExercise != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IceOutlineButton(
                                    text = "تعدیل سطح (ساده‌تر)",
                                    onClick = { onAdjustDifficulty(currentWorkoutExercise.id, false) },
                                    modifier = Modifier.weight(1f)
                                )
                                IceOutlineButton(
                                    text = "ارتقای سطح (دشوارتر)",
                                    onClick = { onAdjustDifficulty(currentWorkoutExercise.id, true) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Next Exercise Preview
                val nextExerciseWE = workoutExercises.getOrNull(selectedExerciseIndex + 1)
                val nextExerciseDetail = allExercises.firstOrNull { it.id == nextExerciseWE?.exerciseId }
                if (nextExerciseDetail != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Obsidian900)
                            .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "حرکت بعدی: ${nextExerciseDetail.persianName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate
                            )
                            Text(
                                text = "${nextExerciseWE?.targetSets} ست",
                                style = MaterialTheme.typography.labelSmall,
                                color = IceBlueLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Rest Timer Bar
                LuxuryCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Obsidian900
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = IceBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "استراحت: $timerSecondsRemaining ثانیه",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isTimerRunning) IceBlueLight else FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isTimerRunning) {
                                IconButton(onClick = onPauseTimer, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Pause, contentDescription = "مکث", tint = FrostWhite)
                                }
                            } else {
                                IconButton(onClick = onResumeTimer, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "شروع", tint = IceBluePrimary)
                                }
                            }
                            IconButton(onClick = onResetTimer, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "ریست", tint = MutedSlate)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            // Quick timer preset chips
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(60, 90, 120).forEach { sec ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Obsidian800)
                                            .clickable { onStartRestTimer(sec) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "${sec}s", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Set Logging Form
                LuxuryCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ثبت ست شماره $nextSetNumber",
                            style = MaterialTheme.typography.titleMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Reps or Hold Seconds Counter
                        if ((currentWorkoutExercise?.targetHoldSec ?: 0) > 0) {
                            Text(text = "زمان مکث ایزومتریک (ثانیه):", style = MaterialTheme.typography.labelMedium, color = MutedSlate)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { if (currentHoldSec > 0) currentHoldSec -= 5 },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Obsidian800)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "کاهش", tint = FrostWhite)
                                }
                                Text(
                                    text = "$currentHoldSec ثانیه",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = IceBlueLight,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { currentHoldSec += 5 },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Obsidian800)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "افزایش", tint = FrostWhite)
                                }
                            }
                        } else {
                            Text(text = "تعداد تکرار اجرا شده:", style = MaterialTheme.typography.labelMedium, color = MutedSlate)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { if (currentReps > 0) currentReps -= 1 },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Obsidian800)
                                        .testTag("decrement_reps_btn")
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "کاهش", tint = FrostWhite)
                                }
                                Text(
                                    text = "$currentReps",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = IceBlueLight,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { currentReps += 1 },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Obsidian800)
                                        .testTag("increment_reps_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "افزایش", tint = FrostWhite)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Added weight (kg) if weighted calisthenics
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "وزن اضافه (کیلوگرم):", style = MaterialTheme.typography.labelMedium, color = MutedSlate)
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedTextField(
                                value = if (currentWeightKg == 0f) "" else currentWeightKg.toString(),
                                onValueChange = { currentWeightKg = it.toFloatOrNull() ?: 0f },
                                placeholder = { Text("۰", color = MutedSlate) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(50.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = FrostWhite,
                                    unfocusedTextColor = FrostWhite,
                                    focusedBorderColor = IceBluePrimary,
                                    unfocusedBorderColor = Obsidian700
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // RPE Slider (1-10)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "شدت تلاش درک‌شده (RPE):", style = MaterialTheme.typography.labelMedium, color = MutedSlate)
                            Text(text = "RPE $currentRpe / 10", style = MaterialTheme.typography.labelMedium, color = IceBlueLight, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = currentRpe.toFloat(),
                            onValueChange = { currentRpe = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = IceBluePrimary,
                                activeTrackColor = IceBluePrimary,
                                inactiveTrackColor = Obsidian700
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // RIR Slider (0-5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "تکرارهای باقی‌مانده در مخزن (RIR):", style = MaterialTheme.typography.labelMedium, color = MutedSlate)
                            Text(text = "$currentRir تکرار ذخیره (RIR)", style = MaterialTheme.typography.labelMedium, color = IceBlueLight, fontWeight = FontWeight.SemiBold)
                        }
                        Slider(
                            value = currentRir.toFloat(),
                            onValueChange = { currentRir = it.toInt() },
                            valueRange = 0f..5f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = IceBlueLight,
                                activeTrackColor = IceBlueLight,
                                inactiveTrackColor = Obsidian700
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Options
                        Text(text = "وضعیت تکمیل ست:", style = MaterialTheme.typography.labelMedium, color = MutedSlate)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "COMPLETED" to "کامل",
                                "PARTIAL" to "ناقص",
                                "SKIPPED" to "رد کردن",
                                "PAIN_STOP" to "توقف با درد"
                            ).forEach { (code, label) ->
                                FilterChip(
                                    selected = setStatus == code,
                                    onClick = { setStatus = code },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (code == "PAIN_STOP") AlertRed else IceBluePrimary,
                                        selectedLabelColor = Obsidian950
                                    )
                                )
                            }
                        }

                        // If Pain stop chosen, show location selector
                        if (setStatus == "PAIN_STOP") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "محل احساس درد یا فشار نامناسب:", style = MaterialTheme.typography.labelSmall, color = CautionYellow)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("کتف", "مچ دست", "آرنج", "کمر", "زانو").forEach { loc ->
                                    FilterChip(
                                        selected = painLocation == loc,
                                        onClick = { painLocation = loc },
                                        label = { Text(loc) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Set Button
                        IceButton(
                            text = "ثبت ست $nextSetNumber و شروع استراحت",
                            onClick = {
                                if (currentWorkoutExercise != null) {
                                    onLogSet(
                                        currentWorkoutExercise.id,
                                        session.id,
                                        currentWorkoutExercise.exerciseId,
                                        exerciseDetail?.persianName ?: "حرکت",
                                        nextSetNumber,
                                        currentReps,
                                        currentHoldSec,
                                        currentWeightKg,
                                        currentRpe,
                                        currentRir,
                                        setStatus,
                                        if (setStatus == "PAIN_STOP") 5 else 0,
                                        if (setStatus == "PAIN_STOP") painLocation else "",
                                        ""
                                    )
                                    // Automatically kick off rest timer
                                    onStartRestTimer(currentWorkoutExercise.restSec)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "submit_set_log_btn"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List of sets already logged for this exercise
                if (currentExerciseLogs.isNotEmpty()) {
                    Text(
                        text = "ست‌های ثبت‌شده در این جلسه:",
                        style = MaterialTheme.typography.titleMedium,
                        color = FrostWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    currentExerciseLogs.forEach { log ->
                        LuxuryCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            backgroundColor = Obsidian900
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (log.status == "PAIN_STOP") Icons.Default.Warning else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (log.status == "PAIN_STOP") AlertRed else SafeGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ست ${log.setNumber}: " +
                                                if (log.holdSeconds > 0) "${log.holdSeconds} ثانیه" else "${log.reps} تکرار",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FrostWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (log.addedWeightKg > 0) {
                                        Text(
                                            text = " (+${log.addedWeightKg}kg)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = IceBlueLight
                                        )
                                    }
                                }

                                Text(
                                    text = "RPE: ${log.rpe}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Finish Workout Confirmation Dialog
    if (showFinishDialog) {
        var overallRpeInput by remember { mutableIntStateOf(7) }
        var workoutNotes by remember { mutableStateOf("") }

        val totalSetsCompleted = setLogs.count { it.status == "COMPLETED" }
        val hadAnyPain = setLogs.any { it.status == "PAIN_STOP" }

        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = {
                Text(
                    text = "پایان و ذخیره جلسه تمرین",
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
                        text = "خلاصه عملکرد این جلسه:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedSlate
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• تعداد ست‌های کامل: $totalSetsCompleted ست",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FrostWhite
                    )
                    if (hadAnyPain) {
                        Text(
                            text = "• هشدار درد ثبت‌شده: در تمرینات آینده بارگذاری مفاصل با احتیاط انجام خواهد شد.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CautionYellow
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "شدت کل جلسه (RPE کلی: $overallRpeInput):",
                        style = MaterialTheme.typography.labelMedium,
                        color = FrostWhite
                    )
                    Slider(
                        value = overallRpeInput.toFloat(),
                        onValueChange = { overallRpeInput = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = IceBluePrimary,
                            activeTrackColor = IceBluePrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = workoutNotes,
                        onValueChange = { workoutNotes = it },
                        label = { Text("یادداشت یا احساس بعد تمرین (اختیاری)") },
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
                    text = "ذخیره نهایی و بازگشت",
                    onClick = {
                        onFinishWorkout(overallRpeInput, workoutNotes)
                        showFinishDialog = false
                        onBack()
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    testTag = "confirm_finish_workout_btn"
                )
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("ادامه تمرین", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }
}
