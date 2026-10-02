package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.engine.BackupRestoreManager
import com.example.domain.engine.DecisionType
import com.example.domain.engine.PlateauEngine
import com.example.domain.engine.ProgressionEngine
import com.example.data.local.SeedData
import com.example.data.local.entity.SetLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Persian app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("کالیستن", appName)
    }

    @Test
    fun `backup manager validates valid format correctly`() {
        val validJson = """
            {
                "format": "kalisten-backup-v1",
                "timestamp": 1700000000000,
                "profiles": [],
                "workoutSessions": [],
                "personalRecords": [],
                "setLogs": []
            }
        """.trimIndent()
        val result = BackupRestoreManager.validateBackup(validJson)
        assertTrue(result.isValid)
        assertEquals("kalisten-backup-v1", result.formatVersion)
    }

    @Test
    fun `backup manager rejects corrupted or invalid schema`() {
        val invalidJson = """{"invalid": "json"}"""
        val result = BackupRestoreManager.validateBackup(invalidJson)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }
}
