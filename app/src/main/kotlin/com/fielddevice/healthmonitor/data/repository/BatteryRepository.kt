package com.fielddevice.healthmonitor.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.fielddevice.healthmonitor.data.model.BatteryHealth
import com.fielddevice.healthmonitor.data.model.BatteryInfo
import com.fielddevice.healthmonitor.data.model.ChargingState
import com.fielddevice.healthmonitor.data.model.PowerSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface BatteryRepository {
    /** Emits immediately with the current battery state, then again on every change. */
    fun observeBatteryInfo(): Flow<BatteryInfo>
}

class BatteryRepositoryImpl(
    private val context: Context
) : BatteryRepository {

    override fun observeBatteryInfo(): Flow<BatteryInfo> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                trySend(intent.toBatteryInfo())
            }
        }

        // ACTION_BATTERY_CHANGED is a "sticky" broadcast: registering for it returns the
        // last broadcast intent immediately, so the very first emission below reflects the
        // true current battery state. It's a protected, system-only broadcast, so it's safe
        // (and required on API 33+) to register it as RECEIVER_NOT_EXPORTED.
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        stickyIntent?.let { trySend(it.toBatteryInfo()) }

        awaitClose { context.unregisterReceiver(receiver) }
    }

    private fun Intent.toBatteryInfo(): BatteryInfo {
        val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = if (level >= 0 && scale > 0) (level * 100) / scale else -1

        val statusExtra = getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val chargingState = when (statusExtra) {
            BatteryManager.BATTERY_STATUS_CHARGING -> ChargingState.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> ChargingState.DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> ChargingState.FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> ChargingState.NOT_CHARGING
            else -> ChargingState.UNKNOWN
        }

        val pluggedExtra = getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val powerSource = when (pluggedExtra) {
            BatteryManager.BATTERY_PLUGGED_AC -> PowerSource.AC
            BatteryManager.BATTERY_PLUGGED_USB -> PowerSource.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> PowerSource.WIRELESS
            0 -> PowerSource.BATTERY
            else -> PowerSource.UNKNOWN
        }

        val healthExtra = getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val health = when (healthExtra) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            else -> BatteryHealth.UNKNOWN
        }

        // EXTRA_TEMPERATURE is reported in tenths of a degree Celsius.
        val temperatureCelsius = getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
        val voltageMillivolts = getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

        return BatteryInfo(
            percentage = percentage,
            chargingState = chargingState,
            powerSource = powerSource,
            health = health,
            temperatureCelsius = temperatureCelsius,
            voltageMillivolts = voltageMillivolts
        )
    }
}
