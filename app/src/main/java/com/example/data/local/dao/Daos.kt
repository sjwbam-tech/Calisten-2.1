package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import com.example.data.local.entity.ExerciseRelationshipEntity
import com.example.data.local.entity.UserCapabilityEntity
import com.example.data.local.entity.AdaptationDecisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getProfileById(id: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileByIdDirect(id: String): ProfileEntity?

    @Query("SELECT * FROM profiles")
    suspend fun getAllProfilesDirect(): List<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)
}

@Dao
interface AssessmentDao {
    @Query("SELECT * FROM assessments WHERE profileId = :profileId ORDER BY recordedAt DESC LIMIT 1")
    fun getAssessmentForProfile(profileId: String): Flow<AssessmentEntity?>

    @Query("SELECT * FROM assessments WHERE profileId = :profileId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getAssessmentForProfileDirect(profileId: String): AssessmentEntity?

    @Query("SELECT * FROM assessments")
    suspend fun getAllAssessmentsDirect(): List<AssessmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: AssessmentEntity)
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY persianName ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE category = :category ORDER BY persianName ASC")
    fun getExercisesByCategory(category: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    fun getExerciseById(id: String): Flow<ExerciseEntity?>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getExerciseByIdDirect(id: String): ExerciseEntity?

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercisesDirect(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExercise(id: String)
}

@Dao
interface ProgramDao {
    @Query("SELECT * FROM programs WHERE profileId = :profileId ORDER BY updatedAt DESC")
    fun getProgramsForProfile(profileId: String): Flow<List<ProgramEntity>>

    @Query("SELECT * FROM programs WHERE profileId = :profileId AND status = 'ACTIVE' LIMIT 1")
    fun getActiveProgramForProfile(profileId: String): Flow<ProgramEntity?>

    @Query("SELECT * FROM programs WHERE id = :id LIMIT 1")
    fun getProgramById(id: String): Flow<ProgramEntity?>

    @Query("SELECT * FROM programs WHERE id = :id LIMIT 1")
    suspend fun getProgramByIdDirect(id: String): ProgramEntity?

    @Query("SELECT * FROM programs")
    suspend fun getAllProgramsDirect(): List<ProgramEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: ProgramEntity)

    @Update
    suspend fun updateProgram(program: ProgramEntity)

    @Query("DELETE FROM programs WHERE id = :id")
    suspend fun deleteProgram(id: String)
}

@Dao
interface ProgramVersionDao {
    @Query("SELECT * FROM program_versions WHERE programId = :programId ORDER BY versionNumber DESC")
    fun getVersionsForProgram(programId: String): Flow<List<ProgramVersionEntity>>

    @Query("SELECT * FROM program_versions WHERE programId = :programId AND versionNumber = :versionNumber LIMIT 1")
    suspend fun getVersion(programId: String, versionNumber: Int): ProgramVersionEntity?

