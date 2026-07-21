package com.fielddevice.healthmonitor.ui.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fielddevice.healthmonitor.data.model.StorageInfo
import com.fielddevice.healthmonitor.data.repository.StorageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StorageViewModel(
    repository: StorageRepository
) : ViewModel() {

    val storageInfo: StateFlow<StorageInfo?> = repository.observeStorageInfo()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = null
        )
}
