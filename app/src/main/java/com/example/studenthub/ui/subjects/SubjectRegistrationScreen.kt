package com.example.studenthub.ui.subjects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studenthub.ui.theme.STUDENTHUBTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectRegistrationScreen(
    modifier: Modifier = Modifier,
    subjectId: String? = null, // Add subjectId parameter
    onBack: () -> Unit = {}
) {
    var subjectName by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    val days = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa")
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var onTimeSelected: (LocalTime) -> Unit by remember { mutableStateOf({}) }

    // If subjectId is not null, it means we are editing an existing subject.
    // You would typically load the subject's data from a ViewModel or repository here.
    LaunchedEffect(subjectId) {
        if (subjectId != null) {
            // TODO: Load subject data from repository
            // For now, we'll just populate with some dummy data
            subjectName = "Mathematics"
            teacherName = "Mr. Smith"
            selectedDays = setOf("Mo", "We", "Fr")
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = {
                onTimeSelected(it)
                showTimePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (subjectId == null) "Subject Registration" else "Edit Subject") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            Text("Please enter the details of the subject below")
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = subjectName,
                onValueChange = { subjectName = it },
                label = { Text("Subject name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = teacherName,
                onValueChange = { teacherName = it },
                label = { Text("Teacher's name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Schedule", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { day ->
                    FilterChip(
                        selected = selectedDays.contains(day),
                        onClick = {
                            selectedDays = if (selectedDays.contains(day)) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                        },
                        label = { Text(day) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            selectedDays.sorted().forEach { day ->
                var startTime by remember { mutableStateOf<LocalTime?>(null) }
                var endTime by remember { mutableStateOf<LocalTime?>(null) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${day.toFullDayName()}:",
                        modifier = Modifier.weight(0.3f)
                    )
                    HourBox(
                        time = startTime,
                        onClick = {
                            onTimeSelected = { startTime = it }
                            showTimePicker = true
                        },
                        modifier = Modifier.weight(0.35f)
                    )
                    Text(
                        text = "-",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HourBox(
                        time = endTime,
                        onClick = {
                            onTimeSelected = { endTime = it }
                            showTimePicker = true
                        },
                        modifier = Modifier.weight(0.35f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { /*TODO: Save or update subject*/ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun HourBox(
    time: LocalTime?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        Text(
            text = time?.format(formatter) ?: "Select time",
        )
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


fun String.toFullDayName(): String {
    return when (this) {
        "Mo" -> "Monday"
        "Tu" -> "Tuesday"
        "We" -> "Wednesday"
        "Th" -> "Thursday"
        "Fr" -> "Friday"
        "Sa" -> "Saturday"
        else -> ""
    }
}


@Preview(showBackground = true)
@Composable
fun SubjectRegistrationScreenPreview() {
    STUDENTHUBTheme {
        SubjectRegistrationScreen()
    }
}
