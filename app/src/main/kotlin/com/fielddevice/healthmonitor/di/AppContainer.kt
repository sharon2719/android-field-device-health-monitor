package com.fielddevice.healthmonitor.di

import android.content.Context
import androidx.room.Room
import com.fielddevice.healthmonitor.data.local.AppDatabase
import com.fielddevice.healthmonitor.data.repository.BatteryRepository
import com.fielddevice.healthmonitor.data.repository.BatteryRepositoryImpl
import com.fielddevice.healthmonitor.data.repository.DeviceInfoRepository
import com.fielddevice.healthmonitor.data.repository.DeviceInfoRepositoryImpl
import com.fielddevice.healthmonitor.data.repository.DiagnosticsRepository
import com.fielddevice.healthmonitor.data.repository.DiagnosticsRepositoryImpl
import com.fielddevice.healthmonitor.data.repository.KioskModeRepository
import com.fielddevice.healthmonitor.data.repository.KioskModeRepositoryImpl
import com.fielddevice.healthmonitor.data.repository.LearningRecordRepository
import com.fielddevice.healthmonitor.data.repository.LearningRecordRepositoryImpl
import com.fielddevice.healthmonitor.data.repository.NetworkRepository
import com.fielddevice.healthmonitor.data.repository.NetworkRepositoryImpl
import com.fielddevice.healthmonitor.data.repository.OtaUpdateRepository
import com.fielddevice.healthmonitor.data.repository.OtaUpdateRepositoryImpl
import com.fielddevice.healthmonitor.data.repository.ReportRepository
import com.fielddevice.healthmonitor.data.repository.ReportRepositoryImpl
import com.fielddevice.healthmonitor.data.repository.StorageRepository
import com.fielddevice.healthmonitor.data.repository.StorageRepositoryImpl

/**
 * Hand-rolled dependency container (a "poor man's Hilt"). For an app this size, a manual
 * container keeps every wiring decision visible in one file and avoids pulling in an
 * annotation-processing DI framework purely for its own sake — every repository below is a
 * cheap, stateless-ish singleton, so a simple lazily-built graph is enough.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(appContext, AppDatabase::class.java, AppDatabase.DATABASE_NAME).build()
    }

    val deviceInfoRepository: DeviceInfoRepository by lazy {
        DeviceInfoRepositoryImpl(appContext)
    }

    val batteryRepository: BatteryRepository by lazy {
        BatteryRepositoryImpl(appContext)
    }

    val storageRepository: StorageRepository by lazy {
        StorageRepositoryImpl(appContext)
    }

    val networkRepository: NetworkRepository by lazy {
        NetworkRepositoryImpl(appContext)
    }

    val learningRecordRepository: LearningRecordRepository by lazy {
        LearningRecordRepositoryImpl(database.learningRecordDao())
    }

    val kioskModeRepository: KioskModeRepository by lazy {
        KioskModeRepositoryImpl(appContext)
    }

    val otaUpdateRepository: OtaUpdateRepository by lazy {
        OtaUpdateRepositoryImpl()
    }

    val diagnosticsRepository: DiagnosticsRepository by lazy {
        DiagnosticsRepositoryImpl(
            deviceInfoRepository = deviceInfoRepository,
            batteryRepository = batteryRepository,
            storageRepository = storageRepository,
            networkRepository = networkRepository,
            learningRecordRepository = learningRecordRepository
        )
    }

    val reportRepository: ReportRepository by lazy {
        ReportRepositoryImpl(
            deviceInfoRepository = deviceInfoRepository,
            batteryRepository = batteryRepository,
            storageRepository = storageRepository,
            networkRepository = networkRepository,
            learningRecordRepository = learningRecordRepository
        )
    }
}
