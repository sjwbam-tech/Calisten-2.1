package com.example.domain.engine

import com.example.data.local.entity.AppSettingEntity
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
import com.example.data.local.entity.SetLogEntity
import com.example.data.local.entity.SkillProgressEntity
import com.example.data.local.entity.WorkoutExerciseEntity
import com.example.data.local.entity.WorkoutSessionEntity
import org.json.JSONArray
import org.json.JSONObject

data class BackupValidationResult(
    val isValid: Boolean,
    val formatVersion: String?,
    val timestamp: Long?,
    val profileCount: Int,
    val workoutCount: Int,
    val prCount: Int,
    val setLogCount: Int,
    val previewSummary: String,
    val errorMessage: String? = null
)

data class FullBackupData(
    val format: String = "kalisten-backup-v1",
    val timestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val profiles: List<ProfileEntity>,
    val programs: List<ProgramEntity>,
    val programVersions: List<ProgramVersionEntity>,
    val workoutSessions: List<WorkoutSessionEntity>,
    val workoutExercises: List<WorkoutExerciseEntity>,
    val setLogs: List<SetLogEntity>,
    val skillProgress: List<SkillProgressEntity>,
    val metrics: List<MetricEntity>,
    val personalRecords: List<PREntity>,
    val nutritionEntries: List<NutritionEntryEntity>,
    val recoveryEntries: List<RecoveryEntryEntity>,
    val dailyCheckIns: List<DailyCheckInEntity>,
    val painEntries: List<PainEntryEntity>
)

object BackupRestoreManager {

    const val EXPECTED_FORMAT = "kalisten-backup-v1"

    /**
     * Serializes all application entities to a clean, well-structured JSON backup.
     */
    fun serializeBackup(data: FullBackupData): String {
        val root = JSONObject()
        root.put("format", data.format)
        root.put("timestamp", data.timestamp)
        root.put("appVersion", data.appVersion)

        // Profiles
        val profilesArr = JSONArray()
        data.profiles.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("age", p.age)
            obj.put("heightCm", p.heightCm.toDouble())
            obj.put("weightKg", p.weightKg.toDouble())
            obj.put("gender", p.gender)
            obj.put("waistCm", p.waistCm?.toDouble() ?: JSONObject.NULL)
            obj.put("activityLevel", p.activityLevel)
            obj.put("experienceLevel", p.experienceLevel)
            obj.put("goalsJson", p.goalsJson)
            obj.put("equipmentJson", p.equipmentJson)
            obj.put("environment", p.environment)
            obj.put("trainingDaysPerWeek", p.trainingDaysPerWeek)
            obj.put("preferredTimeOfDay", p.preferredTimeOfDay)
            obj.put("limitationsNotes", p.limitationsNotes)
            obj.put("createdAt", p.createdAt)
            obj.put("updatedAt", p.updatedAt)
            profilesArr.put(obj)
        }
        root.put("profiles", profilesArr)

