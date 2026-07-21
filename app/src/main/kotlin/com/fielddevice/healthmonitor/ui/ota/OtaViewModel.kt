package com.fielddevice.healthmonitor.ui.ota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fielddevice.healthmonitor.data.model.OtaUpdateInfo
import com.fielddevice.healthmonitor.data.repository.OtaUpdateRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OtaViewModel(
    private val repository: OtaUpdateRepository
) : ViewModel() {

    val updateState: StateFlow<OtaUpdateInfo> = repository.updateState

    fun checkForUpdate() {
        viewModelScope.launch { repository.checkForUpdate() }
    }

    fun validateAndPrepareRollback() {
        viewModelScope.launch { repository.validateAndPrepareRollback() }
    }

    fun installUpdate() {
        viewModelScope.launch { repository.installUpdate() }
    }
}
