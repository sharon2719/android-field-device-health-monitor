package com.fielddevice.healthmonitor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fielddevice.healthmonitor.di.AppContainer
import com.fielddevice.healthmonitor.ui.battery.BatteryViewModel
import com.fielddevice.healthmonitor.ui.dashboard.DeviceInfoViewModel
import com.fielddevice.healthmonitor.ui.diagnostics.DiagnosticsViewModel
import com.fielddevice.healthmonitor.ui.kiosk.KioskViewModel
import com.fielddevice.healthmonitor.ui.learning.LearningRecordsViewModel
import com.fielddevice.healthmonitor.ui.network.NetworkViewModel
import com.fielddevice.healthmonitor.ui.ota.OtaViewModel
import com.fielddevice.healthmonitor.ui.report.ReportViewModel
import com.fielddevice.healthmonitor.ui.storage.StorageViewModel

/**
 * Single [ViewModelProvider.Factory] for the whole app. Pairs with the manual [AppContainer]
 * DI approach: every ViewModel's dependencies are plain constructor parameters pulled
 * straight from the container, which keeps each ViewModel trivially unit-testable without
 * touching this factory at all.
 */
class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = when (modelClass) {
            DeviceInfoViewModel::class.java ->
                DeviceInfoViewModel(container.deviceInfoRepository)

            BatteryViewModel::class.java ->
                BatteryViewModel(container.batteryRepository)

            StorageViewModel::class.java ->
                StorageViewModel(container.storageRepository)

            NetworkViewModel::class.java ->
                NetworkViewModel(container.networkRepository)

            LearningRecordsViewModel::class.java ->
                LearningRecordsViewModel(container.learningRecordRepository)

            DiagnosticsViewModel::class.java ->
                DiagnosticsViewModel(container.diagnosticsRepository)

            ReportViewModel::class.java ->
                ReportViewModel(container.reportRepository)

            KioskViewModel::class.java ->
                KioskViewModel(container.kioskModeRepository)

            OtaViewModel::class.java ->
                OtaViewModel(container.otaUpdateRepository)

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return viewModel as T
    }
}
