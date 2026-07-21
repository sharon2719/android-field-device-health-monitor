package com.fielddevice.healthmonitor.ui.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fielddevice.healthmonitor.data.model.NetworkInfo
import com.fielddevice.healthmonitor.data.repository.NetworkRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class NetworkViewModel(
    repository: NetworkRepository
) : ViewModel() {

    val networkInfo: StateFlow<NetworkInfo?> = repository.observeNetworkInfo()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = null
        )
}
