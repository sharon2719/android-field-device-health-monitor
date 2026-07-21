package com.fielddevice.healthmonitor.data.repository

import com.fielddevice.healthmonitor.data.model.OtaUpdateStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class OtaUpdateRepositoryTest {

    private lateinit var repository: OtaUpdateRepositoryImpl

    @Before
    fun setUp() {
        repository = OtaUpdateRepositoryImpl()
    }

    @Test
    fun `initial state exposes an available update ready to validate`() {
        val state = repository.updateState.value

        assertThat(state.isUpdateAvailable).isTrue()
        assertThat(state.status).isEqualTo(OtaUpdateStatus.READY)
        assertThat(state.rollbackVersion).isNull()
    }

    @Test
    fun `validateAndPrepareRollback captures the running version as the rollback point`() = runTest {
        val currentVersionBeforeInstall = repository.updateState.value.currentVersion

        repository.validateAndPrepareRollback()

        val state = repository.updateState.value
        assertThat(state.status).isEqualTo(OtaUpdateStatus.ROLLBACK_PREPARED)
        assertThat(state.rollbackVersion).isEqualTo(currentVersionBeforeInstall)
    }

    @Test
    fun `installUpdate advances currentVersion to the previously available version`() = runTest {
        val targetVersion = repository.updateState.value.availableVersion

        repository.validateAndPrepareRollback()
        repository.installUpdate()

        val state = repository.updateState.value
        assertThat(state.status).isEqualTo(OtaUpdateStatus.INSTALLED)
        assertThat(state.currentVersion).isEqualTo(targetVersion)
        assertThat(state.isUpdateAvailable).isFalse()
    }

    @Test(expected = IllegalStateException::class)
    fun `installUpdate without validation first is rejected`() = runTest {
        repository.installUpdate()
    }

    @Test
    fun `checkForUpdate on an up-to-date device reports UP_TO_DATE`() = runTest {
        repository.validateAndPrepareRollback()
        repository.installUpdate()

        repository.checkForUpdate()

        assertThat(repository.updateState.value.status).isEqualTo(OtaUpdateStatus.UP_TO_DATE)
    }
}
