package com.fielddevice.healthmonitor.ui.kiosk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fielddevice.healthmonitor.ui.common.SectionCard

@Composable
fun KioskScreen(
    viewModel: KioskViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val isEnabled by viewModel.isDeploymentModeEnabled.collectAsState()

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
            SectionCard(title = "Deployment Settings", icon = Icons.Filled.SettingsSuggest) {
                Text(
                    "Simulates the lock-task ('kiosk') mode a device-owner MDM profile would " +
                        "enforce on a dedicated learning tablet: navigation is hidden, the app " +
                        "runs full-screen, and only the learner-facing records screen is shown.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Deployment Mode", style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = viewModel::setDeploymentModeEnabled
                    )
                }
                if (isEnabled) {
                    Text(
                        "Deployment mode active — this device is running in dedicated " +
                            "learning-tablet mode. Use the unlock icon on that screen to exit " +
                            "back to full navigation (in a real MDM deployment, exiting " +
                            "lock-task mode requires device-owner privileges, not just an " +
                            "in-app toggle).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
