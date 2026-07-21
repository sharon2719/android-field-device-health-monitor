package com.fielddevice.healthmonitor.ui.kiosk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fielddevice.healthmonitor.data.repository.KioskModeRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class KioskViewModel(
    private val repository: KioskModeRepository
) : ViewModel() {

    val isDeploymentModeEnabled: StateFlow<Boolean> = repository.isDeploymentModeEnabled

    fun setDeploymentModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDeploymentModeEnabled(enabled)
        }
    }
}
