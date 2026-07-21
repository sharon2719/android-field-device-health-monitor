package com.fielddevice.healthmonitor.ui.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fielddevice.healthmonitor.data.local.entity.LearningRecord
import com.fielddevice.healthmonitor.data.repository.LearningRecordRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LearningRecordsUiState(
    val allRecords: List<LearningRecord> = emptyList(),
    val pendingRecords: List<LearningRecord> = emptyList(),
    val pendingCount: Int = 0
)

/** One-off, non-persisted signal for the UI to show a snackbar. */
sealed interface LearningRecordsEvent {
    data class RecordAdded(val studentId: String) : LearningRecordsEvent
    data class RecordSynced(val id: Long) : LearningRecordsEvent
    data object AllRecordsSynced : LearningRecordsEvent
    data class ValidationError(val message: String) : LearningRecordsEvent
}

class LearningRecordsViewModel(
    private val repository: LearningRecordRepository
) : ViewModel() {

    val uiState: StateFlow<LearningRecordsUiState> = combine(
        repository.observeAllRecords(),
        repository.observePendingRecords(),
        repository.observePendingCount()
    ) { all, pending, pendingCount ->
        LearningRecordsUiState(allRecords = all, pendingRecords = pending, pendingCount = pendingCount)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = LearningRecordsUiState()
    )

    private val _events = MutableSharedFlow<LearningRecordsEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<LearningRecordsEvent> = _events.asSharedFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    fun addAssessmentRecord(studentId: String, activityName: String, score: Int) {
        viewModelScope.launch {
            _isSubmitting.value = true
            runCatching { repository.addRecord(studentId, activityName, score) }
                .onSuccess { _events.emit(LearningRecordsEvent.RecordAdded(studentId)) }
                .onFailure { error ->
                    _events.emit(
                        LearningRecordsEvent.ValidationError(error.message ?: "Could not save record")
                    )
                }
            _isSubmitting.value = false
        }
    }

    fun markSynced(id: Long) {
        viewModelScope.launch {
            repository.markSynced(id)
            _events.emit(LearningRecordsEvent.RecordSynced(id))
        }
    }

    fun markAllPendingSynced() {
        viewModelScope.launch {
            repository.markAllPendingSynced()
            _events.emit(LearningRecordsEvent.AllRecordsSynced)
        }
    }
}
