package com.fielddevice.healthmonitor.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fielddevice.healthmonitor.data.model.DeviceInfo
import com.fielddevice.healthmonitor.ui.common.FullScreenLoading
import com.fielddevice.healthmonitor.ui.common.LabeledValue
import com.fielddevice.healthmonitor.ui.common.SectionCard

private fun bytesToReadable(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return "%.1f %s".format(value, units[unitIndex])
}

@Composable
fun DeviceInfoScreen(
    viewModel: DeviceInfoViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> FullScreenLoading(modifier = modifier.fillMaxSize())
        uiState.error != null -> Text(
            text = "Unable to read device information: ${uiState.error}",
            modifier = modifier.padding(contentPadding).padding(16.dp),
            color = MaterialTheme.colorScheme.error
        )
        uiState.deviceInfo != null -> DeviceInfoContent(
            deviceInfo = uiState.deviceInfo!!,
            contentPadding = contentPadding,
            modifier = modifier
        )
    }
}

@Composable
private fun DeviceInfoContent(
    deviceInfo: DeviceInfo,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
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
            SectionCard(title = "Identity", icon = Icons.Filled.PhoneAndroid) {
                LabeledValue("Manufacturer", deviceInfo.manufacturer)
                LabeledValue("Model", deviceInfo.model)
                LabeledValue("Android Version", deviceInfo.androidVersion)
                LabeledValue("SDK Version", "API ${deviceInfo.sdkInt}")
                LabeledValue("CPU Architecture", deviceInfo.cpuAbi)
            }
        }
        item {
            SectionCard(title = "Memory & Storage", icon = Icons.Filled.Memory) {
                LabeledValue(
                    "Available RAM",
                    "${bytesToReadable(deviceInfo.availableRamBytes)} / ${bytesToReadable(deviceInfo.totalRamBytes)}"
                )
                LabeledValue("Low-RAM Device", if (deviceInfo.isLowRamDevice) "Yes" else "No")
                LabeledValue("Total Storage", bytesToReadable(deviceInfo.totalStorageBytes))
                LabeledValue("Available Storage", bytesToReadable(deviceInfo.availableStorageBytes))
            }
        }
        item {
            SectionCard(title = "Display", icon = Icons.Filled.SdStorage) {
                LabeledValue("Screen Resolution", deviceInfo.screenResolution)
                LabeledValue("Density", "${deviceInfo.densityDpi} dpi")
            }
        }
    }
}
