package com.fielddevice.healthmonitor.data.repository

import com.fielddevice.healthmonitor.BuildConfig
import com.fielddevice.healthmonitor.data.model.OtaUpdateInfo
import com.fielddevice.healthmonitor.data.model.OtaUpdateStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simulates an OTA (over-the-air) update pipeline for a fleet learning-tablet image.
 *
 * This intentionally never touches real firmware, system partitions, or `PackageInstaller`
 * session APIs — those require privileged system/OEM access that a normal app cannot (and
 * for a portfolio project, should not) exercise. What it *does* demonstrate faithfully is the
 * workflow a real MDM/OTA agent follows: detect an available version, validate the package
 * (checksum) before touching anything, capture a rollback point, then install.
 */
interface OtaUpdateRepository {
    val updateState: StateFlow<OtaUpdateInfo>

    /** Simulates contacting an update server; here it just re-publishes the seeded package. */
    suspend fun checkForUpdate()

    /** Verifies the package checksum and records the current version as a rollback point. */
    suspend fun validateAndPrepareRollback()

    /** Applies the update; only valid once a rollback point has been prepared. */
    suspend fun installUpdate()
}

class OtaUpdateRepositoryImpl : OtaUpdateRepository {

    private val _updateState = MutableStateFlow(
        OtaUpdateInfo(
            currentVersion = BuildConfig.APP_VERSION_NAME,
            availableVersion = SIMULATED_AVAILABLE_VERSION,
            updatePackageName = SIMULATED_PACKAGE_NAME,
            packageSizeBytes = SIMULATED_PACKAGE_SIZE_BYTES,
            checksumSha256 = SIMULATED_CHECKSUM,
            status = OtaUpdateStatus.READY,
            rollbackVersion = null
        )
    )
    override val updateState: StateFlow<OtaUpdateInfo> = _updateState.asStateFlow()

    override suspend fun checkForUpdate() {
        val current = _updateState.value
        if (!current.isUpdateAvailable) {
            _updateState.value = current.copy(status = OtaUpdateStatus.UP_TO_DATE)
            return
        }
        delay(CHECK_DELAY_MS)
        _updateState.value = current.copy(status = OtaUpdateStatus.READY)
    }

    override suspend fun validateAndPrepareRollback() {
        val current = _updateState.value
        _updateState.value = current.copy(status = OtaUpdateStatus.VALIDATING)
        delay(VALIDATION_DELAY_MS)

        val recomputedChecksum = simulateChecksum(current.updatePackageName, current.availableVersion)
        if (recomputedChecksum != current.checksumSha256) {
            _updateState.value = current.copy(status = OtaUpdateStatus.VALIDATION_FAILED)
            return
        }

        // Capture the currently-running version so installUpdate() has somewhere to roll
        // back to if the install were to fail.
        _updateState.value = current.copy(
            status = OtaUpdateStatus.ROLLBACK_PREPARED,
            rollbackVersion = current.currentVersion
        )
    }

    override suspend fun installUpdate() {
        val current = _updateState.value
        check(current.status == OtaUpdateStatus.ROLLBACK_PREPARED) {
            "Cannot install before validation and rollback preparation complete"
        }

        _updateState.value = current.copy(status = OtaUpdateStatus.INSTALLING)
        delay(INSTALL_DELAY_MS)

        _updateState.value = current.copy(
            currentVersion = current.availableVersion,
            status = OtaUpdateStatus.INSTALLED
        )
    }

    private fun simulateChecksum(packageName: String, version: String): String =
        "sha256:${(packageName + version).hashCode().toUInt().toString(16).padStart(8, '0')}"

    companion object {
        private const val SIMULATED_AVAILABLE_VERSION = "1.0.1"
        private const val SIMULATED_PACKAGE_NAME = "learning-platform-update.zip"
        private const val SIMULATED_PACKAGE_SIZE_BYTES = 47_185_920L // ~45 MB
        private val SIMULATED_CHECKSUM =
            "sha256:${(SIMULATED_PACKAGE_NAME + SIMULATED_AVAILABLE_VERSION).hashCode().toUInt().toString(16).padStart(8, '0')}"

        private const val CHECK_DELAY_MS = 600L
        private const val VALIDATION_DELAY_MS = 900L
        private const val INSTALL_DELAY_MS = 1_200L
    }
}