    @Query("SELECT * FROM program_versions")
    suspend fun getAllVersionsDirect(): List<ProgramVersionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: ProgramVersionEntity)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions WHERE profileId = :profileId ORDER BY scheduledDate DESC, createdAt DESC")
    fun getSessionsForProfile(profileId: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    fun getSessionById(id: String): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionByIdDirect(id: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE profileId = :profileId AND status = 'IN_PROGRESS' LIMIT 1")
    fun getInProgressSession(profileId: String): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions")
    suspend fun getAllSessionsDirect(): List<WorkoutSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    @Query("SELECT * FROM workout_exercises WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getExercisesForSession(sessionId: String): Flow<List<WorkoutExerciseEntity>>

    @Query("SELECT * FROM workout_exercises WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    suspend fun getExercisesForSessionDirect(sessionId: String): List<WorkoutExerciseEntity>

    @Query("SELECT * FROM workout_exercises")
    suspend fun getAllWorkoutExercisesDirect(): List<WorkoutExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(exercises: List<WorkoutExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(exercise: WorkoutExerciseEntity)

    @Query("SELECT * FROM workout_exercises WHERE id = :id LIMIT 1")
    suspend fun getWorkoutExerciseByIdDirect(id: String): WorkoutExerciseEntity?

    @Query("SELECT * FROM workout_sessions WHERE profileId = :profileId AND status = 'PLANNED' ORDER BY scheduledDate ASC")
    suspend fun getPlannedSessionsForProfileDirect(profileId: String): List<WorkoutSessionEntity>

    @Query("DELETE FROM workout_exercises WHERE id = :id")
    suspend fun deleteWorkoutExercise(id: String)

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getSetLogsForSession(sessionId: String): Flow<List<SetLogEntity>>

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getSetLogsForSessionDirect(sessionId: String): List<SetLogEntity>

    @Query("SELECT * FROM set_logs WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getSetLogsForProfile(profileId: String): Flow<List<SetLogEntity>>

    @Query("SELECT * FROM set_logs WHERE profileId = :profileId ORDER BY timestamp DESC")
    suspend fun getSetLogsForProfileDirect(profileId: String): List<SetLogEntity>

    @Query("SELECT * FROM set_logs WHERE profileId = :profileId AND exerciseId = :exerciseId ORDER BY timestamp ASC")
    fun getSetLogsForExercise(profileId: String, exerciseId: String): Flow<List<SetLogEntity>>

    @Query("SELECT * FROM set_logs WHERE profileId = :profileId AND exerciseId = :exerciseId ORDER BY timestamp ASC")
    suspend fun getSetLogsForExerciseDirect(profileId: String, exerciseId: String): List<SetLogEntity>

    @Query("SELECT * FROM set_logs")
    suspend fun getAllSetLogsDirect(): List<SetLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetLog(setLog: SetLogEntity)

    @Update
    suspend fun updateSetLog(setLog: SetLogEntity)

    @Query("DELETE FROM set_logs WHERE id = :id")
    suspend fun deleteSetLog(id: String)
}

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY persianName ASC")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills")
    suspend fun getAllSkillsDirect(): List<SkillEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Query("SELECT * FROM skill_progress WHERE profileId = :profileId")
    fun getProgressForProfile(profileId: String): Flow<List<SkillProgressEntity>>

    @Query("SELECT * FROM skill_progress WHERE profileId = :profileId AND skillId = :skillId LIMIT 1")
    fun getSkillProgress(profileId: String, skillId: String): Flow<SkillProgressEntity?>

    @Query("SELECT * FROM skill_progress WHERE profileId = :profileId AND skillId = :skillId LIMIT 1")
    suspend fun getSkillProgressDirect(profileId: String, skillId: String): SkillProgressEntity?

    @Query("SELECT * FROM skill_progress")
    suspend fun getAllSkillProgressDirect(): List<SkillProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSkillProgress(progress: SkillProgressEntity)
}

@Dao
interface MetricDao {
    @Query("SELECT * FROM metrics WHERE profileId = :profileId AND metricType = :metricType ORDER BY recordedAt ASC")
    fun getMetricsForProfile(profileId: String, metricType: String): Flow<List<MetricEntity>>

    @Query("SELECT * FROM metrics WHERE profileId = :profileId ORDER BY recordedAt DESC")
    fun getAllMetricsForProfile(profileId: String): Flow<List<MetricEntity>>

    @Query("SELECT * FROM metrics")
    suspend fun getAllMetricsDirect(): List<MetricEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(metric: MetricEntity)
}

@Dao
interface PRDao {
    @Query("SELECT * FROM personal_records WHERE profileId = :profileId ORDER BY date DESC")
    fun getPRsForProfile(profileId: String): Flow<List<PREntity>>

    @Query("SELECT * FROM personal_records WHERE profileId = :profileId AND exerciseOrSkillId = :exerciseOrSkillId ORDER BY value DESC LIMIT 1")
    suspend fun getPRForExerciseDirect(profileId: String, exerciseOrSkillId: String): PREntity?

    @Query("SELECT * FROM personal_records")
    suspend fun getAllPRsDirect(): List<PREntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPR(pr: PREntity)
}

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition_entries WHERE profileId = :profileId AND date = :date ORDER BY recordedAt ASC")
    fun getEntriesForDate(profileId: String, date: String): Flow<List<NutritionEntryEntity>>

    @Query("SELECT * FROM nutrition_entries WHERE profileId = :profileId ORDER BY date DESC, recordedAt DESC")
    fun getAllEntriesForProfile(profileId: String): Flow<List<NutritionEntryEntity>>

    @Query("SELECT * FROM nutrition_entries")
    suspend fun getAllNutritionDirect(): List<NutritionEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrition(entry: NutritionEntryEntity)

    @Query("DELETE FROM nutrition_entries WHERE id = :id")
    suspend fun deleteNutrition(id: String)
}

@Dao
interface RecoveryDao {
    @Query("SELECT * FROM recovery_entries WHERE profileId = :profileId ORDER BY date DESC")
    fun getRecoveryForProfile(profileId: String): Flow<List<RecoveryEntryEntity>>

    @Query("SELECT * FROM recovery_entries WHERE profileId = :profileId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentRecoveryForProfileDirect(profileId: String, limit: Int = 10): List<RecoveryEntryEntity>

    @Query("SELECT * FROM recovery_entries WHERE profileId = :profileId AND date = :date LIMIT 1")
    fun getRecoveryForDate(profileId: String, date: String): Flow<RecoveryEntryEntity?>

    @Query("SELECT * FROM recovery_entries WHERE profileId = :profileId AND date = :date LIMIT 1")
    suspend fun getRecoveryForDateDirect(profileId: String, date: String): RecoveryEntryEntity?

    @Query("SELECT * FROM recovery_entries")
    suspend fun getAllRecoveryDirect(): List<RecoveryEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecovery(entry: RecoveryEntryEntity)

    @Query("SELECT * FROM daily_checkins WHERE profileId = :profileId ORDER BY date DESC")
    fun getDailyCheckIns(profileId: String): Flow<List<DailyCheckInEntity>>

    @Query("SELECT * FROM daily_checkins")
    suspend fun getAllCheckInsDirect(): List<DailyCheckInEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: DailyCheckInEntity)

    @Query("SELECT * FROM pain_entries WHERE profileId = :profileId ORDER BY date DESC")
    fun getPainEntries(profileId: String): Flow<List<PainEntryEntity>>

    @Query("SELECT * FROM pain_entries WHERE profileId = :profileId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentPainForProfileDirect(profileId: String, limit: Int = 10): List<PainEntryEntity>

    @Query("SELECT * FROM pain_entries")
    suspend fun getAllPainEntriesDirect(): List<PainEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPainEntry(entry: PainEntryEntity)
}

@Dao
interface ScienceDao {
    @Query("SELECT * FROM science_sources ORDER BY topic ASC, publicationYear DESC")
    fun getAllSources(): Flow<List<ScienceSourceEntity>>

    @Query("SELECT * FROM science_sources")
    suspend fun getAllSourcesDirect(): List<ScienceSourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<ScienceSourceEntity>)
}

@Dao
interface AppSettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    fun getSetting(key: String): Flow<String?>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingDirect(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(entity: AppSettingEntity)
}

@Dao
interface ExerciseRelationshipDao {
    @Query("SELECT * FROM exercise_relationships WHERE sourceExerciseId = :sourceId")
    fun getRelationshipsForSource(sourceId: String): Flow<List<ExerciseRelationshipEntity>>

    @Query("SELECT * FROM exercise_relationships WHERE sourceExerciseId = :sourceId")
    suspend fun getRelationshipsForSourceDirect(sourceId: String): List<ExerciseRelationshipEntity>

    @Query("SELECT * FROM exercise_relationships WHERE targetExerciseId = :targetId")
    suspend fun getRelationshipsForTargetDirect(targetId: String): List<ExerciseRelationshipEntity>

    @Query("SELECT * FROM exercise_relationships")
    suspend fun getAllRelationshipsDirect(): List<ExerciseRelationshipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationships(relationships: List<ExerciseRelationshipEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: ExerciseRelationshipEntity)

    @Query("DELETE FROM exercise_relationships WHERE id = :id")
    suspend fun deleteRelationship(id: String)
}

@Dao
interface UserCapabilityDao {
    @Query("SELECT * FROM user_capabilities WHERE profileId = :profileId LIMIT 1")
    fun getCapabilityForProfile(profileId: String): Flow<UserCapabilityEntity?>

    @Query("SELECT * FROM user_capabilities WHERE profileId = :profileId LIMIT 1")
    suspend fun getCapabilityForProfileDirect(profileId: String): UserCapabilityEntity?

    @Query("SELECT * FROM user_capabilities")
    suspend fun getAllCapabilitiesDirect(): List<UserCapabilityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapability(capability: UserCapabilityEntity)
}

@Dao
interface AdaptationDecisionDao {
    @Query("SELECT * FROM adaptation_decisions WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getDecisionsForProfile(profileId: String): Flow<List<AdaptationDecisionEntity>>

    @Query("SELECT * FROM adaptation_decisions WHERE programId = :programId ORDER BY timestamp DESC")
    fun getDecisionsForProgram(programId: String): Flow<List<AdaptationDecisionEntity>>

    @Query("SELECT * FROM adaptation_decisions WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    suspend fun getDecisionsForSessionDirect(sessionId: String): List<AdaptationDecisionEntity>

    @Query("SELECT * FROM adaptation_decisions WHERE profileId = :profileId ORDER BY timestamp DESC LIMIT 20")
    suspend fun getRecentDecisionsForProfileDirect(profileId: String): List<AdaptationDecisionEntity>

    @Query("SELECT * FROM adaptation_decisions")
    suspend fun getAllDecisionsDirect(): List<AdaptationDecisionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecision(decision: AdaptationDecisionEntity)
}
