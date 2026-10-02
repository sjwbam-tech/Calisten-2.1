package com.example.domain.offline.model

/**
 * Lifecycle and download status of the complete offline package.
 */
sealed class OfflinePackageStatus {
    object NotDownloaded : OfflinePackageStatus()

    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val progressPercent: Float,
        val currentFileName: String
    ) : OfflinePackageStatus()

    data class Paused(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val progressPercent: Float
    ) : OfflinePackageStatus()

    data class Verifying(
        val currentFileName: String
    ) : OfflinePackageStatus()

    data class ReadyOffline(
        val manifest: OfflinePackageManifest
    ) : OfflinePackageStatus()

    data class UpdateAvailable(
        val currentVersion: String,
        val newVersion: String,
        val updateSizeBytes: Long
    ) : OfflinePackageStatus()

    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : OfflinePackageStatus()
}

/**
 * Metadata descriptor for each file in the offline package.
 */
data class OfflinePackageFileEntry(
    val fileName: String,
    val category: String, // "EXERCISES", "SKILLS", "RELATIONSHIPS", "PLANNING", "SCIENCE", "ASSETS"
    val sizeBytes: Long,
    val sha256Checksum: String,
    val description: String
)

/**
 * Manifest representing the complete, verified local offline package.
 */
data class OfflinePackageManifest(
    val packageVersion: String = "kalisten-offline-v1.0",
    val schemaVersion: String = "2.0",
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val totalSizeBytes: Long,
    val installedSizeBytes: Long,
    val fileEntries: List<OfflinePackageFileEntry>,
    val compatibilityMinAppVersion: String = "1.0.0"
)

/**
 * Dynamic storage metrics calculated from actual offline resources and device storage.
 */
data class StorageMetrics(
    val totalDownloadSizeBytes: Long,
    val requiredStorageBytes: Long,
    val availableStorageBytes: Long,
    val estimatedInstalledSizeBytes: Long,
    val hasEnoughSpace: Boolean,
    val formattedDownloadSize: String,
    val formattedRequiredSpace: String,
    val formattedAvailableSpace: String,
    val formattedInstalledSize: String,
    val packageVersion: String
)
