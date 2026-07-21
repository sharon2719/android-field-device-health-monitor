package com.fielddevice.healthmonitor.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The app's nine top-level destinations, each mapping 1:1 to a required feature. Kept as a
 * flat list (rather than a bottom nav bar, which is capped at ~5 items by Material guidance)
 * because this is a tablet-first tool where a persistent side navigation drawer is both more
 * appropriate for the larger screen and can comfortably hold every destination at once.
 */
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Device Info", Icons.Filled.PhoneAndroid)
    data object Battery : Screen("battery", "Battery", Icons.Outlined.BatteryFull)
    data object Storage : Screen("storage", "Storage", Icons.Filled.SdStorage)
    data object Network : Screen("network", "Network", Icons.Filled.Wifi)
    data object LearningRecords : Screen("records", "Learning Records", Icons.Filled.Assignment)
    data object Diagnostics : Screen("diagnostics", "Diagnostics", Icons.Filled.FactCheck)
    data object Report : Screen("report", "Report", Icons.Filled.Description)
    data object Kiosk : Screen("kiosk", "Deployment Settings", Icons.Filled.SettingsSuggest)
    data object OtaUpdate : Screen("ota", "System Update", Icons.Filled.SystemUpdate)

    companion object {
        val all = listOf(Dashboard, Battery, Storage, Network, LearningRecords, Diagnostics, Report, Kiosk, OtaUpdate)
    }
}
