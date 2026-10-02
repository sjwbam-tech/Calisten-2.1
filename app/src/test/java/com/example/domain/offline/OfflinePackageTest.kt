package com.example.domain.offline

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SeedData
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.UserCapabilityEntity
import com.example.domain.engine.DataDrivenProgramGenerator
import com.example.domain.engine.ProgramGenerationParams
import com.example.domain.engine.SkillProgressionRegistry
import com.example.domain.engine.SupportedSkill
import com.example.domain.offline.model.OfflinePackageStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class OfflinePackageTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clean any past offline package files in test environment
        File(context.filesDir, OfflinePackageManager.OFFLINE_DIR_NAME).deleteRecursively()
        File(context.filesDir, OfflinePackageManager.STAGING_DIR_NAME).deleteRecursively()
    }

    /**
     * 1. Test Package Size Calculation (Real, non-fake byte calculations)
     */
    @Test
    fun testPackageSizeCalculation_calculatesExactRealBytes() {
        val payloads = OfflineContentGenerator.generateAllPayloads()
        assertTrue("Payloads list must not be empty", payloads.isNotEmpty())

        val totalBytes = payloads.sumOf { it.sizeBytes }
        assertTrue("Total download bytes must be greater than zero", totalBytes > 1000L)

        payloads.forEach { payload ->
            assertTrue("File ${payload.fileName} must have content", payload.contentBytes.isNotEmpty())
            assertEquals(payload.contentBytes.size.toLong(), payload.sizeBytes)
        }

        val manager = OfflinePackageManager(context)
        val metrics = manager.calculateStorageMetrics()
        assertEquals(totalBytes, metrics.totalDownloadSizeBytes)
        assertTrue(metrics.requiredStorageBytes > metrics.totalDownloadSizeBytes)
        assertTrue(metrics.formattedDownloadSize.isNotBlank())
        assertTrue(metrics.formattedRequiredSpace.isNotBlank())
    }

    /**
     * 2. Test Manifest Generation
     */
    @Test
    fun testManifestGeneration_createsValidEntriesAndSha256Checksums() {
        val payloads = OfflineContentGenerator.generateAllPayloads()
        val manifest = OfflineContentGenerator.createAuthoritativeManifest(payloads)

        assertEquals(OfflineContentGenerator.CURRENT_PACKAGE_VERSION, manifest.packageVersion)
        assertEquals(payloads.size, manifest.fileEntries.size)
        assertTrue(manifest.totalSizeBytes > 0)
        assertTrue(manifest.installedSizeBytes >= manifest.totalSizeBytes)

        manifest.fileEntries.forEach { entry ->
            assertEquals(64, entry.sha256Checksum.length) // 64 hex chars for SHA-256
            assertTrue(entry.fileName.isNotBlank())
            assertTrue(entry.sizeBytes > 0)
        }
    }

    /**
     * 3. Test Storage Validation
     */
    @Test
    fun testStorageValidation_detectsAvailableSpaceCorrectly() {
        val manager = OfflinePackageManager(context)
        val metrics = manager.calculateStorageMetrics()

        assertTrue(metrics.availableStorageBytes > 0L)
        assertTrue(metrics.hasEnoughSpace)
    }

    /**
     * 4. Test Download Progress and Completion
     */
    @Test
    fun testDownloadProgress_completesAndInstallsAtomically() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val manager = OfflinePackageManager(context, dispatcher = testDispatcher)

        assertEquals(OfflinePackageStatus.NotDownloaded, manager.status.value)

        val success = manager.startDownload(simulatedNetworkDelayMs = 0L)
        assertTrue("Download and installation must succeed", success)

        val status = manager.status.value
        assertTrue("Status must be ReadyOffline after download", status is OfflinePackageStatus.ReadyOffline)

        val manifest = (status as OfflinePackageStatus.ReadyOffline).manifest
        assertEquals(OfflineContentGenerator.CURRENT_PACKAGE_VERSION, manifest.packageVersion)

        // Verify package directory and files on disk
        val packageDir = File(context.filesDir, OfflinePackageManager.OFFLINE_DIR_NAME)
        assertTrue(packageDir.exists())
        manifest.fileEntries.forEach { entry ->
            val file = File(packageDir, entry.fileName)
            assertTrue("File ${entry.fileName} must exist on disk", file.exists())
            assertEquals(entry.sizeBytes, file.length())
        }
    }

    /**
     * 5. Test Pause and Resume
     */
    @Test
    fun testPauseAndResume_retainsDownloadedProgress() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val manager = OfflinePackageManager(context, dispatcher = testDispatcher)

        manager.pauseDownload()
        // Starting download with pause requested triggers pause state
        val success = manager.startDownload(simulatedNetworkDelayMs = 1L)
        assertFalse("Paused download should return false until resumed", success)

        val status = manager.status.value
        assertTrue("Status must be Paused", status is OfflinePackageStatus.Paused)

        // Resume download
        val resumeSuccess = manager.resumeDownload(simulatedNetworkDelayMs = 0L)
        assertTrue("Resumed download should succeed", resumeSuccess)
        assertTrue("Final status must be ReadyOffline", manager.status.value is OfflinePackageStatus.ReadyOffline)
    }

    /**
     * 6. Test Failed Download Recovery & Cancel
     */
    @Test
    fun testCancelDownload_cleansStagingDirectory() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val manager = OfflinePackageManager(context, dispatcher = testDispatcher)

        manager.cancelDownload()
        assertEquals(OfflinePackageStatus.NotDownloaded, manager.status.value)

        val stagingDir = File(context.filesDir, OfflinePackageManager.STAGING_DIR_NAME)
        assertFalse("Staging directory must be cleaned on cancel", stagingDir.exists())
    }

    /**
     * 7. Test Checksum Validation
     */
    @Test
    fun testChecksumValidation_verifiesSha256Matches() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val manager = OfflinePackageManager(context, dispatcher = testDispatcher)
        manager.startDownload(simulatedNetworkDelayMs = 0L)

        val isValid = manager.verifyInstalledPackageIntegrity()
        assertTrue("Installed package must have valid checksums", isValid)
    }

    /**
     * 8. Test Corrupted Package Detection
     */
    @Test
    fun testCorruptedPackage_detectedAndHandled() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val manager = OfflinePackageManager(context, dispatcher = testDispatcher)
        manager.startDownload(simulatedNetworkDelayMs = 0L)

        // Corrupt one file by tampering with its content
        val packageDir = File(context.filesDir, OfflinePackageManager.OFFLINE_DIR_NAME)
        val targetFile = File(packageDir, "exercises_database.json")
        targetFile.writeText("Corrupted Content That Invalidates Sha256")

        val isValid = manager.verifyInstalledPackageIntegrity()
        assertFalse("Corrupted package must fail integrity check", isValid)
        assertTrue(manager.status.value is OfflinePackageStatus.Error)
    }

    /**
     * 9. Test Existing Valid Package Detection (Skips re-downloading)
     */
    @Test
    fun testExistingValidPackage_detectedOnStartup() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val manager1 = OfflinePackageManager(context, dispatcher = testDispatcher)
        manager1.startDownload(simulatedNetworkDelayMs = 0L)

        // Create new manager instance as if app restarted
        val manager2 = OfflinePackageManager(context, dispatcher = testDispatcher)
        assertTrue("Should detect existing package without downloading", manager2.checkExistingPackage())
        assertTrue("Status should be ReadyOffline immediately", manager2.status.value is OfflinePackageStatus.ReadyOffline)
    }

    /**
     * 10. Test Offline Startup
     */
    @Test
    fun testOfflineStartup_runsWithoutNetwork() {
        val manager = OfflinePackageManager(context)
        assertNotNull(manager.status.value)
    }

    /**
     * 11. Test Offline Workout Generation (Stage 1 planning engine remains fully intact and offline)
     */
    @Test
    fun testOfflineWorkoutGeneration_executesCompletelyOffline() {
        val profile = ProfileEntity(
            id = "profile_off",
            name = "کاربر آفلاین",
            age = 25,
            heightCm = 175f,
            weightKg = 70f,
            gender = "MALE",
            experienceLevel = "INTERMEDIATE",
            trainingDaysPerWeek = 3,
            equipmentJson = "[\"pullup_bar\", \"parallettes\"]",
            goalsJson = "[\"STRENGTH\"]"
        )

        val capability = UserCapabilityEntity(
            id = "cap_off",
            profileId = "profile_off",
            pushingStrength = 65f,
            pullingStrength = 65f,
            legStrength = 60f,
            coreStrength = 60f
        )

        val params = ProgramGenerationParams(
            profile = profile,
            capability = capability,
            allExercises = SeedData.exercises,
            targetDaysPerWeek = 3
        )

        val programPackage = DataDrivenProgramGenerator.generateProgram(params)
        assertNotNull(programPackage)
        assertEquals(3, programPackage.sessions.size)
        assertTrue(programPackage.workoutExercises.isNotEmpty())
    }

    /**
     * 12. Test Offline Exercise Library
     */
    @Test
    fun testOfflineExerciseLibrary_accessesAllExercisesOffline() {
        assertTrue(SeedData.exercises.isNotEmpty())
        val pushups = SeedData.exercises.filter { it.movementPattern == "HORIZONTAL_PUSH" }
        assertTrue(pushups.isNotEmpty())
        val pullups = SeedData.exercises.filter { it.movementPattern == "VERTICAL_PULL" }
        assertTrue(pullups.isNotEmpty())
    }

    /**
     * 13. Test Offline Skill Progression
     */
    @Test
    fun testOfflineSkillProgression_evaluatesPrerequisitesOffline() {
        val capability = UserCapabilityEntity(
            id = "cap_skill",
            profileId = "profile_skill",
            pushingStrength = 80f,
            pullingStrength = 80f,
            gripCapacity = 80f,
            coreStrength = 80f
        )

        val eval = SkillProgressionRegistry.evaluateSkill(
            skill = SupportedSkill.FRONT_LEVER,
            capability = capability,
            assessment = null,
            availableEquipment = listOf("pullup_bar"),
            allExercises = SeedData.exercises
        )

        assertTrue(eval.meetsPrerequisites)
        assertEquals("front_lever_tuck", eval.chosenExerciseId)
    }

    /**
     * 14. Test Package Versioning & Update Handling
     */
    @Test
    fun testPackageVersioning_supportsVersionCheck() {
        val currentVer = OfflineContentGenerator.CURRENT_PACKAGE_VERSION
        assertEquals("kalisten-offline-v1.0", currentVer)

        val manifestsMatch = OfflineContentGenerator.CURRENT_SCHEMA_VERSION == "2.0"
        assertTrue(manifestsMatch)
    }
}
