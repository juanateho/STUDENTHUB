package com.example.studenthub.ui.subjects

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studenthub.data.ScheduleItem
import com.example.studenthub.data.Teacher
import com.example.studenthub.ui.theme.STUDENTHUBTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectRegistrationScreen(
    modifier: Modifier = Modifier,
    subjectId: String? = null,
    onBack: () -> Unit = {}
) {
    var subjectName by remember { mutableStateOf("") }
    var selectedTeacherId by remember { mutableStateOf("") }
    var selectedTeacherName by remember { mutableStateOf("") }
    var scheduleList by remember { mutableStateOf<List<ScheduleItem>>(emptyList()) }
    var teachersList by remember { mutableStateOf<List<Teacher>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var expandedTeacherDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = Firebase.firestore
    val auth = Firebase.auth
    val user = auth.currentUser

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    LaunchedEffect(key1 = user) {
        if (user != null) {
            isLoading = true
            db.collection("teachers").whereEqualTo("userId", user.uid).get()
                .addOnSuccessListener { result ->
                    teachersList = result.documents.mapNotNull { it.toObject<Teacher>() }
                    if (subjectId != null) {
                        db.collection("subjects").document(subjectId).get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    subjectName = document.getString("name") ?: ""
                                    selectedTeacherName = document.getString("teacherName") ?: ""
                                    selectedTeacherId = document.getString("teacherId") ?: ""
                                    val items = document.get("schedule") as? List<Map<String, String>>
                                    scheduleList = items?.map {
                                        ScheduleItem(day = it["day"] ?: "", startTime = it["startTime"] ?: "", endTime = it["endTime"] ?: "")
                                    } ?: emptyList()
                                }
                                isLoading = false
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Error loading subject", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        isLoading = false
                    }
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Error loading teachers", Toast.LENGTH_SHORT).show()
                }
        }
    }

    if (showDeleteConfirmation && subjectId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Subject") },
            text = { Text("Are you sure you want to delete this subject? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    db.collection("subjects").document(subjectId).delete()
                    Toast.makeText(context, "Subject deleted", Toast.LENGTH_SHORT).show()
                    onBack()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (subjectId == null) "Add Subject" else "Edit Subject") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    if (subjectId != null) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Subject")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier.padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Text("Please enter the details of the subject below")
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = subjectName, onValueChange = { subjectName = it }, label = { Text("Subject's name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = expandedTeacherDropdown, onExpandedChange = { expandedTeacherDropdown = it }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = selectedTeacherName, onValueChange = {}, readOnly = true, label = { Text("Teacher") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = expandedTeacherDropdown, onDismissRequest = { expandedTeacherDropdown = false }) {
                        if (teachersList.isEmpty()) {
                            DropdownMenuItem(text = { Text("No teachers found. Add one first.") }, onClick = { expandedTeacherDropdown = false })
                        } else {
                            teachersList.forEach { teacher ->
                                DropdownMenuItem(text = { Text(teacher.name) }, onClick = {
                                    selectedTeacherName = teacher.name
                                    selectedTeacherId = teacher.id
                                    expandedTeacherDropdown = false
                                })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Schedule", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                daysOfWeek.forEach { day ->
                    var isChecked by remember { mutableStateOf(scheduleList.any { it.day == day }) }
                    LaunchedEffect(scheduleList) { isChecked = scheduleList.any { it.day == day } }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(checked = isChecked, onCheckedChange = { checked ->
                                isChecked = checked
                                if (checked) {
                                    if (scheduleList.none { it.day == day }) {
                                        scheduleList = scheduleList + ScheduleItem(day, "08:00", "09:00")
                                    }
                                } else {
                                    scheduleList = scheduleList.filter { it.day != day }
                                }
                            })
                            Text(day, style = MaterialTheme.typography.bodyLarge)
                        }

                        if (isChecked) {
                            val item = scheduleList.find { it.day == day }
                            if (item != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 48.dp, bottom = 8.dp, end = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    TimePickerButton(label = "Start", time = item.startTime, onTimeSelected = { newTime ->
                                        val newEndTime = String.format("%02d:%02d", (newTime.split(":")[0].toInt() + 1) % 24, newTime.split(":")[1].toInt())
                                        scheduleList = scheduleList.map {
                                            if (it.day == day) it.copy(startTime = newTime, endTime = newEndTime) else it
                                        }
                                    }, modifier = Modifier.weight(1f))
                                    TimePickerButton(label = "End", time = item.endTime, onTimeSelected = { newTime ->
                                        scheduleList = scheduleList.map {
                                            if (it.day == day) it.copy(endTime = newTime) else it
                                        }
                                    }, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (user == null) {
                            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (subjectName.isBlank()) {
                            Toast.makeText(context, "Please enter a subject name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val invalidSchedule = scheduleList.any { it.startTime >= it.endTime }
                        if (invalidSchedule) {
                            Toast.makeText(context, "Check schedules: End time must be after Start time", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val id = subjectId ?: UUID.randomUUID().toString()
                        val subjectData = hashMapOf(
                            "id" to id,
                            "name" to subjectName,
                            "schedule" to scheduleList,
                            "teacherName" to selectedTeacherName,
                            "teacherId" to selectedTeacherId,
                            "userId" to user.uid
                        )
                        db.collection("subjects").document(id).set(subjectData)
                        Toast.makeText(context, "Subject saved", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save") }
            }
        }
    }
}

@Composable
fun TimePickerButton(label: String, time: String, onTimeSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val parts = time.split(":")
    val initialHour = if (parts.size == 2) parts[0].toIntOrNull() ?: 8 else 8
    val initialMinute = if (parts.size == 2) parts[1].toIntOrNull() ?: 0 else 0

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            onTimeSelected(formattedTime)
        },
        initialHour, initialMinute, true
    )

    OutlinedButton(onClick = { timePickerDialog.show() }, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(time, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubjectRegistrationScreenPreview() {
    STUDENTHUBTheme {
        SubjectRegistrationScreen()
    }
}
