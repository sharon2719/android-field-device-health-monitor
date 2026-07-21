package com.fielddevice.healthmonitor.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StorageInfoTest {

    @Test
    fun `usage below warning threshold is healthy`() {
        val info = StorageInfo(totalBytes = 100_000L, freeBytes = 50_000L)

        assertThat(info.usagePercent).isEqualTo(50)
        assertThat(info.healthStatus).isEqualTo(StorageHealthStatus.HEALTHY)
    }

    @Test
    fun `usage at 80 percent is a warning`() {
        val info = StorageInfo(totalBytes = 100_000L, freeBytes = 20_000L)

        assertThat(info.usagePercent).isEqualTo(80)
        assertThat(info.healthStatus).isEqualTo(StorageHealthStatus.WARNING)
    }

    @Test
    fun `usage at 90 percent or above is critical`() {
        val info = StorageInfo(totalBytes = 100_000L, freeBytes = 5_000L)

        assertThat(info.usagePercent).isEqualTo(95)
        assertThat(info.healthStatus).isEqualTo(StorageHealthStatus.CRITICAL)
    }

    @Test
    fun `zero total storage does not divide by zero`() {
        val info = StorageInfo(totalBytes = 0L, freeBytes = 0L)

        assertThat(info.usagePercent).isEqualTo(0)
        assertThat(info.healthStatus).isEqualTo(StorageHealthStatus.HEALTHY)
    }
}
