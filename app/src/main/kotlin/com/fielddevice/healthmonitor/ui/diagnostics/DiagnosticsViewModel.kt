package com.fielddevice.healthmonitor.ui.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fielddevice.healthmonitor.data.model.DiagnosticsReport
import com.fielddevice.healthmonitor.data.repository.DiagnosticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiagnosticsUiState(
    val isRunning: Boolean = false,
    val report: DiagnosticsReport? = null,
    val error: String? = null
)

class DiagnosticsViewModel(
    private val repository: DiagnosticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    fun runDeviceCheck() {
        if (_uiState.value.isRunning) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRunning = true, error = null)
            runCatching { repository.runDiagnostics() }
                .onSuccess { report ->
                    _uiState.value = DiagnosticsUiState(isRunning = false, report = report)
                }
                .onFailure { error ->
                    _uiState.value = DiagnosticsUiState(
                        isRunning = false,
                        error = error.message ?: "Diagnostics run failed"
                    )
                }
        }
    }
}