        // Programs
        val programsArr = JSONArray()
        data.programs.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("profileId", p.profileId)
            obj.put("title", p.title)
            obj.put("description", p.description)
            obj.put("level", p.level)
            obj.put("daysPerWeek", p.daysPerWeek)
            obj.put("status", p.status)
            obj.put("currentVersionNumber", p.currentVersionNumber)
            obj.put("createdAt", p.createdAt)
            obj.put("updatedAt", p.updatedAt)
            programsArr.put(obj)
        }
        root.put("programs", programsArr)

        // Program Versions
        val versionsArr = JSONArray()
        data.programVersions.forEach { v ->
            val obj = JSONObject()
            obj.put("id", v.id)
            obj.put("programId", v.programId)
            obj.put("versionNumber", v.versionNumber)
            obj.put("createdAt", v.createdAt)
            obj.put("reason", v.reason)
            obj.put("changesSummary", v.changesSummary)
            obj.put("programStructureJson", v.programStructureJson)
            obj.put("previousVersionNumber", v.previousVersionNumber ?: JSONObject.NULL)
            versionsArr.put(obj)
        }
        root.put("programVersions", versionsArr)

        // Workout Sessions
        val sessionsArr = JSONArray()
        data.workoutSessions.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("profileId", s.profileId)
            obj.put("programId", s.programId ?: JSONObject.NULL)
            obj.put("title", s.title)
            obj.put("scheduledDate", s.scheduledDate)
            obj.put("completedDate", s.completedDate ?: JSONObject.NULL)
            obj.put("status", s.status)
            obj.put("totalDurationSec", s.totalDurationSec)
            obj.put("overallRpe", s.overallRpe ?: JSONObject.NULL)
            obj.put("painFlag", s.painFlag)
            obj.put("notes", s.notes)
            obj.put("createdAt", s.createdAt)
            sessionsArr.put(obj)
        }
        root.put("workoutSessions", sessionsArr)

        // Workout Exercises
        val exercisesArr = JSONArray()
        data.workoutExercises.forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("sessionId", e.sessionId)
            obj.put("exerciseId", e.exerciseId)
            obj.put("orderIndex", e.orderIndex)
            obj.put("targetSets", e.targetSets)
            obj.put("targetReps", e.targetReps)
            obj.put("targetHoldSec", e.targetHoldSec)
            obj.put("targetRpe", e.targetRpe)
            obj.put("restSec", e.restSec)
            obj.put("notes", e.notes)
            exercisesArr.put(obj)
        }
        root.put("workoutExercises", exercisesArr)

        // Set Logs
        val setLogsArr = JSONArray()
        data.setLogs.forEach { l ->
            val obj = JSONObject()
            obj.put("id", l.id)
            obj.put("workoutExerciseId", l.workoutExerciseId)
            obj.put("sessionId", l.sessionId)
            obj.put("profileId", l.profileId)
            obj.put("exerciseId", l.exerciseId)
            obj.put("setNumber", l.setNumber)
            obj.put("reps", l.reps)
            obj.put("holdSeconds", l.holdSeconds)
            obj.put("addedWeightKg", l.addedWeightKg.toDouble())
            obj.put("rpe", l.rpe)
            obj.put("rir", l.rir)
            obj.put("status", l.status)
            obj.put("painSeverity", l.painSeverity)
            obj.put("painLocation", l.painLocation)
            obj.put("notes", l.notes)
            obj.put("timestamp", l.timestamp)
            setLogsArr.put(obj)
        }
        root.put("setLogs", setLogsArr)

        // PRs
        val prsArr = JSONArray()
        data.personalRecords.forEach { pr ->
            val obj = JSONObject()
            obj.put("id", pr.id)
            obj.put("profileId", pr.profileId)
            obj.put("exerciseOrSkillId", pr.exerciseOrSkillId)
            obj.put("title", pr.title)
            obj.put("prType", pr.prType)
            obj.put("value", pr.value.toDouble())
            obj.put("unit", pr.unit)
            obj.put("date", pr.date)
            obj.put("conditionsNote", pr.conditionsNote)
            prsArr.put(obj)
        }
        root.put("personalRecords", prsArr)

        // Skill Progress
        val skillProgArr = JSONArray()
        data.skillProgress.forEach { sp ->
            val obj = JSONObject()
            obj.put("id", sp.id)
            obj.put("profileId", sp.profileId)
            obj.put("skillId", sp.skillId)
            obj.put("currentStepIndex", sp.currentStepIndex)
            obj.put("bestHoldSec", sp.bestHoldSec)
            obj.put("bestReps", sp.bestReps)
            obj.put("qualityScore", sp.qualityScore)
            obj.put("lastTrainedDate", sp.lastTrainedDate)
            obj.put("notes", sp.notes)
            skillProgArr.put(obj)
        }
        root.put("skillProgress", skillProgArr)

        // Metrics
        val metricsArr = JSONArray()
        data.metrics.forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("profileId", m.profileId)
            obj.put("metricType", m.metricType)
            obj.put("value", m.value.toDouble())
            obj.put("unit", m.unit)
            obj.put("notes", m.notes)
            obj.put("recordedAt", m.recordedAt)
            metricsArr.put(obj)
        }
        root.put("metrics", metricsArr)

        // Recovery
        val recoveryArr = JSONArray()
        data.recoveryEntries.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("profileId", r.profileId)
            obj.put("date", r.date)
            obj.put("sleepDurationHours", r.sleepDurationHours.toDouble())
            obj.put("sleepQuality", r.sleepQuality)
            obj.put("energyLevel", r.energyLevel)
            obj.put("fatigueLevel", r.fatigueLevel)
            obj.put("muscleSoreness", r.muscleSoreness)
            obj.put("stressLevel", r.stressLevel)
            obj.put("readiness", r.readiness)
            obj.put("notes", r.notes)
            obj.put("recordedAt", r.recordedAt)
            recoveryArr.put(obj)
        }
        root.put("recoveryEntries", recoveryArr)

        // Nutrition
        val nutritionArr = JSONArray()
        data.nutritionEntries.forEach { n ->
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("profileId", n.profileId)
            obj.put("date", n.date)
            obj.put("mealType", n.mealType)
            obj.put("foodName", n.foodName)
            obj.put("calories", n.calories)
            obj.put("proteinG", n.proteinG.toDouble())
            obj.put("carbsG", n.carbsG.toDouble())
            obj.put("fatG", n.fatG.toDouble())
            obj.put("waterMl", n.waterMl)
            obj.put("notes", n.notes)
            obj.put("recordedAt", n.recordedAt)
            nutritionArr.put(obj)
        }
        root.put("nutritionEntries", nutritionArr)

        // Daily Check-ins
        val checkInsArr = JSONArray()
        data.dailyCheckIns.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("profileId", c.profileId)
            obj.put("date", c.date)
            obj.put("sleepHours", c.sleepHours.toDouble())
            obj.put("energy", c.energy)
            obj.put("fatigue", c.fatigue)
            obj.put("soreness", c.soreness)
            obj.put("painLevel", c.painLevel)
            obj.put("readiness", c.readiness)
            obj.put("habitsCompletedJson", c.habitsCompletedJson)
            obj.put("notes", c.notes)
            obj.put("recordedAt", c.recordedAt)
            checkInsArr.put(obj)
        }
        root.put("dailyCheckIns", checkInsArr)

        // Pain entries
        val painArr = JSONArray()
        data.painEntries.forEach { pe ->
            val obj = JSONObject()
            obj.put("id", pe.id)
            obj.put("profileId", pe.profileId)
            obj.put("date", pe.date)
            obj.put("location", pe.location)
            obj.put("severity", pe.severity)
            obj.put("timing", pe.timing)
            obj.put("exerciseId", pe.exerciseId ?: JSONObject.NULL)
            obj.put("duringWorkout", pe.duringWorkout)
            obj.put("afterWorkout", pe.afterWorkout)
            obj.put("nextDay", pe.nextDay)
            obj.put("effectOnForm", pe.effectOnForm)
            obj.put("status", pe.status)
            obj.put("notes", pe.notes)
            obj.put("recordedAt", pe.recordedAt)
            painArr.put(obj)
        }
        root.put("painEntries", painArr)

        return root.toString(2)
    }

    /**
     * Validates raw JSON string before attempting restore.
     * Prevents corrupted records from overwriting database.
     */
    fun validateBackup(jsonStr: String): BackupValidationResult {
        return try {
            val root = JSONObject(jsonStr)
            val format = root.optString("format")
            if (format != EXPECTED_FORMAT) {
                return BackupValidationResult(
                    isValid = false,
                    formatVersion = format,
                    timestamp = null,
                    profileCount = 0,
                    workoutCount = 0,
                    prCount = 0,
                    setLogCount = 0,
                    previewSummary = "",
                    errorMessage = "فرمت فایل پشتیبان نامعتبر است. انتظار می‌رفت: $EXPECTED_FORMAT اما دریافت شد: '$format'"
                )
            }

            val timestamp = root.optLong("timestamp", System.currentTimeMillis())
            val profilesArr = root.optJSONArray("profiles") ?: JSONArray()
            val sessionsArr = root.optJSONArray("workoutSessions") ?: JSONArray()
            val prsArr = root.optJSONArray("personalRecords") ?: JSONArray()
            val setLogsArr = root.optJSONArray("setLogs") ?: JSONArray()

            val summary = "تعداد پروفایل‌ها: ${profilesArr.length()} | جلسات تمرینی: ${sessionsArr.length()} | رکوردهای شخصی: ${prsArr.length()} | ست‌های ثبت‌شده: ${setLogsArr.length()}"

            BackupValidationResult(
                isValid = true,
                formatVersion = format,
                timestamp = timestamp,
                profileCount = profilesArr.length(),
                workoutCount = sessionsArr.length(),
                prCount = prsArr.length(),
                setLogCount = setLogsArr.length(),
                previewSummary = summary,
                errorMessage = null
            )
        } catch (e: Exception) {
            BackupValidationResult(
                isValid = false,
                formatVersion = null,
                timestamp = null,
                profileCount = 0,
                workoutCount = 0,
                prCount = 0,
                setLogCount = 0,
                previewSummary = "",
                errorMessage = "خطا در تحلیل ساختار JSON فایل پشتیبان: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Parses validated JSON into domain models.
     */
    fun parseBackup(jsonStr: String): FullBackupData {
        val root = JSONObject(jsonStr)
        val format = root.getString("format")
        val timestamp = root.optLong("timestamp", System.currentTimeMillis())
        val appVersion = root.optString("appVersion", "1.0")

        val profiles = mutableListOf<ProfileEntity>()
        val profilesArr = root.optJSONArray("profiles") ?: JSONArray()
        for (i in 0 until profilesArr.length()) {
            val o = profilesArr.getJSONObject(i)
            profiles.add(
                ProfileEntity(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    age = o.getInt("age"),
                    heightCm = o.getDouble("heightCm").toFloat(),
                    weightKg = o.getDouble("weightKg").toFloat(),
                    gender = o.getString("gender"),
                    waistCm = if (o.isNull("waistCm")) null else o.getDouble("waistCm").toFloat(),
                    activityLevel = o.optString("activityLevel", "MODERATE"),
                    experienceLevel = o.optString("experienceLevel", "BEGINNER"),
                    goalsJson = o.optString("goalsJson", "[]"),
                    equipmentJson = o.optString("equipmentJson", "[]"),
                    environment = o.optString("environment", "HOME"),
                    trainingDaysPerWeek = o.optInt("trainingDaysPerWeek", 3),
                    preferredTimeOfDay = o.optString("preferredTimeOfDay", "EVENING"),
                    limitationsNotes = o.optString("limitationsNotes", ""),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }

        val programs = mutableListOf<ProgramEntity>()
        val programsArr = root.optJSONArray("programs") ?: JSONArray()
        for (i in 0 until programsArr.length()) {
            val o = programsArr.getJSONObject(i)
            programs.add(
                ProgramEntity(
                    id = o.getString("id"),
                    profileId = o.getString("profileId"),
                    title = o.getString("title"),
                    description = o.optString("description", ""),
                    level = o.optString("level", "BEGINNER"),
                    daysPerWeek = o.optInt("daysPerWeek", 3),
                    status = o.optString("status", "ACTIVE"),
                    currentVersionNumber = o.optInt("currentVersionNumber", 1),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }

        val programVersions = mutableListOf<ProgramVersionEntity>()
        val versionsArr = root.optJSONArray("programVersions") ?: JSONArray()
        for (i in 0 until versionsArr.length()) {
            val o = versionsArr.getJSONObject(i)
            programVersions.add(
                ProgramVersionEntity(
                    id = o.getString("id"),
                    programId = o.getString("programId"),
                    versionNumber = o.getInt("versionNumber"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    reason = o.optString("reason", "نسخه"),
                    changesSummary = o.optString("changesSummary", ""),
                    programStructureJson = o.optString("programStructureJson", "{}"),
                    previousVersionNumber = if (o.isNull("previousVersionNumber")) null else o.getInt("previousVersionNumber")
                )
            )
        }

        val workoutSessions = mutableListOf<WorkoutSessionEntity>()
        val sessionsArr = root.optJSONArray("workoutSessions") ?: JSONArray()
        for (i in 0 until sessionsArr.length()) {
            val o = sessionsArr.getJSONObject(i)
            workoutSessions.add(
                WorkoutSessionEntity(
                    id = o.getString("id"),
                    profileId = o.getString("profileId"),
                    programId = if (o.isNull("programId")) null else o.getString("programId"),
                    title = o.getString("title"),
                    scheduledDate = o.getString("scheduledDate"),
                    completedDate = if (o.isNull("completedDate")) null else o.getString("completedDate"),
                    status = o.optString("status", "PLANNED"),
                    totalDurationSec = o.optInt("totalDurationSec", 0),
                    overallRpe = if (o.isNull("overallRpe")) null else o.getInt("overallRpe"),
                    painFlag = o.optBoolean("painFlag", false),
                    notes = o.optString("notes", ""),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        val workoutExercises = mutableListOf<WorkoutExerciseEntity>()
        val exercisesArr = root.optJSONArray("workoutExercises") ?: JSONArray()
        for (i in 0 until exercisesArr.length()) {
            val o = exercisesArr.getJSONObject(i)
            workoutExercises.add(
                WorkoutExerciseEntity(
                    id = o.getString("id"),
                    sessionId = o.getString("sessionId"),
                    exerciseId = o.getString("exerciseId"),
                    orderIndex = o.optInt("orderIndex", 0),
                    targetSets = o.optInt("targetSets", 3),
                    targetReps = o.optInt("targetReps", 8),
                    targetHoldSec = o.optInt("targetHoldSec", 0),
                    targetRpe = o.optInt("targetRpe", 7),
                    restSec = o.optInt("restSec", 90),
                    notes = o.optString("notes", "")
                )
            )
        }

        val setLogs = mutableListOf<SetLogEntity>()
        val setLogsArr = root.optJSONArray("setLogs") ?: JSONArray()
        for (i in 0 until setLogsArr.length()) {
            val o = setLogsArr.getJSONObject(i)
            setLogs.add(
                SetLogEntity(
                    id = o.getString("id"),
                    workoutExerciseId = o.getString("workoutExerciseId"),
                    sessionId = o.getString("sessionId"),
                    profileId = o.getString("profileId"),
                    exerciseId = o.getString("exerciseId"),
                    setNumber = o.getInt("setNumber"),
                    reps = o.getInt("reps"),
                    holdSeconds = o.optInt("holdSeconds", 0),
                    addedWeightKg = o.optDouble("addedWeightKg", 0.0).toFloat(),
                    rpe = o.optInt("rpe", 7),
                    rir = o.optInt("rir", 2),
                    status = o.optString("status", "COMPLETED"),
                    painSeverity = o.optInt("painSeverity", 0),
                    painLocation = o.optString("painLocation", ""),
                    notes = o.optString("notes", ""),
                    timestamp = o.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }

        val skillProgress = mutableListOf<SkillProgressEntity>()
        val skillProgArr = root.optJSONArray("skillProgress") ?: JSONArray()
        for (i in 0 until skillProgArr.length()) {
            val o = skillProgArr.getJSONObject(i)
            skillProgress.add(
                SkillProgressEntity(
                    id = o.getString("id"),
                    profileId = o.getString("profileId"),
                    skillId = o.getString("skillId"),
                    currentStepIndex = o.optInt("currentStepIndex", 0),
                    bestHoldSec = o.optInt("bestHoldSec", 0),
                    bestReps = o.optInt("bestReps", 0),
                    qualityScore = o.optInt("qualityScore", 8),
                    lastTrainedDate = o.optLong("lastTrainedDate", System.currentTimeMillis()),
                    notes = o.optString("notes", "")
                )
            )
        }

        val metrics = mutableListOf<MetricEntity>()
        val metricsArr = root.optJSONArray("metrics") ?: JSONArray()
        for (i in 0 until metricsArr.length()) {
            val o = metricsArr.getJSONObject(i)
            metrics.add(
                MetricEntity(
                    id = o.getString("id"),
                    profileId = o.getString("profileId"),
                    metricType = o.getString("metricType"),
                    value = o.getDouble("value").toFloat(),
                    unit = o.getString("unit"),
                    notes = o.optString("notes", ""),
                    recordedAt = o.optLong("recordedAt", System.currentTimeMillis())
                )
            )
        }

        val personalRecords = mutableListOf<PREntity>()
        val prsArr = root.optJSONArray("personalRecords") ?: JSONArray()
        for (i in 0 until prsArr.length()) {
            val o = prsArr.getJSONObject(i)
            personalRecords.add(
                PREntity(
                    id = o.getString("id"),
                    profileId = o.getString("profileId"),
                    exerciseOrSkillId = o.getString("exerciseOrSkillId"),
                    title = o.getString("title"),
                    prType = o.getString("prType"),
                    value = o.getDouble("value").toFloat(),
                    unit = o.getString("unit"),
                    date = o.optLong("date", System.currentTimeMillis()),
                    conditionsNote = o.optString("conditionsNote", "")
                )
            )
        }

        val recoveryEntries = mutableListOf<RecoveryEntryEntity>()
        val recoveryArr = root.optJSONArray("recoveryEntries") ?: JSONArray()
        for (i in 0 until recoveryArr.length()) {
            val o = recoveryArr.getJSONObject(i)
            recoveryEntries.add(
                RecoveryEntryEntity(
                    id = o.getString("id"),
                    profileId = o.getString("profileId"),
                    date = o.getString("date"),
                    sleepDurationHours = o.getDouble("sleepDurationHours").toFloat(),
                    sleepQuality = o.getInt("sleepQuality"),
                    energyLevel = o.getInt("energyLevel"),
                    fatigueLevel = o.getInt("fatigueLevel"),
                    muscleSoreness = o.getInt("muscleSoreness"),
                    stressLevel = o.getInt("stressLevel"),
                    readiness = o.optString("readiness", "GREEN"),
                    notes = o.optString("notes", ""),
                    recordedAt = o.optLong("recordedAt", System.currentTimeMillis())
                )
            )
        }

        val nutritionEntries = mutableListOf<NutritionEntryEntity>()
        val nutritionArr = root.optJSONArray("nutritionEntries") ?: JSONArray()
        for (i in 0 until nutritionArr.length()) {
            val o = nutritionArr.getJSONObject(i)
            nutritionEntries.add(
                NutritionEntryEntity(
                    id = o.getString("id"),
                    profileId = o.getString("profileId"),
                    date = o.getString("date"),
                    mealType = o.getString("mealType"),
                    foodName = o.getString("foodName"),
                    calories = o.optInt("calories", 0),
                    proteinG = o.optDouble("proteinG", 0.0).toFloat(),
                    carbsG = o.optDouble("carbsG", 0.0).toFloat(),
                    fatG = o.optDouble("fatG", 0.0).toFloat(),
                    waterMl = o.optInt("waterMl", 0),
                    notes = o.optString("notes", ""),
                    recordedAt = o.optLong("recordedAt", System.currentTimeMillis())
                )
            )
        }

        val dailyCheckIns = mutableListOf<DailyCheckInEntity>()
        val checkInsArr = root.optJSONArray("dailyCheckIns") ?: JSONArray()
        for (i in 0 until checkInsArr.length()) {
            val o = checkInsArr.getJSONObject(i)
            dailyCheckIns.add(
                DailyCheckInEntity(
                    id = o.getString("id"),
                    profileId = o.getString("profileId"),
                    date = o.getString("date"),
                    sleepHours = o.getDouble("sleepHours").toFloat(),
                    energy = o.getInt("energy"),
                    fatigue = o.getInt("fatigue"),
                    soreness = o.getInt("soreness"),
                    painLevel = o.optInt("painLevel", 0),
                    readiness = o.optString("readiness", "GREEN"),
                    habitsCompletedJson = o.optString("habitsCompletedJson", "[]"),
                    notes = o.optString("notes", ""),
                    recordedAt = o.optLong("recordedAt", System.currentTimeMillis())
                )
            )
        }

        val painEntries = mutableListOf<PainEntryEntity>()
        val painArr = root.optJSONArray("painEntries") ?: JSONArray()
        for (i in 0 until painArr.length()) {
            val o = painArr.getJSONObject(i)
            painEntries.add(
                PainEntryEntity(
                    id = o.getString("id"),
                    profileId = o.getString("profileId"),
                    date = o.getString("date"),
                    location = o.getString("location"),
                    severity = o.getInt("severity"),
                    timing = o.getString("timing"),
                    exerciseId = if (o.isNull("exerciseId")) null else o.getString("exerciseId"),
                    duringWorkout = o.optBoolean("duringWorkout", false),
                    afterWorkout = o.optBoolean("afterWorkout", false),
                    nextDay = o.optBoolean("nextDay", false),
                    effectOnForm = o.optBoolean("effectOnForm", false),
                    status = o.optString("status", "GREEN"),
                    notes = o.optString("notes", ""),
                    recordedAt = o.optLong("recordedAt", System.currentTimeMillis())
                )
            )
        }

        return FullBackupData(
            format = format,
            timestamp = timestamp,
            appVersion = appVersion,
            profiles = profiles,
            programs = programs,
            programVersions = programVersions,
            workoutSessions = workoutSessions,
            workoutExercises = workoutExercises,
            setLogs = setLogs,
            skillProgress = skillProgress,
            metrics = metrics,
            personalRecords = personalRecords,
            nutritionEntries = nutritionEntries,
            recoveryEntries = recoveryEntries,
            dailyCheckIns = dailyCheckIns,
            painEntries = painEntries
        )
    }
}
