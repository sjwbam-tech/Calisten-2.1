package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
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
import com.example.data.local.entity.AdaptationDecisionEntity
import com.example.domain.engine.BackupRestoreManager
import com.example.domain.engine.BackupValidationResult
import com.example.domain.engine.FullBackupData
import com.example.domain.engine.DataDrivenProgramGenerator
import com.example.domain.engine.ProgramGenerationParams
import com.example.domain.engine.PlanModificationEngine
import com.example.domain.engine.DifficultyDirection
import com.example.domain.engine.ExerciseRelationshipGraph
import com.example.domain.engine.UserCapabilityEngine
import com.example.domain.equipment.UserEquipmentProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class KalistenRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)

    // DAOs
    val profileDao = db.profileDao()
    val assessmentDao = db.assessmentDao()
    val exerciseDao = db.exerciseDao()
    val programDao = db.programDao()
    val programVersionDao = db.programVersionDao()
    val workoutDao = db.workoutDao()
    val skillDao = db.skillDao()
    val metricDao = db.metricDao()
    val prDao = db.prDao()
    val nutritionDao = db.nutritionDao()
    val recoveryDao = db.recoveryDao()
    val scienceDao = db.scienceDao()
    val appSettingDao = db.appSettingDao()
    val adaptationDecisionDao = db.adaptationDecisionDao()
    val userCapabilityDao = db.userCapabilityDao()
    val exerciseRelationshipDao = db.exerciseRelationshipDao()

    // -------------------------------------------------------------
    // Static Library Initialization
    // -------------------------------------------------------------
    suspend fun ensureStaticContentInitialized() = withContext(Dispatchers.IO) {
        val existing = exerciseDao.getAllExercisesDirect()
        if (existing.isEmpty()) {
            exerciseDao.insertExercises(com.example.data.local.SeedData.exercises)
            skillDao.insertSkills(com.example.data.local.SeedData.skills)
            scienceDao.insertSources(com.example.data.local.SeedData.scienceSources)
            appSettingDao.setSetting(AppSettingEntity("science_pack_version", "v1.0"))
        } else {
            val existingIds = existing.map { it.id }.toSet()
            val missing = com.example.data.local.SeedData.exercises.filter { it.id !in existingIds }
            if (missing.isNotEmpty()) {
                exerciseDao.insertExercises(missing)
            }
        }
    }

    // -------------------------------------------------------------
    // Adaptive Equipment Profile Management
    // -------------------------------------------------------------
    fun getUserEquipmentProfile(profileId: String): Flow<UserEquipmentProfile> = flow {
        val json = appSettingDao.getSettingDirect("equipment_config_$profileId")
            ?: appSettingDao.getSettingDirect("user_equipment_profile")
        if (!json.isNullOrBlank()) {
            emit(UserEquipmentProfile.fromJson(json))
        } else {
            val profile = profileDao.getProfileByIdDirect(profileId)
            val list = if (profile != null) {
                try {
                    val cleaned = profile.equipmentJson.trim().removeSurrounding("[", "]")
                    if (cleaned.isBlank()) emptyList()
                    else cleaned.split(",").map { it.trim().removeSurrounding("\"").removeSurrounding("'").trim() }.filter { it.isNotBlank() }
                } catch (_: Exception) {
                    emptyList()
                }
            } else emptyList()
            emit(
                UserEquipmentProfile(
                    hasBodyweightOnly = list.isEmpty(),
                    hasPullupBar = list.contains("pullup_bar"),
                    hasParallettes = list.contains("parallettes"),
                    hasRings = list.contains("rings"),
                    hasResistanceBands = list.contains("resistance_bands"),
                    hasSingleDumbbell = list.contains("dumbbell_single"),
                    hasPairDumbbells = list.contains("dumbbell_pair"),
                    hasAdjustableDumbbells = list.contains("dumbbell_adjustable"),
                    hasBarbell = list.contains("barbell"),
                    hasWeightVest = list.contains("weight_vest"),
                    hasBench = list.contains("bench")
                )
            )
        }
    }

    suspend fun saveUserEquipmentProfile(profileId: String, config: UserEquipmentProfile) {
        val json = config.toJson()
        appSettingDao.setSetting(AppSettingEntity("equipment_config_$profileId", json))
        appSettingDao.setSetting(AppSettingEntity("user_equipment_profile", json))
        val profile = profileDao.getProfileByIdDirect(profileId)
        if (profile != null) {
            val equipmentList = config.toEquipmentIdList()
            val equipmentJson = equipmentList.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
            profileDao.updateProfile(profile.copy(equipmentJson = equipmentJson, updatedAt = System.currentTimeMillis()))
        }
    }

    // -------------------------------------------------------------
    // Active Profile Management
    // -------------------------------------------------------------
    fun getActiveProfileId(): Flow<String?> = appSettingDao.getSetting("active_profile_id")

    suspend fun setActiveProfileId(profileId: String) {
        appSettingDao.setSetting(AppSettingEntity("active_profile_id", profileId))
    }

    suspend fun createProfile(profile: ProfileEntity, assessment: AssessmentEntity? = null) {
        db.withTransaction {
            profileDao.insertProfile(profile)
            if (assessment != null) {
                assessmentDao.insertAssessment(assessment)
                // Automatically record baseline metrics
                if (assessment.pushupsMax > 0) {
                    metricDao.insertMetric(
                        MetricEntity(
                            id = UUID.randomUUID().toString(),
                            profileId = profile.id,
                            metricType = "PUSHUPS",
                            value = assessment.pushupsMax.toFloat(),
                            unit = "تکرار",
                            notes = "ارزیابی اولیه رکورد شنا"
                        )
                    )
                }
                if (assessment.pullupsMax > 0) {
                    metricDao.insertMetric(
                        MetricEntity(
                            id = UUID.randomUUID().toString(),
                            profileId = profile.id,
                            metricType = "PULLUPS",
                            value = assessment.pullupsMax.toFloat(),
                            unit = "تکرار",
                            notes = "ارزیابی اولیه رکورد بارفیکس"
                        )
                    )
                }
                if (assessment.plankSec > 0) {
                    metricDao.insertMetric(
                        MetricEntity(
                            id = UUID.randomUUID().toString(),
                            profileId = profile.id,
                            metricType = "PLANK",
                            value = assessment.plankSec.toFloat(),
                            unit = "ثانیه",
                            notes = "ارزیابی اولیه پلانک"
                        )
                    )
                }
            }
            // Always record initial bodyweight
            if (profile.weightKg > 0) {
                metricDao.insertMetric(
                    MetricEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = profile.id,
                        metricType = "WEIGHT",
                        value = profile.weightKg,
                        unit = "کیلوگرم",
                        notes = "وزن اولیه ثبت‌شده در پروفایل"
                    )
                )
            }
            // Set as active profile
            appSettingDao.setSetting(AppSettingEntity("active_profile_id", profile.id))
        }
    }

    suspend fun deleteProfile(profileId: String) {
        db.withTransaction {
            profileDao.deleteProfile(profileId)
            val currentActive = appSettingDao.getSettingDirect("active_profile_id")
            if (currentActive == profileId) {
                val remaining = profileDao.getAllProfilesDirect()
                if (remaining.isNotEmpty()) {
                    appSettingDao.setSetting(AppSettingEntity("active_profile_id", remaining.first().id))
                } else {
                    appSettingDao.setSetting(AppSettingEntity("active_profile_id", ""))
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Smart Program Generator & Versioning
    // -------------------------------------------------------------
    suspend fun generateSmartProgram(
        profile: ProfileEntity,
        targetDaysPerWeek: Int? = null,
        sessionDurationMinutes: Int? = null,
        forcedSplit: String? = null,
        equipmentProfile: UserEquipmentProfile? = null
    ): ProgramEntity {
        return db.withTransaction {
            val assessment = assessmentDao.getAssessmentForProfileDirect(profile.id)
            val recentLogs = workoutDao.getSetLogsForProfileDirect(profile.id)
            val recentRecovery = recoveryDao.getRecentRecoveryForProfileDirect(profile.id, 10)
            val recentPain = recoveryDao.getRecentPainForProfileDirect(profile.id, 10)
            val skillProgress = skillDao.getProgressForProfile(profile.id).firstOrNull() ?: emptyList()
            val allExercises = exerciseDao.getAllExercisesDirect()

            val effectiveEquipmentProfile = equipmentProfile ?: run {
                val json = appSettingDao.getSettingDirect("equipment_config_${profile.id}")
                    ?: appSettingDao.getSettingDirect("user_equipment_profile")
                if (!json.isNullOrBlank()) {
                    UserEquipmentProfile.fromJson(json)
                } else {
                    null
                }
            }

            val currentCap = userCapabilityDao.getCapabilityForProfileDirect(profile.id)
            val capability = UserCapabilityEngine.calculateCapability(
                profileId = profile.id,
                assessment = assessment,
                recentSetLogs = recentLogs,
                skillProgress = skillProgress,
                recentRecovery = recentRecovery,
                recentPain = recentPain,
                currentCapability = currentCap
            )
            userCapabilityDao.insertCapability(capability)

            val programPackage = DataDrivenProgramGenerator.generateProgram(
                ProgramGenerationParams(
                    profile = profile,
                    capability = capability,
                    allExercises = allExercises,
                    assessment = assessment,
                    recentSetLogs = recentLogs,
                    recentRecovery = recentRecovery,
                    recentPain = recentPain,
                    skillProgress = skillProgress,
                    targetDaysPerWeek = targetDaysPerWeek,
                    sessionDurationMinutes = sessionDurationMinutes,
                    forcedSplit = forcedSplit,
                    userEquipmentProfile = effectiveEquipmentProfile
                )
            )

            // Archive any existing active programs for profile
            val existingActive = programDao.getProgramsForProfile(profile.id).firstOrNull()?.filter { it.status == "ACTIVE" }
            existingActive?.forEach {
                programDao.updateProgram(it.copy(status = "ARCHIVED", updatedAt = System.currentTimeMillis()))
            }

            programDao.insertProgram(programPackage.program)
            programVersionDao.insertVersion(programPackage.initialVersion)
            programPackage.sessions.forEach { workoutDao.insertSession(it) }
            workoutDao.insertWorkoutExercises(programPackage.workoutExercises)
            programPackage.decisions.forEach { adaptationDecisionDao.insertDecision(it) }

            programPackage.program
        }
    }

    suspend fun addExerciseToWorkoutSession(
        sessionId: String,
        exerciseId: String,
        targetSets: Int = 3,
        targetRpe: Int = 7
    ): WorkoutExerciseEntity? {
        return db.withTransaction {
            val session = workoutDao.getSessionByIdDirect(sessionId) ?: return@withTransaction null
            val existingExercises = workoutDao.getExercisesForSessionDirect(sessionId)
            val exerciseDetail = exerciseDao.getExerciseByIdDirect(exerciseId) ?: return@withTransaction null

            val result = PlanModificationEngine.addExerciseToSession(
                session = session,
                existingExercises = existingExercises,
                newExercise = exerciseDetail,
                targetSets = targetSets,
                targetRpe = targetRpe
            )

            val addedEntity = result.updatedExercise ?: return@withTransaction null
            workoutDao.insertWorkoutExercise(addedEntity)
            result.decision?.let { adaptationDecisionDao.insertDecision(it) }

            session.programId?.let { progId ->
                createProgramVersion(
                    programId = progId,
                    reason = "افزودن ${exerciseDetail.persianName} به ${session.title}",
                    changesSummary = result.changeExplanation,
                    newStructureJson = "{\"exerciseAdded\": \"$exerciseId\", \"sessionId\": \"$sessionId\"}"
                )
            }

            addedEntity
        }
    }

    suspend fun removeExerciseFromWorkoutSession(workoutExerciseId: String): Boolean {
        return db.withTransaction {
            val we = workoutDao.getWorkoutExerciseByIdDirect(workoutExerciseId) ?: return@withTransaction false
            val session = workoutDao.getSessionByIdDirect(we.sessionId) ?: return@withTransaction false
            val existingExercises = workoutDao.getExercisesForSessionDirect(we.sessionId)

            val result = PlanModificationEngine.removeExerciseFromSession(
                session = session,
                existingExercises = existingExercises,
                workoutExerciseIdToRemove = workoutExerciseId
            )

            workoutDao.deleteWorkoutExercise(workoutExerciseId)
            workoutDao.insertWorkoutExercises(result.reindexedSessionExercises)
            result.decision?.let { adaptationDecisionDao.insertDecision(it) }

            session.programId?.let { progId ->
                createProgramVersion(
                    programId = progId,
                    reason = "حذف حرکت از ${session.title}",
                    changesSummary = result.changeExplanation,
                    newStructureJson = "{\"exerciseRemoved\": \"${we.exerciseId}\", \"sessionId\": \"${session.id}\"}"
                )
            }

            true
        }
    }

    suspend fun adjustWorkoutExerciseDifficulty(workoutExerciseId: String, increase: Boolean): WorkoutExerciseEntity? {
        return db.withTransaction {
            val we = workoutDao.getWorkoutExerciseByIdDirect(workoutExerciseId) ?: return@withTransaction null
            val session = workoutDao.getSessionByIdDirect(we.sessionId) ?: return@withTransaction null
            val exerciseDetail = exerciseDao.getExerciseByIdDirect(we.exerciseId) ?: return@withTransaction null
            val allExercises = exerciseDao.getAllExercisesDirect()
            val relationships = exerciseRelationshipDao.getAllRelationshipsDirect()
            val graph = ExerciseRelationshipGraph(allExercises, relationships)

            val direction = if (increase) DifficultyDirection.INCREASE else DifficultyDirection.DECREASE
            val result = PlanModificationEngine.adjustDifficulty(
                session = session,
                currentWorkoutExercise = we,
                currentExerciseDetail = exerciseDetail,
                direction = direction,
                allExercises = allExercises,
                relationshipGraph = graph
            )

            val updated = result.updatedExercise ?: return@withTransaction null
            workoutDao.insertWorkoutExercise(updated)
            result.decision?.let { adaptationDecisionDao.insertDecision(it) }

            session.programId?.let { progId ->
                createProgramVersion(
                    programId = progId,
                    reason = "تعدیل سطح دشواری ${exerciseDetail.persianName}",
                    changesSummary = result.changeExplanation,
                    newStructureJson = "{\"oldExercise\": \"${we.exerciseId}\", \"newExercise\": \"${updated.exerciseId}\", \"direction\": \"$direction\"}"
                )
            }

            updated
        }
    }

    suspend fun adaptUpcomingSessions(profileId: String): List<AdaptationDecisionEntity> {
        return db.withTransaction {
            val plannedSessions = workoutDao.getPlannedSessionsForProfileDirect(profileId)
            val recentLogs = workoutDao.getSetLogsForProfileDirect(profileId)
            val allExercises = exerciseDao.getAllExercisesDirect()

            val decisionsCreated = mutableListOf<AdaptationDecisionEntity>()

            for (session in plannedSessions) {
                val exercises = workoutDao.getExercisesForSessionDirect(session.id)
                val adaptedPairs = PlanModificationEngine.adaptUpcomingExercises(exercises, recentLogs, allExercises)
                for ((adaptedWE, decision) in adaptedPairs) {
                    if (adaptedWE != exercises.firstOrNull { it.id == adaptedWE.id }) {
                        workoutDao.insertWorkoutExercise(adaptedWE)
                    }
                    if (decision != null) {
                        val boundDecision = decision.copy(
                            profileId = profileId,
                            programId = session.programId ?: ""
                        )
                        adaptationDecisionDao.insertDecision(boundDecision)
                        decisionsCreated.add(boundDecision)
                    }
                }
            }

            decisionsCreated
        }
    }

    suspend fun createProgramVersion(
        programId: String,
        reason: String,
        changesSummary: String,
        newStructureJson: String
    ): Int {
        return db.withTransaction {
            val program = programDao.getProgramByIdDirect(programId)
                ?: return@withTransaction 1
            val newVer = program.currentVersionNumber + 1
            val versionEntity = ProgramVersionEntity(
                id = UUID.randomUUID().toString(),
                programId = programId,
                versionNumber = newVer,
                reason = reason,
                changesSummary = changesSummary,
                programStructureJson = newStructureJson,
                previousVersionNumber = program.currentVersionNumber
            )
            programVersionDao.insertVersion(versionEntity)
            programDao.updateProgram(program.copy(currentVersionNumber = newVer, updatedAt = System.currentTimeMillis()))
            newVer
        }
    }

    suspend fun rollbackProgramVersion(programId: String, targetVersionNumber: Int): Boolean {
        return db.withTransaction {
            val targetVersion = programVersionDao.getVersion(programId, targetVersionNumber)
                ?: return@withTransaction false
            val program = programDao.getProgramByIdDirect(programId)
                ?: return@withTransaction false

            val newVersionNumber = program.currentVersionNumber + 1
            val rollbackRecord = ProgramVersionEntity(
                id = UUID.randomUUID().toString(),
                programId = programId,
                versionNumber = newVersionNumber,
                reason = "بازگشت (Rollback) به تنظیمات نسخه $targetVersionNumber",
                changesSummary = "بازیابی ساختار برنامه از نسخه $targetVersionNumber: ${targetVersion.changesSummary}",
                programStructureJson = targetVersion.programStructureJson,
                previousVersionNumber = program.currentVersionNumber
            )
            programVersionDao.insertVersion(rollbackRecord)
            programDao.updateProgram(program.copy(currentVersionNumber = newVersionNumber, updatedAt = System.currentTimeMillis()))
            true
        }
    }

    // -------------------------------------------------------------
    // Set Logging & Automatic PR Detection
    // -------------------------------------------------------------
    suspend fun logSetAndCheckPR(
        setLog: SetLogEntity,
        exerciseName: String
    ): PREntity? {
        return db.withTransaction {
            workoutDao.insertSetLog(setLog)

            // Real PR Check: only if status is COMPLETED and pain is 0
            if (setLog.status == "COMPLETED" && setLog.painSeverity == 0) {
                val existingPR = prDao.getPRForExerciseDirect(setLog.profileId, setLog.exerciseId)

                var newPR: PREntity? = null
                if (setLog.holdSeconds > 0) {
                    if (existingPR == null || setLog.holdSeconds > existingPR.value) {
                        newPR = PREntity(
                            id = UUID.randomUUID().toString(),
                            profileId = setLog.profileId,
                            exerciseOrSkillId = setLog.exerciseId,
                            title = "رکورد مکث: $exerciseName",
                            prType = "MAX_DURATION",
                            value = setLog.holdSeconds.toFloat(),
                            unit = "ثانیه",
                            date = System.currentTimeMillis(),
                            conditionsNote = "ست ${setLog.setNumber} با RPE ${setLog.rpe}"
                        )
                        prDao.insertPR(newPR)
                    }
                } else if (setLog.reps > 0) {
                    if (existingPR == null || setLog.reps > existingPR.value) {
                        newPR = PREntity(
                            id = UUID.randomUUID().toString(),
                            profileId = setLog.profileId,
                            exerciseOrSkillId = setLog.exerciseId,
                            title = "رکورد تکرار: $exerciseName",
                            prType = "MAX_REPS",
                            value = setLog.reps.toFloat(),
                            unit = "تکرار",
                            date = System.currentTimeMillis(),
                            conditionsNote = "ست ${setLog.setNumber} با RPE ${setLog.rpe}"
                        )
                        prDao.insertPR(newPR)
                    }
                }
                newPR
            } else {
                null
            }
        }
    }

    suspend fun completeWorkoutSession(
        sessionId: String,
        overallRpe: Int,
        notes: String = ""
    ) {
        db.withTransaction {
            val session = workoutDao.getSessionByIdDirect(sessionId) ?: return@withTransaction
            val setLogs = workoutDao.getSetLogsForSessionDirect(sessionId)
            val hadPain = setLogs.any { it.status == "PAIN_STOP" || it.painSeverity >= 4 }

            val completedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            workoutDao.updateSession(
                session.copy(
                    completedDate = completedDateStr,
                    status = "COMPLETED",
                    overallRpe = overallRpe,
                    painFlag = hadPain,
                    notes = notes
                )
            )
        }
    }

    // -------------------------------------------------------------
    // Full Backup and Restore System
    // -------------------------------------------------------------
    suspend fun exportFullBackup(): String = withContext(Dispatchers.IO) {
        val fullData = FullBackupData(
            profiles = profileDao.getAllProfilesDirect(),
            programs = programDao.getAllProgramsDirect(),
            programVersions = programVersionDao.getAllVersionsDirect(),
            workoutSessions = workoutDao.getAllSessionsDirect(),
            workoutExercises = workoutDao.getAllWorkoutExercisesDirect(),
            setLogs = workoutDao.getAllSetLogsDirect(),
            skillProgress = skillDao.getAllSkillProgressDirect(),
            metrics = metricDao.getAllMetricsDirect(),
            personalRecords = prDao.getAllPRsDirect(),
            nutritionEntries = nutritionDao.getAllNutritionDirect(),
            recoveryEntries = recoveryDao.getAllRecoveryDirect(),
            dailyCheckIns = recoveryDao.getAllCheckInsDirect(),
            painEntries = recoveryDao.getAllPainEntriesDirect()
        )
        BackupRestoreManager.serializeBackup(fullData)
    }

    fun validateBackupJson(jsonStr: String): BackupValidationResult {
        return BackupRestoreManager.validateBackup(jsonStr)
    }

    suspend fun restoreFullBackup(jsonStr: String): Boolean = withContext(Dispatchers.IO) {
        val validation = BackupRestoreManager.validateBackup(jsonStr)
        if (!validation.isValid) {
            return@withContext false
        }

        val data = BackupRestoreManager.parseBackup(jsonStr)

        db.withTransaction {
            // Restore Profiles
            for (p in data.profiles) {
                profileDao.insertProfile(p)
            }
            // Restore Programs & Versions
            for (prog in data.programs) {
                programDao.insertProgram(prog)
            }
            for (v in data.programVersions) {
                programVersionDao.insertVersion(v)
            }
            // Restore Workouts & Sets
            for (w in data.workoutSessions) {
                workoutDao.insertSession(w)
            }
            for (we in data.workoutExercises) {
                workoutDao.insertWorkoutExercise(we)
            }
            for (s in data.setLogs) {
                workoutDao.insertSetLog(s)
            }
            // Restore Skills, Metrics, PRs
            for (sp in data.skillProgress) {
                skillDao.insertOrUpdateSkillProgress(sp)
            }
            for (m in data.metrics) {
                metricDao.insertMetric(m)
            }
            for (pr in data.personalRecords) {
                prDao.insertPR(pr)
            }
            // Restore Lifestyle
            for (n in data.nutritionEntries) {
                nutritionDao.insertNutrition(n)
            }
            for (r in data.recoveryEntries) {
                recoveryDao.insertRecovery(r)
            }
            for (c in data.dailyCheckIns) {
                recoveryDao.insertCheckIn(c)
            }
            for (pe in data.painEntries) {
                recoveryDao.insertPainEntry(pe)
            }

            // Set active profile if profiles restored
            if (data.profiles.isNotEmpty()) {
                appSettingDao.setSetting(AppSettingEntity("active_profile_id", data.profiles.first().id))
            }
        }
        true
    }

    // -------------------------------------------------------------
    // Science Pack Updater
    // -------------------------------------------------------------
    suspend fun checkAndApplyScienceUpdate(targetVersion: String = "v1.1"): Boolean {
        return db.withTransaction {
            val current = appSettingDao.getSettingDirect("science_pack_version") ?: "v1.0"
            if (current != targetVersion) {
                appSettingDao.setSetting(AppSettingEntity("science_pack_version", targetVersion))
                true
            } else {
                false
            }
        }
    }

    // -------------------------------------------------------------
    // Strength + Skill Integration (Specification Requirement 21)
    // -------------------------------------------------------------
    suspend fun integrateSkillIntoRoutine(profileId: String, skillId: String): Boolean {
        return db.withTransaction {
            val activeProg = programDao.getProgramsForProfile(profileId).firstOrNull()?.firstOrNull { it.status == "ACTIVE" }
                ?: return@withTransaction false
            val session = workoutDao.getSessionsForProfile(profileId).firstOrNull()?.firstOrNull()
                ?: return@withTransaction false

            // Map skill to lead exercise
            val leadExerciseId = when (skillId) {
                "skill_front_lever" -> "front_lever_tuck"
                "skill_handstand" -> "handstand"
                "skill_lsit" -> "lsit"
                "skill_planche" -> "pushup_planche_lean"
                "skill_muscleup" -> "dips_parallel"
                else -> "front_lever_tuck"
            }

            val existingExercises = workoutDao.getExercisesForSessionDirect(session.id)
            if (existingExercises.any { it.exerciseId == leadExerciseId }) {
                return@withTransaction true // Already integrated
            }

            // Skill exercises are placed first (Neuromuscular skill practice prior to fatigue)
            val newExercise = WorkoutExerciseEntity(
                id = UUID.randomUUID().toString(),
                sessionId = session.id,
                exerciseId = leadExerciseId,
                orderIndex = 0,
                targetSets = 3,
                targetReps = 0,
                targetHoldSec = 12,
                targetRpe = 7,
                restSec = 120,
                notes = "تمرین تمرکز عصبی مهارت کالیستنیکس قبل از شروع حرکات قدرتی"
            )
            workoutDao.insertWorkoutExercise(newExercise)

            // Increment program version with reason
            createProgramVersion(
                programId = activeProg.id,
                reason = "ادغام کار مهارتی و قدرت پایه در جلسه تمرین",
                changesSummary = "افزودن بلوک تمرین مهارت در ابتدای جلسه تمرین با کنترل حجم هفتگی",
                newStructureJson = "{\"skillIntegrated\": \"$skillId\", \"leadExercise\": \"$leadExerciseId\"}"
            )
            true
        }
    }

    // -------------------------------------------------------------
    // Habits System (Specification Requirement 26 & 29)
    // -------------------------------------------------------------
    suspend fun toggleHabit(profileId: String, habitKey: String): List<String> {
        return db.withTransaction {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val existingCheckIn = recoveryDao.getDailyCheckIns(profileId).firstOrNull()?.firstOrNull { it.date == todayStr }

            val currentHabits = try {
                if (existingCheckIn != null && existingCheckIn.habitsCompletedJson.isNotEmpty()) {
                    val arr = org.json.JSONArray(existingCheckIn.habitsCompletedJson)
                    (0 until arr.length()).map { arr.getString(it) }.toMutableList()
                } else {
                    mutableListOf()
                }
            } catch (_: Exception) {
                mutableListOf()
            }

            if (currentHabits.contains(habitKey)) {
                currentHabits.remove(habitKey)
            } else {
                currentHabits.add(habitKey)
            }

            val jsonStr = currentHabits.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")

            if (existingCheckIn != null) {
                recoveryDao.insertCheckIn(existingCheckIn.copy(habitsCompletedJson = jsonStr))
            } else {
                recoveryDao.insertCheckIn(
                    DailyCheckInEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        date = todayStr,
                        sleepHours = 7.5f,
                        energy = 4,
                        fatigue = 2,
                        soreness = 2,
                        painLevel = 0,
                        readiness = "GREEN",
                        habitsCompletedJson = jsonStr
                    )
                )
            }
            currentHabits
        }
    }

    // -------------------------------------------------------------
    // Custom Program Builder with Warm-up / Main / Cooldown (Spec 14)
    // -------------------------------------------------------------
    suspend fun createCustomProgram(
        profileId: String,
        title: String,
        description: String,
        level: String,
        daysPerWeek: Int,
        exerciseIds: List<String>
    ): ProgramEntity {
        return db.withTransaction {
            val programId = UUID.randomUUID().toString()
            val program = ProgramEntity(
                id = programId,
                profileId = profileId,
                title = title,
                description = description,
                level = level,
                daysPerWeek = daysPerWeek,
                status = "ACTIVE",
                currentVersionNumber = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            programDao.insertProgram(program)

            // Initial Version record
            val version = ProgramVersionEntity(
                id = UUID.randomUUID().toString(),
                programId = programId,
                versionNumber = 1,
                reason = "ساخت برنامه سفارشی توسط کاربر با تفکیک ساختار تمرین",
                changesSummary = "تنظیم $daysPerWeek روز تمرینی با ${exerciseIds.size} حرکت انتخابی",
                programStructureJson = "{\"customPlan\": true, \"exerciseCount\": ${exerciseIds.size}}",
                previousVersionNumber = null
            )
            programVersionDao.insertVersion(version)

            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val sessionId = UUID.randomUUID().toString()
            val session = WorkoutSessionEntity(
                id = sessionId,
                profileId = profileId,
                programId = programId,
                title = "$title - جلسه اصلی",
                scheduledDate = todayStr,
                status = "PLANNED"
            )
            workoutDao.insertSession(session)

            val workoutExercises = exerciseIds.mapIndexed { idx, exId ->
                WorkoutExerciseEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    exerciseId = exId,
                    orderIndex = idx + 1,
                    targetSets = 3,
                    targetReps = 8,
                    targetHoldSec = 0,
                    targetRpe = 7,
                    restSec = 90
                )
            }
            workoutDao.insertWorkoutExercises(workoutExercises)

            program
        }
    }
}
