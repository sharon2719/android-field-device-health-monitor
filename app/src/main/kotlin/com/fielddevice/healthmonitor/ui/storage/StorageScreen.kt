package com.fielddevice.healthmonitor.ui.storage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fielddevice.healthmonitor.data.model.StorageHealthStatus
import com.fielddevice.healthmonitor.ui.common.FullScreenLoading
import com.fielddevice.healthmonitor.ui.common.LabeledValue
import com.fielddevice.healthmonitor.ui.common.SectionCard
import com.fielddevice.healthmonitor.ui.common.StatusBadge
import com.fielddevice.healthmonitor.ui.common.storageHealthColor

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
fun StorageScreen(
    viewModel: StorageViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val info by viewModel.storageInfo.collectAsState()

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
            SectionCard(title = "Storage Health", icon = Icons.Filled.SdStorage) {
                StatusBadge(
                    text = info!!.healthStatus.displayName(),
                    color = storageHealthColor(info!!.healthStatus)
                )
                LinearProgressIndicator(
                    progress = { info!!.usagePercent.coerceIn(0, 100) / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = storageHealthColor(info!!.healthStatus)
                )
                LabeledValue("Usage", "${info!!.usagePercent}% used")
                LabeledValue("Total Storage", bytesToReadable(info!!.totalBytes))
                LabeledValue("Free Storage", bytesToReadable(info!!.freeBytes))
            }
        }
    }
}

private fun StorageHealthStatus.displayName(): String = when (this) {
    StorageHealthStatus.HEALTHY -> "Healthy"
    StorageHealthStatus.WARNING -> "Warning"
    StorageHealthStatus.CRITICAL -> "Critical"
}
