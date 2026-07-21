package com.fielddevice.healthmonitor.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fielddevice.healthmonitor.data.model.DeviceInfo
import com.fielddevice.healthmonitor.data.repository.DeviceInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DeviceDashboardUiState(
    val isLoading: Boolean = true,
    val deviceInfo: DeviceInfo? = null,
    val error: String? = null
)

class DeviceInfoViewModel(
    private val repository: DeviceInfoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceDashboardUiState())
    val uiState: StateFlow<DeviceDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDeviceInfo()
    }

    fun loadDeviceInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { repository.getDeviceInfo() }
                .onSuccess { info ->
                    _uiState.value = DeviceDashboardUiState(isLoading = false, deviceInfo = info)
                }
                .onFailure { throwable ->
                    _uiState.value = DeviceDashboardUiState(
                        isLoading = false,
                        error = throwable.message ?: "Failed to read device information"
                    )
                }
        }
    }
}
