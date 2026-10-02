package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GlobalTimerDialog
import com.example.ui.components.FloatingGlassMenu
import com.example.ui.parallax.LocalReduceMotion
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.exercises.ExerciseLibraryScreen
import com.example.ui.screens.nutrition.NutritionScreen
import com.example.ui.screens.profiles.ProfilesScreen
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.screens.programs.ProgramsScreen
import com.example.ui.screens.recovery.RecoveryScreen
import com.example.ui.screens.science.ScienceScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.skills.SkillsScreen
import com.example.ui.screens.welcome.WelcomeScreen
import com.example.ui.screens.workout.WorkoutExecutionScreen
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.KalistenTheme
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.Obsidian800
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950
import com.example.viewmodel.KalistenViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KalistenTheme {
                KalistenApp()
            }
        }
    }
}

@Composable
fun KalistenApp(viewModel: KalistenViewModel = viewModel()) {
    val coroutineScope = rememberCoroutineScope()

    // State collections
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val activeProgram by viewModel.activeProgram.collectAsStateWithLifecycle()
    val programVersions by viewModel.programVersions.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val inProgressSession by viewModel.inProgressSession.collectAsStateWithLifecycle()
    val currentSessionExercises by viewModel.currentSessionExercises.collectAsStateWithLifecycle()
    val currentSessionSetLogs by viewModel.currentSessionSetLogs.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val allSkills by viewModel.allSkills.collectAsStateWithLifecycle()
    val skillProgressList by viewModel.skillProgressList.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val prs by viewModel.prs.collectAsStateWithLifecycle()
    val recoveryEntries by viewModel.recoveryEntries.collectAsStateWithLifecycle()
    val painEntries by viewModel.painEntries.collectAsStateWithLifecycle()
    val nutritionEntries by viewModel.nutritionEntries.collectAsStateWithLifecycle()
    val scienceSources by viewModel.scienceSources.collectAsStateWithLifecycle()
    val sciencePackVersion by viewModel.sciencePackVersion.collectAsStateWithLifecycle()
    val reduceMotionStr by viewModel.reduceMotion.collectAsStateWithLifecycle()
    val userEquipmentProfile by viewModel.userEquipmentProfile.collectAsStateWithLifecycle()
    val latestPrEarned by viewModel.latestPrEarned.collectAsStateWithLifecycle()
    val todayHabits by viewModel.todayHabits.collectAsStateWithLifecycle()

    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val timerSecondsRemaining by viewModel.timerSecondsRemaining.collectAsStateWithLifecycle()
    val timerTotalSeconds by viewModel.timerTotalSeconds.collectAsStateWithLifecycle()
    val isStopwatchRunning by viewModel.isStopwatchRunning.collectAsStateWithLifecycle()
    val stopwatchSeconds by viewModel.stopwatchSeconds.collectAsStateWithLifecycle()

    // Screen State Stack
    val backstack = remember { mutableStateListOf("dashboard") }
    val currentScreen = backstack.lastOrNull() ?: "dashboard"

    fun navigateTo(screen: String) {
        if (screen == "dashboard") {
            backstack.clear()
            backstack.add("dashboard")
        } else {
            backstack.add(screen)
        }
    }

    fun navigateBack() {
        if (backstack.size > 1) {
            backstack.removeAt(backstack.lastIndex)
        }
    }

    // Global Floating Timer Dialog visibility
    var showGlobalTimerDialog by remember { mutableStateOf(false) }

    // If no active profile, show Welcome / Onboarding Screen
    if (activeProfile == null) {
        WelcomeScreen(
            profiles = allProfiles,
            onSelectProfile = { viewModel.switchProfile(it) },
            onCreateProfile = { name, age, height, weight, gender, waist, act, exp, goals, eq, days, assess ->
                viewModel.createProfile(name, age, height, weight, gender, waist, act, exp, goals, eq, days, assess)
            },
            onRestoreBackup = { json ->
                val validation = viewModel.validateBackup(json)
                if (validation.isValid) {
                    coroutineScope.launch {
                        viewModel.restoreBackup(json)
                    }
                    true
                } else {
                    false
                }
            },
            onStartDemoMode = {
                viewModel.createProfile(
                    name = "کاربر نمایشی (Demo)",
                    age = 24,
                    heightCm = 178f,
                    weightKg = 74f,
                    gender = "MALE",
                    waistCm = 80f,
                    activityLevel = "MODERATE",
                    experienceLevel = "INTERMEDIATE",
                    goals = listOf("STRENGTH", "CALISTHENICS_SKILLS"),
                    equipment = listOf("pullup_bar", "parallettes"),
                    trainingDays = 3,
                    assessment = null,
                    autoGenerateProgram = true
                )
            }
        )
        return
    }

    val profile = activeProfile!!

    // Handle Back Button in sub-screens
    BackHandler(enabled = backstack.size > 1) {
        navigateBack()
    }

    // Main App Shell
    val showBottomBar = currentScreen != "workout"
    val isReduceMotion = reduceMotionStr == "true"

    CompositionLocalProvider(LocalReduceMotion provides isReduceMotion) {
        Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Obsidian950,
        bottomBar = {
            if (showBottomBar) {
                FloatingGlassMenu(
                    currentScreen = currentScreen,
                    onNavigate = { navigateTo(it) },
                    onOpenTimer = { showGlobalTimerDialog = true }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "dashboard" -> {
                    val todaySession = sessions.firstOrNull { it.status == "IN_PROGRESS" }
                        ?: sessions.firstOrNull()
                    val latestPr = prs.firstOrNull()
                    val recentRecovery = recoveryEntries.firstOrNull()

                    DashboardScreen(
                        activeProfile = profile,
                        activeProgram = activeProgram,
                        todaySession = todaySession,
                        recentRecovery = recentRecovery,
                        latestPR = latestPr,
                        todayHabits = todayHabits,
                        reduceMotion = reduceMotionStr == "true",
                        onToggleHabit = { viewModel.toggleHabit(it) },
                        onStartWorkout = { sessionId ->
                            viewModel.startSession(sessionId)
                            navigateTo("workout")
                        },
                        onNavigateToSection = { section ->
                            when (section) {
                                "timer" -> showGlobalTimerDialog = true
                                else -> navigateTo(section)
                            }
                        },
                        onOpenCheckInDialog = { navigateTo("recovery") },
                        onOpenProfileSwitcher = { navigateTo("profiles") }
                    )
                }

                "workout" -> {
                    val activeSession = inProgressSession
                        ?: sessions.firstOrNull { it.status == "IN_PROGRESS" }
                        ?: sessions.firstOrNull()

                    if (activeSession != null) {
                        WorkoutExecutionScreen(
                            session = activeSession,
                            workoutExercises = currentSessionExercises,
                            allExercises = allExercises,
                            setLogs = currentSessionSetLogs,
                            latestPrEarned = latestPrEarned,
                            timerSecondsRemaining = timerSecondsRemaining,
                            timerTotalSeconds = timerTotalSeconds,
                            isTimerRunning = isTimerRunning,
                            onStartRestTimer = { viewModel.startRestTimer(it) },
                            onPauseTimer = { viewModel.pauseTimer() },
                            onResumeTimer = { viewModel.resumeTimer() },
                            onResetTimer = { viewModel.resetTimer() },
                            onLogSet = { weId, sessId, exId, exName, setNum, reps, hold, weight, rpe, rir, st, painSev, painLoc, notes ->
                                viewModel.logSet(
                                    weId, sessId, exId, exName, setNum, reps, hold, weight, rpe, rir, st, painSev, painLoc, notes
                                )
                            },
                            onDismissPrAlert = { viewModel.dismissLatestPr() },
                            onFinishWorkout = { overallRpe, notes ->
                                viewModel.finishWorkout(activeSession.id, overallRpe, notes)
                                navigateBack()
                            },
                            onAdjustDifficulty = { weId, inc ->
                                viewModel.adjustExerciseDifficulty(weId, inc)
                            },
                            onBack = { navigateBack() }
                        )
                    } else {
                        // Empty workout state
                        ProgramsScreen(
                            activeProgram = activeProgram,
                            programVersions = programVersions,
                            sessions = sessions,
                            allExercises = allExercises,
                            onCreateCustomProgram = { title, desc, lvl, days, exIds ->
                                viewModel.buildCustomProgram(title, desc, lvl, days, exIds)
                            },
                            onRollbackVersion = { viewModel.rollbackProgram(it) },
                            onStartSession = {
                                viewModel.startSession(it)
                                navigateTo("workout")
                            },
                            onBack = { navigateBack() }
                        )
                    }
                }

                "programs" -> {
                    ProgramsScreen(
                        activeProgram = activeProgram,
                        programVersions = programVersions,
                        sessions = sessions,
                        allExercises = allExercises,
                        onCreateCustomProgram = { title, desc, lvl, days, exIds ->
                            viewModel.buildCustomProgram(title, desc, lvl, days, exIds)
                        },
                        onRollbackVersion = { viewModel.rollbackProgram(it) },
                        onStartSession = {
                            viewModel.startSession(it)
                            navigateTo("workout")
                        },
                        onBack = { navigateTo("dashboard") }
                    )
                }

                "exercises" -> {
                    ExerciseLibraryScreen(
                        exercises = allExercises,
                        onCreateCustomExercise = { pName, eName, cat, mus, pat, diff, startPos, inst, safety ->
                            viewModel.createCustomExercise(pName, eName, cat, mus, pat, diff, startPos, inst, safety)
                        },
                        onBack = { navigateTo("dashboard") }
                    )
                }

                "skills" -> {
                    SkillsScreen(
                        skills = allSkills,
                        skillProgressList = skillProgressList,
                        onUpdateSkillProgress = { skillId, step, hold, reps, q ->
                            viewModel.updateSkillStep(skillId, step, hold, reps, q)
                        },
                        onIntegrateSkill = { skillId ->
                            viewModel.integrateSkillIntoWorkout(skillId)
                        },
                        onBack = { navigateTo("dashboard") }
                    )
                }

                "progress" -> {
                    ProgressScreen(
                        metrics = metrics,
                        prs = prs,
                        recoveryEntries = recoveryEntries,
                        setLogs = currentSessionSetLogs,
                        onRecordMetric = { type, value, unit, notes ->
                            viewModel.recordMetric(type, value, unit, notes)
                        },
                        onBack = { navigateTo("dashboard") }
                    )
                }

                "recovery" -> {
                    RecoveryScreen(
                        recoveryEntries = recoveryEntries,
                        painEntries = painEntries,
                        onLogRecovery = { sH, sQ, eL, fL, mS, stL, notes ->
                            viewModel.logDailyRecovery(sH, sQ, eL, fL, mS, stL, notes)
                        },
                        onLogPain = { loc, sev, tim, duringW, afterW, nextD, effForm, notes ->
                            viewModel.logPainEntry(loc, sev, tim, null, duringW, afterW, nextD, effForm, notes)
                        },
                        onBack = { navigateBack() }
                    )
                }

                "nutrition" -> {
                    NutritionScreen(
                        profile = profile,
                        entries = nutritionEntries,
                        onLogNutrition = { food, meal, cal, prot, carb, fat, water, notes ->
                            viewModel.logNutrition(food, meal, cal, prot, carb, fat, water, notes)
                        },
                        onDeleteNutrition = { viewModel.deleteNutrition(it) },
                        onBack = { navigateBack() }
                    )
                }

                "science" -> {
                    ScienceScreen(
                        sources = scienceSources,
                        currentPackVersion = sciencePackVersion ?: "v1.0",
                        onUpdateSciencePack = { viewModel.updateSciencePack() },
                        onBack = { navigateBack() }
                    )
                }

                "profiles" -> {
                    ProfilesScreen(
                        profiles = allProfiles,
                        activeProfileId = profile.id,
                        onSwitchProfile = {
                            viewModel.switchProfile(it)
                            navigateTo("dashboard")
                        },
                        onCreateProfile = { name, age, h, w, g, waist, act, exp, goals, eq, days ->
                            viewModel.createProfile(name, age, h, w, g, waist, act, exp, goals, eq, days)
                        },
                        onDeleteProfile = { viewModel.deleteProfile(it) },
                        onBack = { navigateBack() }
                    )
                }

                "settings" -> {
                    SettingsScreen(
                        equipmentProfile = userEquipmentProfile,
                        onSaveEquipmentProfile = { viewModel.updateUserEquipmentProfile(it) },
                        onRegenerateSmartProgram = { viewModel.generateSmartProgram() },
                        reduceMotionEnabled = reduceMotionStr == "true",
                        onToggleReduceMotion = { viewModel.toggleReduceMotion(it) },
                        onExportBackup = { viewModel.exportBackupJson() },
                        onValidateBackup = { viewModel.validateBackup(it) },
                        onRestoreBackup = { viewModel.restoreBackup(it) },
                        onBack = { navigateBack() }
                    )
                }
            }
        }
    }
}

    // Global Floating Timer & Stopwatch Dialog
    GlobalTimerDialog(
        isOpen = showGlobalTimerDialog,
        timerSecondsRemaining = timerSecondsRemaining,
        timerTotalSeconds = timerTotalSeconds,
        isTimerRunning = isTimerRunning,
        stopwatchSeconds = stopwatchSeconds,
        isStopwatchRunning = isStopwatchRunning,
        onStartRestTimer = { viewModel.startRestTimer(it) },
        onPauseTimer = { viewModel.pauseTimer() },
        onResumeTimer = { viewModel.resumeTimer() },
        onResetTimer = { viewModel.resetTimer() },
        onStartStopwatch = { viewModel.startStopwatch() },
        onPauseStopwatch = { viewModel.pauseStopwatch() },
        onResetStopwatch = { viewModel.resetStopwatch() },
        onDismiss = { showGlobalTimerDialog = false }
    )
}
