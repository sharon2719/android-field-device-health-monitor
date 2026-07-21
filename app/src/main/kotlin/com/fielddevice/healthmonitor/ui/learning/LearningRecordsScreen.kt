package com.fielddevice.healthmonitor.ui.learning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fielddevice.healthmonitor.data.local.entity.LearningRecord
import com.fielddevice.healthmonitor.data.local.entity.SyncStatus
import com.fielddevice.healthmonitor.ui.common.LabeledValue
import com.fielddevice.healthmonitor.ui.common.SectionCard
import com.fielddevice.healthmonitor.ui.common.StatusBadge
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LearningRecordsScreen(
    viewModel: LearningRecordsViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is LearningRecordsEvent.RecordAdded -> "Assessment saved for ${event.studentId} (pending sync)"
                is LearningRecordsEvent.RecordSynced -> "Record marked as synced"
                LearningRecordsEvent.AllRecordsSynced -> "All pending records marked as synced"
                is LearningRecordsEvent.ValidationError -> event.message
            }
            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = 16.dp
            )
        ) {
            item {
                AddRecordForm(isSubmitting = isSubmitting, onSubmit = viewModel::addAssessmentRecord)
            }
            item {
                SectionCard(title = "Pending Sync (${uiState.pendingCount})", icon = Icons.Filled.CloudQueue) {
                    if (uiState.pendingRecords.isEmpty()) {
                        Text(
                            "No records waiting to sync.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.pendingRecords.forEach { record ->
                            RecordRow(record = record, onMarkSynced = { viewModel.markSynced(record.id) })
                            HorizontalDivider()
                        }
                        OutlinedButton(
                            onClick = viewModel::markAllPendingSynced,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mark All As Synced")
                        }
                    }
                }
            }
            item {
                SectionCard(title = "All Records (${uiState.allRecords.size})", icon = Icons.Filled.CloudDone) {
                    if (uiState.allRecords.isEmpty()) {
                        Text(
                            "No assessment records yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.allRecords.forEach { record ->
                            RecordRow(record = record, onMarkSynced = null)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun AddRecordForm(
    isSubmitting: Boolean,
    onSubmit: (studentId: String, activityName: String, score: Int) -> Unit
) {
    var studentId by remember { mutableStateOf("") }
    var activityName by remember { mutableStateOf("") }
    var scoreText by remember { mutableStateOf("") }

    SectionCard(title = "Add Assessment Record", icon = Icons.Filled.CheckCircle) {
        OutlinedTextField(
            value = studentId,
            onValueChange = { studentId = it },
            label = { Text("Student ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = activityName,
            onValueChange = { activityName = it },
            label = { Text("Activity Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = scoreText,
            onValueChange = { input -> if (input.length <= 3 && input.all(Char::isDigit)) scoreText = input },
            label = { Text("Score (0-100)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                val score = scoreText.toIntOrNull() ?: -1
                onSubmit(studentId, activityName, score)
                studentId = ""
                activityName = ""
                scoreText = ""
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Saving…" else "Add Assessment Record")
        }
    }
}

@Composable
private fun RecordRow(
    record: LearningRecord,
    onMarkSynced: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LabeledValue(
            label = record.studentId,
            value = "${record.activityName} · ${record.score}/100 · ${formatTimestamp(record.createdAt)}",
            modifier = Modifier
        )
        StatusBadge(
            text = if (record.syncStatus == SyncStatus.SYNCED) "Synced" else "Pending",
            color = if (record.syncStatus == SyncStatus.SYNCED)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.tertiary
        )
        if (onMarkSynced != null && record.syncStatus == SyncStatus.PENDING) {
            TextButton(onClick = onMarkSynced) {
                Text("Mark Synced")
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(millis))
