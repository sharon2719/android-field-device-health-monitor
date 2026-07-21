package com.fielddevice.healthmonitor.data.model

/**
 * Snapshot of the device's battery, rebuilt each time the system broadcasts
 * `Intent.ACTION_BATTERY_CHANGED`. That intent is a "sticky" broadcast, so a receiver
 * registered for it (see `BatteryRepository`) gets the current state immediately on
 * registration and every subsequent change, without polling.
 */
data class BatteryInfo(
    val percentage: Int,
    val chargingState: ChargingState,
    val powerSource: PowerSource,
    val health: BatteryHealth,
    val temperatureCelsius: Float,
    val voltageMillivolts: Int
)

enum class ChargingState {
    CHARGING,
    DISCHARGING,
    FULL,
    NOT_CHARGING,
    UNKNOWN
}

enum class PowerSource {
    AC,
    USB,
    WIRELESS,
    BATTERY,
    UNKNOWN
}

/**
 * Mirrors the `BatteryManager.BATTERY_HEALTH_*` constants with readable names, since a
 * field technician reading a diagnostics report shouldn't need to decode raw platform ints.
 */
enum class BatteryHealth {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    UNSPECIFIED_FAILURE,
    COLD,
    UNKNOWN
}
