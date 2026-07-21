package com.fielddevice.healthmonitor.ui.battery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fielddevice.healthmonitor.data.model.BatteryHealth
import com.fielddevice.healthmonitor.data.model.BatteryInfo
import com.fielddevice.healthmonitor.data.model.ChargingState
import com.fielddevice.healthmonitor.data.model.PowerSource
import com.fielddevice.healthmonitor.data.repository.BatteryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

class BatteryViewModel(
    repository: BatteryRepository
) : ViewModel() {

    val batteryInfo: StateFlow<BatteryInfo?> = repository.observeBatteryInfo()
        .catch { emit(UNAVAILABLE_BATTERY_INFO) }
        .stateIn(
            scope = viewModelScope,
            // Keeps the sticky-broadcast receiver registered for a few seconds across
            // configuration changes / brief backgrounding instead of tearing it down
            // and re-registering on every recomposition pass.
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = null
        )

    companion object {
        private val UNAVAILABLE_BATTERY_INFO = BatteryInfo(
            percentage = -1,
            chargingState = ChargingState.UNKNOWN,
            powerSource = PowerSource.UNKNOWN,
            health = BatteryHealth.UNKNOWN,
            temperatureCelsius = 0f,
            voltageMillivolts = 0
        )
    }
}
