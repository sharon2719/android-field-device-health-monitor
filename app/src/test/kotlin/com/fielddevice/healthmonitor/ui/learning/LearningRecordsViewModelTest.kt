package com.fielddevice.healthmonitor.ui.learning

import app.cash.turbine.test
import com.fielddevice.healthmonitor.data.local.entity.LearningRecord
import com.fielddevice.healthmonitor.data.local.entity.SyncStatus
import com.fielddevice.healthmonitor.data.repository.LearningRecordRepository
import com.fielddevice.healthmonitor.testutil.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LearningRecordsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: LearningRecordRepository = mockk()
    private lateinit var viewModel: LearningRecordsViewModel

    private val sampleRecord = LearningRecord(
        id = 1L,
        studentId = "s-1",
        activityName = "Reading",
        score = 90,
        syncStatus = SyncStatus.PENDING,
        createdAt = 1_000L
    )

    @Before
    fun setUp() {
        every { repository.observeAllRecords() } returns flowOf(listOf(sampleRecord))
        every { repository.observePendingRecords() } returns flowOf(listOf(sampleRecord))
        every { repository.observePendingCount() } returns flowOf(1)
        viewModel = LearningRecordsViewModel(repository)
    }

    @Test
    fun `uiState reflects the combined repository flows`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()

            assertThat(state.allRecords).containsExactly(sampleRecord)
            assertThat(state.pendingRecords).containsExactly(sampleRecord)
            assertThat(state.pendingCount).isEqualTo(1)
        }
    }

    @Test
    fun `addAssessmentRecord emits a RecordAdded event on success`() = runTest {
        coEvery { repository.addRecord("s-2", "Math Quiz", 75) } returns 2L

        viewModel.events.test {
            viewModel.addAssessmentRecord("s-2", "Math Quiz", 75)
            val event = awaitItem()
            assertThat(event).isInstanceOf(LearningRecordsEvent.RecordAdded::class.java)
        }
    }

    @Test
    fun `addAssessmentRecord emits a ValidationError when the repository rejects it`() = runTest {
        coEvery { repository.addRecord(any(), any(), any()) } throws IllegalArgumentException("score must be between 0 and 100")

        viewModel.events.test {
            viewModel.addAssessmentRecord("s-3", "Quiz", 999)
            val event = awaitItem()
            assertThat(event).isInstanceOf(LearningRecordsEvent.ValidationError::class.java)
        }
    }

    @Test
    fun `markSynced delegates to the repository and emits RecordSynced`() = runTest {
        coEvery { repository.markSynced(1L) } returns Unit

        viewModel.events.test {
            viewModel.markSynced(1L)
            awaitItem()
        }
        coVerify { repository.markSynced(1L) }
    }

    @Test
    fun `markAllPendingSynced delegates to the repository`() = runTest {
        coEvery { repository.markAllPendingSynced() } returns Unit

        viewModel.events.test {
            viewModel.markAllPendingSynced()
            awaitItem()
        }
        coVerify { repository.markAllPendingSynced() }
    }
}
