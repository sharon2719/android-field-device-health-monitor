package com.fielddevice.healthmonitor.ui.report

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.fielddevice.healthmonitor.ui.common.SectionCard

@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
            SectionCard(title = "Diagnostics Report Generator", icon = Icons.Filled.Description) {
                Text(
                    "Generates a plain-text device report covering device information, " +
                        "battery, storage, network, and local database status — ready to " +
                        "paste into a deployment ticket or checklist.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = viewModel::generateReport,
                    enabled = !uiState.isGenerating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isGenerating) "Generating…" else "Generate Report")
                }
            }
        }

        uiState.error?.let { error ->
            item {
                Text("Failed to generate report: $error", color = MaterialTheme.colorScheme.error)
            }
        }

        uiState.report?.let { report ->
            item {
                SectionCard(title = "Generated Report") {
                    Text(
                        text = report.reportText,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { copyToClipboard(context, report.reportText) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Copy")
                        }
                        OutlinedButton(
                            onClick = { shareReport(context, report.reportText) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Share")
                        }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText("Device Report", text))
}

private fun shareReport(context: Context, text: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Device Report"))
}
