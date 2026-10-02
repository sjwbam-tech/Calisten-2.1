package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AppSettingDao
import com.example.data.local.dao.AssessmentDao
import com.example.data.local.dao.ExerciseDao
import com.example.data.local.dao.MetricDao
import com.example.data.local.dao.NutritionDao
import com.example.data.local.dao.PRDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.ProgramDao
import com.example.data.local.dao.ProgramVersionDao
import com.example.data.local.dao.RecoveryDao
import com.example.data.local.dao.ScienceDao
import com.example.data.local.dao.SkillDao
import com.example.data.local.dao.WorkoutDao
import com.example.data.local.dao.ExerciseRelationshipDao
import com.example.data.local.dao.UserCapabilityDao
import com.example.data.local.dao.AdaptationDecisionDao
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProfileEntity::class,
        AssessmentEntity::class,
        ExerciseEntity::class,
        ExerciseRelationshipEntity::class,
        UserCapabilityEntity::class,
        AdaptationDecisionEntity::class,
        ProgramEntity::class,
        ProgramVersionEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        SetLogEntity::class,
        SkillEntity::class,
        SkillProgressEntity::class,
        MetricEntity::class,
        PREntity::class,
        NutritionEntryEntity::class,
        RecoveryEntryEntity::class,
        DailyCheckInEntity::class,
        PainEntryEntity::class,
        ScienceSourceEntity::class,
        AppSettingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseRelationshipDao(): ExerciseRelationshipDao
    abstract fun userCapabilityDao(): UserCapabilityDao
    abstract fun adaptationDecisionDao(): AdaptationDecisionDao
    abstract fun programDao(): ProgramDao
    abstract fun programVersionDao(): ProgramVersionDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun skillDao(): SkillDao
    abstract fun metricDao(): MetricDao
    abstract fun prDao(): PRDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun recoveryDao(): RecoveryDao
    abstract fun scienceDao(): ScienceDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kalisten_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate only non-user content: Exercises, Skills, Science sources
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                database.exerciseDao().insertExercises(SeedData.exercises)
                                database.skillDao().insertSkills(SeedData.skills)
                                database.scienceDao().insertSources(SeedData.scienceSources)
                                database.appSettingDao().setSetting(
                                    AppSettingEntity("science_pack_version", "v1.0")
                                )
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
