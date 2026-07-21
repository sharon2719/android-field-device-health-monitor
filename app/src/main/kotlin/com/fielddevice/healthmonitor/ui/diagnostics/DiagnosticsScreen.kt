package com.fielddevice.healthmonitor.ui.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fielddevice.healthmonitor.data.model.DeploymentStatus
import com.fielddevice.healthmonitor.data.model.DiagnosticCheckResult
import com.fielddevice.healthmonitor.data.model.DiagnosticsReport
import com.fielddevice.healthmonitor.ui.common.SectionCard
import com.fielddevice.healthmonitor.ui.common.StatusBadge

@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
            SectionCard(title = "Run Device Check", icon = Icons.Filled.FactCheck) {
                Text(
                    "Runs five read-only checks — battery, storage, network, device info, and " +
                        "database — to confirm this tablet is ready to hand over for deployment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = viewModel::runDeviceCheck,
                    enabled = !uiState.isRunning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Run Device Check")
                    }
                }
            }
        }

        uiState.error?.let { error ->
            item {
                Text(
                    "Diagnostics failed: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        uiState.report?.let { report ->
            item { DeviceReadinessReport(report) }
        }
    }
}

@Composable
private fun DeviceReadinessReport(report: DiagnosticsReport) {
    SectionCard(title = "Device Readiness Report") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Passed Tests", style = MaterialTheme.typography.labelLarge)
                Text(
                    "${report.passedCount}/${report.totalCount}",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Deployment Status", style = MaterialTheme.typography.labelLarge)
                StatusBadge(
                    text = when (report.deploymentStatus) {
                        DeploymentStatus.READY -> "READY"
                        DeploymentStatus.NEEDS_ATTENTION -> "NEEDS ATTENTION"
                    },
                    color = when (report.deploymentStatus) {
                        DeploymentStatus.READY -> Color(0xFF2E7D32)
                        DeploymentStatus.NEEDS_ATTENTION -> Color(0xFFC62828)
                    }
                )
            }
        }

        report.checks.forEach { check -> CheckResultRow(check) }
    }
}

@Composable
private fun CheckResultRow(check: DiagnosticCheckResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (check.passed) Icons.Filled.CheckCircle else Icons.Filled.Error,
            contentDescription = null,
            tint = if (check.passed) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
        Column {
            Text(check.id.label, style = MaterialTheme.typography.bodyLarge)
            Text(
                check.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
