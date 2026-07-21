package com.fielddevice.healthmonitor.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fielddevice.healthmonitor.data.model.DeviceReport
import com.fielddevice.healthmonitor.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportUiState(
    val isGenerating: Boolean = false,
    val report: DeviceReport? = null,
    val error: String? = null
)

class ReportViewModel(
    private val repository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun generateReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, error = null)
            runCatching { repository.generateReport() }
                .onSuccess { report ->
                    _uiState.value = ReportUiState(isGenerating = false, report = report)
                }
                .onFailure { error ->
                    _uiState.value = ReportUiState(
                        isGenerating = false,
                        error = error.message ?: "Failed to generate report"
                    )
                }
        }
    }
}
