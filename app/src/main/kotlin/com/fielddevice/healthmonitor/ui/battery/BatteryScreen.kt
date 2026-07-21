package com.fielddevice.healthmonitor.ui.battery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fielddevice.healthmonitor.data.model.BatteryHealth
import com.fielddevice.healthmonitor.data.model.BatteryInfo
import com.fielddevice.healthmonitor.data.model.ChargingState
import com.fielddevice.healthmonitor.data.model.PowerSource
import com.fielddevice.healthmonitor.ui.common.FullScreenLoading
import com.fielddevice.healthmonitor.ui.common.LabeledValue
import com.fielddevice.healthmonitor.ui.common.SectionCard
import com.fielddevice.healthmonitor.ui.common.StatusBadge

@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val info by viewModel.batteryInfo.collectAsState()

    if (info == null) {
        FullScreenLoading(modifier = modifier.fillMaxSize())
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = 16.dp
        )
    ) {
        item {
            SectionCard(title = "Charge", icon = Icons.Filled.BatteryChargingFull) {
                LabeledValue("Battery Percentage", "${info!!.percentage}%")
                LinearProgressIndicator(
                    progress = { info!!.percentage.coerceIn(0, 100) / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = batteryLevelColor(info!!.percentage)
                )
                LabeledValue("Charging Status", info!!.chargingState.displayName())
                LabeledValue("Power Source", info!!.powerSource.displayName())
            }
        }
        item {
            SectionCard(title = "Health", icon = Icons.Filled.Thermostat) {
                Text(
                    text = "Battery Health",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusBadge(
                    text = info!!.health.displayName(),
                    color = batteryHealthColor(info!!.health)
                )
                LabeledValue("Temperature", "${info!!.temperatureCelsius} °C")
                LabeledValue("Voltage", "${info!!.voltageMillivolts} mV")
            }
        }
    }
}

private fun batteryLevelColor(percentage: Int): Color = when {
    percentage in 0..15 -> Color(0xFFC62828)
    percentage in 16..40 -> Color(0xFFF9A825)
    else -> Color(0xFF2E7D32)
}

private fun batteryHealthColor(health: BatteryHealth): Color = when (health) {
    BatteryHealth.GOOD -> Color(0xFF2E7D32)
    BatteryHealth.OVERHEAT, BatteryHealth.OVER_VOLTAGE, BatteryHealth.COLD -> Color(0xFFF9A825)
    BatteryHealth.DEAD, BatteryHealth.UNSPECIFIED_FAILURE -> Color(0xFFC62828)
    BatteryHealth.UNKNOWN -> Color(0xFF757575)
}

private fun ChargingState.displayName(): String = when (this) {
    ChargingState.CHARGING -> "Charging"
    ChargingState.DISCHARGING -> "Discharging"
    ChargingState.FULL -> "Full"
    ChargingState.NOT_CHARGING -> "Not charging"
    ChargingState.UNKNOWN -> "Unknown"
}

private fun PowerSource.displayName(): String = when (this) {
    PowerSource.AC -> "AC adapter"
    PowerSource.USB -> "USB"
    PowerSource.WIRELESS -> "Wireless"
    PowerSource.BATTERY -> "On battery"
    PowerSource.UNKNOWN -> "Unknown"
}

private fun BatteryHealth.displayName(): String = when (this) {
    BatteryHealth.GOOD -> "Good"
    BatteryHealth.OVERHEAT -> "Overheating"
    BatteryHealth.DEAD -> "Dead"
    BatteryHealth.OVER_VOLTAGE -> "Over voltage"
    BatteryHealth.UNSPECIFIED_FAILURE -> "Unspecified failure"
    BatteryHealth.COLD -> "Cold"
    BatteryHealth.UNKNOWN -> "Unknown"
}
