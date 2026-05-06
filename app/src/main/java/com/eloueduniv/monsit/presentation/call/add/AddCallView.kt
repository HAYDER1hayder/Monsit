package com.eloueduniv.monsit.presentation.call.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCallScreen(
    onBack: () -> Unit,
    viewModel: AddCallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Audio Picker
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onAction(AddCallUiAction.onAudioUrlChange(it.toString()))
        }
    }

    // Date Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.callDate.time
    )

    // Time Picker State
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { time = uiState.callTime }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { time = uiState.callTime }.get(Calendar.MINUTE)
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onAction(AddCallUiAction.onCallDateChange(Date(it)))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    viewModel.onAction(AddCallUiAction.onCallTimeChange(calendar.time))
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add New Entry",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.onAction(AddCallUiAction.onAddCall)
                        onBack()
                    }) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F7FA)
                )
            )
        },
        containerColor = Color(0xFFF3F7FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contact Name
            AddCallTextField(
                value = uiState.contactName ?: "",
                onValueChange = { viewModel.onAction(AddCallUiAction.onContactNameChange(it)) },
                label = "Contact Name",
                placeholder = "Contact Name"
            )

            // Call Date
            AddCallTextField(
                value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(uiState.callDate),
                onValueChange = {},
                label = "Call Date",
                placeholder = "Call Date",
                trailingIcon = Icons.Default.DateRange,
                readOnly = true,
                onIconClick = { showDatePicker = true }
            )

            // Call Time
            AddCallTextField(
                value = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(uiState.callTime),
                onValueChange = {},
                label = "Call Time",
                placeholder = "Call Time",
                trailingIcon = Icons.Default.Notifications, // Closest to Clock icon in default icons
                readOnly = true,
                onIconClick = { showTimePicker = true }
            )

            // Duration
            AddCallTextField(
                value = if (uiState.duration == 0L) "" else uiState.duration.toString(),
                onValueChange = { 
                    val duration = it.toLongOrNull() ?: 0L
                    viewModel.onAction(AddCallUiAction.onDurationChange(duration)) 
                },
                label = "Duration (Min:Sec)",
                placeholder = "Duration (Min:Sec)"
            )

            // Audio URL
            val displayAudioName = uiState.audioUrl?.let { url ->
                if (url.startsWith("content://") || url.startsWith("file://")) {
                    Uri.parse(url).lastPathSegment ?: url
                } else url
            } ?: ""

            AddCallTextField(
                value = displayAudioName,
                onValueChange = {},
                label = "Audio File",
                placeholder = "Select Audio File",
                trailingIcon = Icons.Default.Add,
                readOnly = true,
                onIconClick = { audioPickerLauncher.launch("audio/*") }
            )

            // Note
            AddCallTextField(
                value = uiState.note ?: "",
                onValueChange = { viewModel.onAction(AddCallUiAction.onNoteChange(it)) },
                label = "Add Note (optional)",
                placeholder = "Add Note (optional)",
                singleLine = false,
                modifier = Modifier.height(140.dp)
            )

            if (uiState.isProcessing) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.processingMessage ?: "Processing...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // AI Generated Content
            if (uiState.transcript != null || uiState.summary != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "AI Generated Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    AddCallTextField(
                        value = uiState.transcript ?: "",
                        onValueChange = { viewModel.onAction(AddCallUiAction.onTranscriptChange(it)) },
                        label = "Transcript",
                        placeholder = "Call Transcript",
                        singleLine = false,
                        modifier = Modifier.height(120.dp)
                    )

                    AddCallTextField(
                        value = uiState.summary ?: "",
                        onValueChange = { viewModel.onAction(AddCallUiAction.onSummaryChange(it)) },
                        label = "AI Summary",
                        placeholder = "AI Summary",
                        singleLine = false,
                        modifier = Modifier.height(100.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI Process Button
            if (!uiState.isProcessing && !uiState.audioUrl.isNullOrEmpty()) {
                OutlinedButton(
                    onClick = { viewModel.onAction(AddCallUiAction.onProcessCall) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transcribe & Summarize",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Create Summary Button
            Button(
                onClick = { 
                    viewModel.onAction(AddCallUiAction.onAddCall)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD1E1EF),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Save Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AddCallTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    trailingIcon: ImageVector? = null,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    onIconClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        placeholder = { Text(placeholder) },
        trailingIcon = {
            if (trailingIcon != null) {
                IconButton(onClick = { onIconClick?.invoke() }) {
                    Icon(trailingIcon, contentDescription = null, tint = Color.Black.copy(alpha = 0.7f))
                }
            }
        },
        readOnly = readOnly,
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedBorderColor = Color.Black.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.Black.copy(alpha = 0.2f),
        )
    )
}