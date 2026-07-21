package com.fielddevice.healthmonitor.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OtaUpdateInfoTest {

    private fun infoWith(current: String, available: String) = OtaUpdateInfo(
        currentVersion = current,
        availableVersion = available,
        updatePackageName = "learning-platform-update.zip",
        packageSizeBytes = 1_000L,
        checksumSha256 = "sha256:abc",
        status = OtaUpdateStatus.READY,
        rollbackVersion = null
    )

    @Test
    fun `a higher patch version is detected as an available update`() {
        assertThat(infoWith(current = "1.0.0", available = "1.0.1").isUpdateAvailable).isTrue()
    }

    @Test
    fun `identical versions report no update available`() {
        assertThat(infoWith(current = "1.0.0", available = "1.0.0").isUpdateAvailable).isFalse()
    }

    @Test
    fun `a lower available version is not treated as an update`() {
        assertThat(infoWith(current = "1.2.0", available = "1.1.9").isUpdateAvailable).isFalse()
    }

    @Test
    fun `a higher minor version is detected as an available update`() {
        assertThat(infoWith(current = "1.0.9", available = "1.1.0").isUpdateAvailable).isTrue()
    }
}
