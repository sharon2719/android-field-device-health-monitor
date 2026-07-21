package com.fielddevice.healthmonitor.ui.network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fielddevice.healthmonitor.data.model.ConnectionType
import com.fielddevice.healthmonitor.ui.common.FullScreenLoading
import com.fielddevice.healthmonitor.ui.common.LabeledValue
import com.fielddevice.healthmonitor.ui.common.SectionCard
import com.fielddevice.healthmonitor.ui.common.StatusBadge

@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val info by viewModel.networkInfo.collectAsState()

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
            SectionCard(
                title = "Connectivity",
                icon = if (info!!.isConnected) Icons.Filled.Wifi else Icons.Filled.CloudOff
            ) {
                StatusBadge(
                    text = if (info!!.isConnected) "Online" else "Offline Mode",
                    color = if (info!!.isConnected) Color(0xFF2E7D32) else Color(0xFF757575)
                )
                LabeledValue("Internet Availability", if (info!!.isConnected) "Available" else "Unavailable")
                LabeledValue("Connection Type", info!!.connectionType.displayName())
                LabeledValue("Metered Connection", if (info!!.isMetered) "Yes" else "No")
                LabeledValue("Validated", if (info!!.isValidated) "Yes (internet reachable)" else "No")
            }
        }
        if (info!!.isOfflineMode) {
            item {
                SectionCard(title = "Offline Mode") {
                    LabeledValue(
                        "Status",
                        "This device is operating offline. Learning records are still fully " +
                            "usable and will queue locally for sync when connectivity returns."
                    )
                }
            }
        }
    }
}

private fun ConnectionType.displayName(): String = when (this) {
    ConnectionType.WIFI -> "Wi-Fi"
    ConnectionType.CELLULAR -> "Mobile data"
    ConnectionType.ETHERNET -> "Ethernet"
    ConnectionType.VPN -> "VPN"
    ConnectionType.NONE -> "None"
    ConnectionType.UNKNOWN -> "Unknown"
}
