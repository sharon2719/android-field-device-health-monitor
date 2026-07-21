package com.fielddevice.healthmonitor.ui.ota

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fielddevice.healthmonitor.data.model.OtaUpdateStatus
import com.fielddevice.healthmonitor.ui.common.LabeledValue
import com.fielddevice.healthmonitor.ui.common.SectionCard
import com.fielddevice.healthmonitor.ui.common.StatusBadge

@Composable
fun OtaScreen(
    viewModel: OtaViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val update by viewModel.updateState.collectAsState()

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
            SectionCard(title = "System Update", icon = Icons.Filled.SystemUpdate) {
                LabeledValue("Current Version", update.currentVersion)
                LabeledValue("Available Version", update.availableVersion)
                LabeledValue("Update Package", update.updatePackageName)
                LabeledValue("Package Size", "${update.packageSizeBytes / (1024 * 1024)} MB")
                LabeledValue("Checksum (SHA-256)", update.checksumSha256)
                update.rollbackVersion?.let { LabeledValue("Rollback Point", it) }

                StatusBadge(
                    text = update.status.displayName,
                    color = statusColor(update.status)
                )

                Text(
                    "This simulates the workflow a real OTA agent follows — version check, " +
                        "checksum validation, capturing a rollback point, then install — without " +
                        "touching real firmware or system partitions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = viewModel::checkForUpdate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check For Update")
                }
                Button(
                    onClick = viewModel::validateAndPrepareRollback,
                    enabled = update.status == OtaUpdateStatus.READY,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Validate Package & Prepare Rollback")
                }
                Button(
                    onClick = viewModel::installUpdate,
                    enabled = update.status == OtaUpdateStatus.ROLLBACK_PREPARED,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Install Update")
                }
            }
        }
    }
}

private fun statusColor(status: OtaUpdateStatus): Color = when (status) {
    OtaUpdateStatus.UP_TO_DATE, OtaUpdateStatus.INSTALLED -> Color(0xFF2E7D32)
    OtaUpdateStatus.READY, OtaUpdateStatus.ROLLBACK_PREPARED -> Color(0xFF00695C)
    OtaUpdateStatus.VALIDATING, OtaUpdateStatus.INSTALLING -> Color(0xFFF9A825)
    OtaUpdateStatus.VALIDATION_FAILED -> Color(0xFFC62828)
}
