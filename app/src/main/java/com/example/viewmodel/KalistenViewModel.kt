package com.example.viewmodel

import android.app.Application
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AppSettingEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.DailyCheckInEntity
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.MetricEntity
import com.example.data.local.entity.NutritionEntryEntity
import com.example.data.local.entity.PREntity
import com.example.data.local.entity.PainEntryEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ProgramEntity
import com.example.data.local.entity.ProgramVersionEntity
import com.example.data.local.entity.RecoveryEntryEntity
import com.example.data.local.entity.ScienceSourceEntity
import com.example.data.local.entity.SetLogEntity
import com.example.data.local.entity.SkillEntity
import com.example.data.local.entity.SkillProgressEntity
import com.example.data.local.entity.WorkoutExerciseEntity
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.data.repository.KalistenRepository
import com.example.domain.engine.BackupValidationResult
import com.example.domain.engine.PlateauAnalysisResult
import com.example.domain.engine.PlateauEngine
import com.example.domain.engine.ProgressionDecision
import com.example.domain.engine.ProgressionEngine
import com.example.domain.equipment.UserEquipmentProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class KalistenViewModel(application: Application) : AndroidViewModel(application) {

    val repository = KalistenRepository(application)
    private val vibrator = application.getSystemService(Vibrator::class.java)

    // All profiles
    val allProfiles: StateFlow<List<ProfileEntity>> = repository.profileDao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Profile ID
    val activeProfileId: StateFlow<String?> = repository.getActiveProfileId()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Active Profile
    private val _activeProfile = MutableStateFlow<ProfileEntity?>(null)
    val activeProfile: StateFlow<ProfileEntity?> = _activeProfile.asStateFlow()

    // Active Assessment
    private val _activeAssessment = MutableStateFlow<AssessmentEntity?>(null)
    val activeAssessment: StateFlow<AssessmentEntity?> = _activeAssessment.asStateFlow()

    // Active Program
    private val _activeProgram = MutableStateFlow<ProgramEntity?>(null)
    val activeProgram: StateFlow<ProgramEntity?> = _activeProgram.asStateFlow()

    // Program Versions
    private val _programVersions = MutableStateFlow<List<ProgramVersionEntity>>(emptyList())
    val programVersions: StateFlow<List<ProgramVersionEntity>> = _programVersions.asStateFlow()

    // Today's / Recent Sessions
    private val _sessions = MutableStateFlow<List<WorkoutSessionEntity>>(emptyList())
    val sessions: StateFlow<List<WorkoutSessionEntity>> = _sessions.asStateFlow()

    // Active Workout in progress
    private val _inProgressSession = MutableStateFlow<WorkoutSessionEntity?>(null)
    val inProgressSession: StateFlow<WorkoutSessionEntity?> = _inProgressSession.asStateFlow()

    // Exercises for in-progress session
    private val _currentSessionExercises = MutableStateFlow<List<WorkoutExerciseEntity>>(emptyList())
    val currentSessionExercises: StateFlow<List<WorkoutExerciseEntity>> = _currentSessionExercises.asStateFlow()

    // Set logs for current session
    private val _currentSessionSetLogs = MutableStateFlow<List<SetLogEntity>>(emptyList())
    val currentSessionSetLogs: StateFlow<List<SetLogEntity>> = _currentSessionSetLogs.asStateFlow()

    // Exercise Library
    val allExercises: StateFlow<List<ExerciseEntity>> = repository.exerciseDao.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Skills
    val allSkills: StateFlow<List<SkillEntity>> = repository.skillDao.getAllSkills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _skillProgressList = MutableStateFlow<List<SkillProgressEntity>>(emptyList())
    val skillProgressList: StateFlow<List<SkillProgressEntity>> = _skillProgressList.asStateFlow()

    // Metrics for active profile
    private val _metrics = MutableStateFlow<List<MetricEntity>>(emptyList())
    val metrics: StateFlow<List<MetricEntity>> = _metrics.asStateFlow()

    // PRs for active profile
    private val _prs = MutableStateFlow<List<PREntity>>(emptyList())
    val prs: StateFlow<List<PREntity>> = _prs.asStateFlow()

    // Recovery & Check-in
    private val _recoveryEntries = MutableStateFlow<List<RecoveryEntryEntity>>(emptyList())
    val recoveryEntries: StateFlow<List<RecoveryEntryEntity>> = _recoveryEntries.asStateFlow()

    private val _dailyCheckIns = MutableStateFlow<List<DailyCheckInEntity>>(emptyList())
    val dailyCheckIns: StateFlow<List<DailyCheckInEntity>> = _dailyCheckIns.asStateFlow()

    private val _todayHabits = MutableStateFlow<List<String>>(emptyList())
    val todayHabits: StateFlow<List<String>> = _todayHabits.asStateFlow()

    private val _painEntries = MutableStateFlow<List<PainEntryEntity>>(emptyList())
    val painEntries: StateFlow<List<PainEntryEntity>> = _painEntries.asStateFlow()

    // Nutrition
    private val _nutritionEntries = MutableStateFlow<List<NutritionEntryEntity>>(emptyList())
    val nutritionEntries: StateFlow<List<NutritionEntryEntity>> = _nutritionEntries.asStateFlow()

    // Science
    val scienceSources: StateFlow<List<ScienceSourceEntity>> = repository.scienceDao.getAllSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sciencePackVersion: StateFlow<String?> = repository.appSettingDao.getSetting("science_pack_version")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "v1.0")

    // Reduce Motion Setting
    val reduceMotion: StateFlow<String?> = repository.appSettingDao.getSetting("reduce_motion")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "false")

    // Equipment Profile for active profile
    private val _userEquipmentProfile = MutableStateFlow(UserEquipmentProfile())
    val userEquipmentProfile: StateFlow<UserEquipmentProfile> = _userEquipmentProfile.asStateFlow()

    // Latest real-time PR alert
    private val _latestPrEarned = MutableStateFlow<PREntity?>(null)
    val latestPrEarned: StateFlow<PREntity?> = _latestPrEarned.asStateFlow()

    // Floating Timer & Stopwatch
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerSecondsRemaining = MutableStateFlow(90)
    val timerSecondsRemaining: StateFlow<Int> = _timerSecondsRemaining.asStateFlow()

    private val _timerTotalSeconds = MutableStateFlow(90)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private val _isStopwatchRunning = MutableStateFlow(false)
    val isStopwatchRunning: StateFlow<Boolean> = _isStopwatchRunning.asStateFlow()

    private val _stopwatchSeconds = MutableStateFlow(0)
    val stopwatchSeconds: StateFlow<Int> = _stopwatchSeconds.asStateFlow()

    private var timerJob: Job? = null
    private var stopwatchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureStaticContentInitialized()
        }
        // Observe active profile and bind dependent flows
        viewModelScope.launch {
            activeProfileId.collectLatest { profileId ->
                if (!profileId.isNullOrEmpty()) {
                    launch {
                        repository.profileDao.getProfileById(profileId).collectLatest {
                            _activeProfile.value = it
                        }
                    }
                    launch {
                        repository.assessmentDao.getAssessmentForProfile(profileId).collectLatest {
                            _activeAssessment.value = it
                        }
                    }
                    launch {
                        repository.programDao.getActiveProgramForProfile(profileId).collectLatest { prog ->
                            _activeProgram.value = prog
                            if (prog != null) {
                                repository.programVersionDao.getVersionsForProgram(prog.id).collectLatest {
                                    _programVersions.value = it
                                }
                            } else {
                                _programVersions.value = emptyList()
                            }
                        }
                    }
                    launch {
                        repository.workoutDao.getSessionsForProfile(profileId).collectLatest {
                            _sessions.value = it
                        }
                    }
                    launch {
                        repository.workoutDao.getInProgressSession(profileId).collectLatest { session ->
                            _inProgressSession.value = session
                            if (session != null) {
                                repository.workoutDao.getExercisesForSession(session.id).collectLatest {
                                    _currentSessionExercises.value = it
                                }
                                repository.workoutDao.getSetLogsForSession(session.id).collectLatest {
                                    _currentSessionSetLogs.value = it
                                }
                            } else {
                                _currentSessionExercises.value = emptyList()
                                _currentSessionSetLogs.value = emptyList()
                            }
                        }
                    }
                    launch {
                        repository.skillDao.getProgressForProfile(profileId).collectLatest {
                            _skillProgressList.value = it
                        }
                    }
                    launch {
                        repository.metricDao.getAllMetricsForProfile(profileId).collectLatest {
                            _metrics.value = it
                        }
                    }
                    launch {
                        repository.prDao.getPRsForProfile(profileId).collectLatest {
                            _prs.value = it
                        }
                    }
                    launch {
                        repository.recoveryDao.getRecoveryForProfile(profileId).collectLatest {
                            _recoveryEntries.value = it
                        }
                    }
                    launch {
                        repository.recoveryDao.getDailyCheckIns(profileId).collectLatest { checkIns ->
                            _dailyCheckIns.value = checkIns
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val todayCheckIn = checkIns.firstOrNull { it.date == todayStr }
                            if (todayCheckIn != null && todayCheckIn.habitsCompletedJson.isNotEmpty()) {
                                try {
                                    val arr = org.json.JSONArray(todayCheckIn.habitsCompletedJson)
                                    _todayHabits.value = (0 until arr.length()).map { arr.getString(it) }
                                } catch (_: Exception) {
                                    _todayHabits.value = emptyList()
                                }
                            } else {
                                _todayHabits.value = emptyList()
                            }
                        }
                    }
                    launch {
                        repository.recoveryDao.getPainEntries(profileId).collectLatest {
                            _painEntries.value = it
                        }
                    }
                    launch {
                        repository.nutritionDao.getAllEntriesForProfile(profileId).collectLatest {
                            _nutritionEntries.value = it
                        }
                    }
                    launch {
                        repository.getUserEquipmentProfile(profileId).collectLatest {
                            _userEquipmentProfile.value = it
                        }
                    }
                } else {
                    _activeProfile.value = null
                    _userEquipmentProfile.value = UserEquipmentProfile()
                    _activeAssessment.value = null
                    _activeProgram.value = null
                    _programVersions.value = emptyList()
                    _sessions.value = emptyList()
                    _inProgressSession.value = null
                    _skillProgressList.value = emptyList()
                    _metrics.value = emptyList()
                    _prs.value = emptyList()
                    _recoveryEntries.value = emptyList()
                    _dailyCheckIns.value = emptyList()
                    _painEntries.value = emptyList()
                    _nutritionEntries.value = emptyList()
                    _todayHabits.value = emptyList()
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Profile Actions
    // -------------------------------------------------------------
    fun createProfile(
        name: String,
        age: Int,
        heightCm: Float,
        weightKg: Float,
        gender: String,
        waistCm: Float? = null,
        activityLevel: String = "MODERATE",
        experienceLevel: String = "BEGINNER",
        goals: List<String> = emptyList(),
        equipment: List<String> = emptyList(),
        trainingDays: Int = 3,
        assessment: AssessmentEntity? = null,
        autoGenerateProgram: Boolean = true
    ) {
        viewModelScope.launch {
            val profileId = UUID.randomUUID().toString()
            val profile = ProfileEntity(
                id = profileId,
                name = name,
                age = age,
                heightCm = heightCm,
                weightKg = weightKg,
                gender = gender,
                waistCm = waistCm,
                activityLevel = activityLevel,
                experienceLevel = experienceLevel,
                goalsJson = goals.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]"),
                equipmentJson = equipment.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]"),
                trainingDaysPerWeek = trainingDays
            )
            val assessmentWithId = assessment?.copy(id = UUID.randomUUID().toString(), profileId = profileId)
            repository.createProfile(profile, assessmentWithId)

            if (autoGenerateProgram) {
                repository.generateSmartProgram(profile)
            }
        }
    }

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            repository.setActiveProfileId(profileId)
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            repository.deleteProfile(profileId)
        }
    }

    // -------------------------------------------------------------
    // Workout Execution Actions
    // -------------------------------------------------------------
    fun startSession(sessionId: String) {
        viewModelScope.launch {
            val session = repository.workoutDao.getSessionByIdDirect(sessionId) ?: return@launch
            repository.workoutDao.updateSession(session.copy(status = "IN_PROGRESS"))
        }
    }

    fun logSet(
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
    ) {
        val currentProfile = _activeProfile.value ?: return
        viewModelScope.launch {
            val setLog = SetLogEntity(
                id = UUID.randomUUID().toString(),
                workoutExerciseId = workoutExerciseId,
                sessionId = sessionId,
                profileId = currentProfile.id,
                exerciseId = exerciseId,
                setNumber = setNumber,
                reps = reps,
                holdSeconds = holdSeconds,
                addedWeightKg = addedWeightKg,
                rpe = rpe,
                rir = rir,
                status = status,
                painSeverity = painSeverity,
                painLocation = painLocation,
                notes = notes
            )
            val newPr = repository.logSetAndCheckPR(setLog, exerciseName)
            if (newPr != null) {
                _latestPrEarned.value = newPr
            }
        }
    }

    fun dismissLatestPr() {
        _latestPrEarned.value = null
    }

    fun finishWorkout(sessionId: String, overallRpe: Int, notes: String = "") {
        viewModelScope.launch {
            repository.completeWorkoutSession(sessionId, overallRpe, notes)
        }
    }

    // -------------------------------------------------------------
    // Progression & Plateau Evaluation
    // -------------------------------------------------------------
    suspend fun getProgressionDecision(
        exercise: ExerciseEntity,
        currentSets: Int,
        currentReps: Int,
        currentHoldSec: Int
    ): ProgressionDecision {
        val profileId = _activeProfile.value?.id ?: ""
        val recentLogs = repository.workoutDao.getSetLogsForExerciseDirect(profileId, exercise.id)
        val recentSleep = _recoveryEntries.value.take(3).map { it.sleepDurationHours }
        val avgSleep = if (recentSleep.isNotEmpty()) recentSleep.average().toFloat() else 7.5f
        val recentFatigue = _recoveryEntries.value.take(3).map { it.fatigueLevel }
        val avgFatigue = if (recentFatigue.isNotEmpty()) recentFatigue.average().toInt() else 2

        return ProgressionEngine.evaluateProgression(
            exercise = exercise,
            currentSets = currentSets,
            currentReps = currentReps,
            currentHoldSec = currentHoldSec,
            recentSetLogs = recentLogs.takeLast(currentSets * 2),
            recentSleepAvg = avgSleep,
            recentFatigueAvg = avgFatigue
        )
    }

    suspend fun checkExercisePlateau(exerciseId: String): PlateauAnalysisResult {
        val profileId = _activeProfile.value?.id ?: ""
        val logs = repository.workoutDao.getSetLogsForExerciseDirect(profileId, exerciseId)
        val groupedBySession = logs.groupBy { it.sessionId }
        val sleepAvg = _recoveryEntries.value.take(3).map { it.sleepQuality.toFloat() }.average().toFloat().let {
            if (it.isNaN()) 3.5f else it
        }
        val hasPain = _painEntries.value.any { it.status == "RED" || it.status == "YELLOW" }

        return PlateauEngine.analyzePlateau(
            exerciseId = exerciseId,
            sessionsLogs = groupedBySession,
            sleepQualityAvg = sleepAvg,
            recentPainLogged = hasPain
        )
    }

    // -------------------------------------------------------------
    // Program Management & Rollback
    // -------------------------------------------------------------
    fun rollbackProgram(targetVersionNumber: Int) {
        val prog = _activeProgram.value ?: return
        viewModelScope.launch {
            repository.rollbackProgramVersion(prog.id, targetVersionNumber)
        }
    }

    // -------------------------------------------------------------
    // Lifestyle: Nutrition, Recovery & Pain
    // -------------------------------------------------------------
    fun logNutrition(
        foodName: String,
        mealType: String,
        calories: Int,
        proteinG: Float,
        carbsG: Float,
        fatG: Float,
        waterMl: Int = 0,
        notes: String = ""
    ) {
        val profileId = _activeProfile.value?.id ?: return
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            val entry = NutritionEntryEntity(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                date = todayStr,
                mealType = mealType,
                foodName = foodName,
                calories = calories,
                proteinG = proteinG,
                carbsG = carbsG,
                fatG = fatG,
                waterMl = waterMl,
                notes = notes
            )
            repository.nutritionDao.insertNutrition(entry)
        }
    }

    fun deleteNutrition(id: String) {
        viewModelScope.launch {
            repository.nutritionDao.deleteNutrition(id)
        }
    }

    fun logDailyRecovery(
        sleepHours: Float,
        sleepQuality: Int,
        energyLevel: Int,
        fatigueLevel: Int,
        muscleSoreness: Int,
        stressLevel: Int,
        notes: String = ""
    ) {
        val profileId = _activeProfile.value?.id ?: return
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Calculate readiness score
        val readiness = when {
            fatigueLevel >= 4 || sleepHours < 5.5f || stressLevel >= 4 -> "RED"
            fatigueLevel == 3 || sleepHours < 7.0f || muscleSoreness >= 3 -> "YELLOW"
            else -> "GREEN"
        }

        viewModelScope.launch {
            val recovery = RecoveryEntryEntity(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                date = todayStr,
                sleepDurationHours = sleepHours,
                sleepQuality = sleepQuality,
                energyLevel = energyLevel,
                fatigueLevel = fatigueLevel,
                muscleSoreness = muscleSoreness,
                stressLevel = stressLevel,
                readiness = readiness,
                notes = notes
            )
            repository.recoveryDao.insertRecovery(recovery)

            // Also record daily check-in
            val checkIn = DailyCheckInEntity(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                date = todayStr,
                sleepHours = sleepHours,
                energy = energyLevel,
                fatigue = fatigueLevel,
                soreness = muscleSoreness,
                readiness = readiness,
                notes = notes
            )
            repository.recoveryDao.insertCheckIn(checkIn)
        }
    }

    fun logPainEntry(
        location: String,
        severity: Int,
        timing: String,
        exerciseId: String? = null,
        duringWorkout: Boolean = false,
        afterWorkout: Boolean = false,
        nextDay: Boolean = false,
        effectOnForm: Boolean = false,
        notes: String = ""
    ) {
        val profileId = _activeProfile.value?.id ?: return
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Conservative classification according to specs:
        // GREEN: mild, no worsening
        // YELLOW: increased symptoms, form alteration
        // RED: severe (>=7), numbness, joint instability, red flags
        val status = when {
            severity >= 7 || effectOnForm && severity >= 5 -> "RED"
            severity in 3..6 || effectOnForm -> "YELLOW"
            else -> "GREEN"
        }

        viewModelScope.launch {
            val entry = PainEntryEntity(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                date = todayStr,
                location = location,
                severity = severity,
                timing = timing,
                exerciseId = exerciseId,
                duringWorkout = duringWorkout,
                afterWorkout = afterWorkout,
                nextDay = nextDay,
                effectOnForm = effectOnForm,
                status = status,
                notes = notes
            )
            repository.recoveryDao.insertPainEntry(entry)
        }
    }

    // -------------------------------------------------------------
    // Skills Progress
    // -------------------------------------------------------------
    fun updateSkillStep(skillId: String, newStepIndex: Int, holdSec: Int, reps: Int, quality: Int) {
        val profileId = _activeProfile.value?.id ?: return
        viewModelScope.launch {
            val current = repository.skillDao.getSkillProgressDirect(profileId, skillId)
            val updated = SkillProgressEntity(
                id = current?.id ?: UUID.randomUUID().toString(),
                profileId = profileId,
                skillId = skillId,
                currentStepIndex = newStepIndex,
                bestHoldSec = maxOf(holdSec, current?.bestHoldSec ?: 0),
                bestReps = maxOf(reps, current?.bestReps ?: 0),
                qualityScore = quality,
                lastTrainedDate = System.currentTimeMillis()
            )
            repository.skillDao.insertOrUpdateSkillProgress(updated)
        }
    }

    // -------------------------------------------------------------
    // Custom Exercise Creation
    // -------------------------------------------------------------
    fun createCustomExercise(
        persianName: String,
        englishName: String,
        category: String,
        muscles: List<String>,
        movementPattern: String,
        difficulty: String,
        startingPosition: String,
        instructions: List<String>,
        safetyNotes: String
    ) {
        val profileId = _activeProfile.value?.id
        viewModelScope.launch {
            val exercise = ExerciseEntity(
                id = "custom_" + UUID.randomUUID().toString().take(8),
                persianName = persianName,
                englishName = englishName,
                category = category,
                musclesJson = muscles.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]"),
                movementPattern = movementPattern,
                difficulty = difficulty,
                startingPosition = startingPosition,
                instructionsJson = instructions.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]"),
                safetyCriteria = safetyNotes,
                isCustom = true,
                profileId = profileId
            )
            repository.exerciseDao.insertExercise(exercise)
        }
    }

    // -------------------------------------------------------------
    // Metrics
    // -------------------------------------------------------------
    fun recordMetric(metricType: String, value: Float, unit: String, notes: String = "") {
        val profileId = _activeProfile.value?.id ?: return
        viewModelScope.launch {
            val metric = MetricEntity(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                metricType = metricType,
                value = value,
                unit = unit,
                notes = notes
            )
            repository.metricDao.insertMetric(metric)
        }
    }

    // -------------------------------------------------------------
    // Science Pack Update
    // -------------------------------------------------------------
    fun updateSciencePack() {
        viewModelScope.launch {
            repository.checkAndApplyScienceUpdate("v1.1")
        }
    }

    // -------------------------------------------------------------
    // Backup & Restore
    // -------------------------------------------------------------
    suspend fun exportBackupJson(): String {
        return repository.exportFullBackup()
    }

    fun validateBackup(jsonStr: String): BackupValidationResult {
        return repository.validateBackupJson(jsonStr)
    }

    suspend fun restoreBackup(jsonStr: String): Boolean {
        return repository.restoreFullBackup(jsonStr)
    }

    // -------------------------------------------------------------
    // Reduce Motion Toggle
    // -------------------------------------------------------------
    fun toggleReduceMotion(enabled: Boolean) {
        viewModelScope.launch {
            repository.appSettingDao.setSetting(AppSettingEntity("reduce_motion", enabled.toString()))
        }
    }

    // -------------------------------------------------------------
    // Global Timer / Stopwatch Control
    // -------------------------------------------------------------
    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        _timerTotalSeconds.value = seconds
        _timerSecondsRemaining.value = seconds
        _isTimerRunning.value = true

        timerJob = viewModelScope.launch {
            while (_timerSecondsRemaining.value > 0) {
                delay(1000)
                _timerSecondsRemaining.value -= 1
            }
            _isTimerRunning.value = false
            // Vibrate on finish
            try {
                vibrator?.vibrate(longArrayOf(0, 400, 200, 400), -1)
            } catch (_: Exception) {}
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
    }

    fun resumeTimer() {
        if (_timerSecondsRemaining.value > 0) {
            _isTimerRunning.value = true
            timerJob = viewModelScope.launch {
                while (_timerSecondsRemaining.value > 0) {
                    delay(1000)
                    _timerSecondsRemaining.value -= 1
                }
                _isTimerRunning.value = false
                try {
                    vibrator?.vibrate(longArrayOf(0, 400, 200, 400), -1)
                } catch (_: Exception) {}
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _timerSecondsRemaining.value = _timerTotalSeconds.value
    }

    fun startStopwatch() {
        stopwatchJob?.cancel()
        _isStopwatchRunning.value = true
        stopwatchJob = viewModelScope.launch {
            while (_isStopwatchRunning.value) {
                delay(1000)
                _stopwatchSeconds.value += 1
            }
        }
    }

    fun pauseStopwatch() {
        _isStopwatchRunning.value = false
        stopwatchJob?.cancel()
    }

    fun resetStopwatch() {
        _isStopwatchRunning.value = false
        stopwatchJob?.cancel()
        _stopwatchSeconds.value = 0
    }

    // -------------------------------------------------------------
    // Habits Toggle
    // -------------------------------------------------------------
    fun toggleHabit(habitKey: String) {
        val profileId = _activeProfile.value?.id ?: return
        viewModelScope.launch {
            val updated = repository.toggleHabit(profileId, habitKey)
            _todayHabits.value = updated
        }
    }

    // -------------------------------------------------------------
    // Skill Integration into Program
    // -------------------------------------------------------------
    fun integrateSkillIntoWorkout(skillId: String) {
        val profileId = _activeProfile.value?.id ?: return
        viewModelScope.launch {
            repository.integrateSkillIntoRoutine(profileId, skillId)
        }
    }

    // -------------------------------------------------------------
    // Program Builder & Planning Engine Operations
    // -------------------------------------------------------------
    fun updateUserEquipmentProfile(config: UserEquipmentProfile) {
        val profileId = activeProfileId.value ?: return
        viewModelScope.launch {
            repository.saveUserEquipmentProfile(profileId, config)
            _userEquipmentProfile.value = config
        }
    }

    fun generateSmartProgram(
        targetDaysPerWeek: Int? = null,
        sessionDurationMinutes: Int? = null,
        forcedSplit: String? = null
    ) {
        val currentProfile = _activeProfile.value ?: return
        viewModelScope.launch {
            repository.generateSmartProgram(
                profile = currentProfile,
                targetDaysPerWeek = targetDaysPerWeek,
                sessionDurationMinutes = sessionDurationMinutes,
                forcedSplit = forcedSplit,
                equipmentProfile = _userEquipmentProfile.value
            )
        }
    }

    fun addExerciseToSession(
        sessionId: String,
        exerciseId: String,
        targetSets: Int = 3,
        targetRpe: Int = 7
    ) {
        viewModelScope.launch {
            repository.addExerciseToWorkoutSession(sessionId, exerciseId, targetSets, targetRpe)
        }
    }

    fun removeExerciseFromSession(workoutExerciseId: String) {
        viewModelScope.launch {
            repository.removeExerciseFromWorkoutSession(workoutExerciseId)
        }
    }

    fun adjustExerciseDifficulty(workoutExerciseId: String, increase: Boolean) {
        viewModelScope.launch {
            repository.adjustWorkoutExerciseDifficulty(workoutExerciseId, increase)
        }
    }

    fun adaptUpcomingSessions() {
        val profileId = _activeProfile.value?.id ?: return
        viewModelScope.launch {
            repository.adaptUpcomingSessions(profileId)
        }
    }

    fun buildCustomProgram(
        title: String,
        description: String,
        level: String,
        daysPerWeek: Int,
        exerciseIds: List<String>
    ) {
        val profileId = _activeProfile.value?.id ?: return
        viewModelScope.launch {
            repository.createCustomProgram(profileId, title, description, level, daysPerWeek, exerciseIds)
        }
    }
}
