package com.example.studenthub.ui.assignments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studenthub.ui.dialogs.DatePickerDialog
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentRegistrationScreen(
    modifier: Modifier = Modifier,
    assignmentId: String? = null,
    onBack: () -> Unit
) {
    var assignmentName by remember { mutableStateOf("") }
    var subjectName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var dueTime by remember { mutableStateOf<LocalTime?>(null) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Dummy subject list
    val subjects = listOf("Mathematics", "Physics", "Chemistry", "History")

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = {
                dueDate = it
                showDatePicker = false
            }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = {
                dueTime = it
                showTimePicker = false
            }
        )
    }


    LaunchedEffect(assignmentId) {
        if (assignmentId != null) {
            // TODO: Load assignment data from repository
            assignmentName = "Existing Assignment"
            subjectName = "Mathematics"
            description = "Chapter 5 exercises from the main book."
            dueDate = Date()
            dueTime = LocalTime.of(22, 0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (assignmentId == null) "Assignment Registration" else "Edit Assignment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Please enter the details of the assignment below")
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = assignmentName,
                onValueChange = { assignmentName = it },
                label = { Text("Assignment name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = !subjectExpanded }
            ) {
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Subject name") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = subjectExpanded,
                    onDismissRequest = { subjectExpanded = false }
                ) {
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject) },
                            onClick = {
                                subjectName = subject
                                subjectExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Assignment Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Delivery date")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dueDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "",
                    onValueChange = { },
                    label = { Text("Due Date") },
                    readOnly = true,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = dueTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "",
                    onValueChange = { },
                    label = { Text("Due Time") },
                    readOnly = true,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showTimePicker = true },
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Select Time"
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f, fill = true))

            Button(
                onClick = { /* TODO: Save or update assignment */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit
) {
    val timeState = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Time") },
        text = {
            TimePicker(state = timeState)
        },
        confirmButton = {
            Button(
                onClick = {
                    onTimeSelected(LocalTime.of(timeState.hour, timeState.minute))
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
