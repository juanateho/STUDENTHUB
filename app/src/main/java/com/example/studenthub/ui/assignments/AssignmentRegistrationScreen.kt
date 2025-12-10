package com.example.studenthub.ui.assignments

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studenthub.data.Subject
import com.example.studenthub.ui.dialogs.DatePickerDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentRegistrationScreen(
    modifier: Modifier = Modifier,
    assignmentId: String? = null,
    onBack: () -> Unit
) {
    var assignmentName by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf("") }
    var selectedSubjectName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") } // Format: yyyy-MM-dd
    var dueTime by remember { mutableStateOf("") } // Format: HH:mm
    
    var subjectsList by remember { mutableStateOf<List<Subject>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    // Removed isSaving state to prevent UI locking, using optimistic navigation instead
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    var subjectExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = Firebase.firestore
    val auth = Firebase.auth
    val user = auth.currentUser

    // Load initial data (Subjects and Assignment if editing)
    LaunchedEffect(key1 = user) {
        if (user != null) {
            isLoading = true
            // Load Subjects first
            db.collection("subjects")
                .whereEqualTo("userId", user.uid)
                .get()
                .addOnSuccessListener { result ->
                    subjectsList = result.documents.mapNotNull { it.toObject<Subject>() }
                    
                    // If editing, load assignment data
                    if (assignmentId != null) {
                        db.collection("assignments").document(assignmentId).get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    assignmentName = document.getString("name") ?: ""
                                    selectedSubjectId = document.getString("subjectId") ?: ""
                                    // Try to find subject name from loaded list or use saved name
                                    val savedSubjName = document.getString("subjectName") 
                                    selectedSubjectName = subjectsList.find { it.id == selectedSubjectId }?.name ?: savedSubjName ?: ""
                                    
                                    description = document.getString("description") ?: ""
                                    dueDate = document.getString("dueDate") ?: ""
                                    dueTime = document.getString("dueTime") ?: ""
                                }
                                isLoading = false
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Error loading assignment", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        isLoading = false
                    }
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Error loading subjects", Toast.LENGTH_SHORT).show()
                }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                showDatePicker = false
            }
        )
    }
    
    // TimePicker logic using Android native dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            dueTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        Calendar.getInstance().get(Calendar.MINUTE),
        false // 12 hour format or 24? Let's use 24 for storage/simplicity, display can vary
    )

    if (showDeleteConfirmation && assignmentId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Assignment") },
            text = { Text("Are you sure you want to delete this assignment? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        // Optimistic Delete
                        db.collection("assignments").document(assignmentId).delete()
                        Toast.makeText(context, "Assignment deleted", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (assignmentId == null) "Assignment Registration" else "Edit Assignment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (assignmentId != null) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Assignment")
                        }
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
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Text("Please enter the details of the assignment below")
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = assignmentName,
                    onValueChange = { assignmentName = it },
                    label = { Text("Assignment name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedSubjectName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject name") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                         if (subjectsList.isEmpty()) {
                             DropdownMenuItem(
                                text = { Text("No subjects found. Add one first.") },
                                onClick = { subjectExpanded = false }
                            )
                        } else {
                            subjectsList.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = {
                                        selectedSubjectName = subject.name
                                        selectedSubjectId = subject.id
                                        subjectExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Assignment Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Delivery date")
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date Picker Field
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { },
                        label = { Text("Due Date") },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker = true }, // Make the whole field clickable
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select Date"
                                )
                            }
                        },
                        enabled = false, // Disable text editing
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )

                    // Time Picker Field
                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { },
                        label = { Text("Due Time") },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { timePickerDialog.show() },
                        trailingIcon = {
                            IconButton(onClick = { timePickerDialog.show() }) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Select Time"
                                )
                            }
                        },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f, fill = true))

                Button(
                    onClick = {
                        if (user == null) {
                            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (assignmentName.isBlank()) {
                            Toast.makeText(context, "Please enter name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedSubjectId.isBlank()) {
                            Toast.makeText(context, "Please select a subject", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Optimistic Update: Save and Close immediately
                        val id = assignmentId ?: UUID.randomUUID().toString()
                        val assignmentData = hashMapOf(
                            "id" to id,
                            "name" to assignmentName,
                            "subjectId" to selectedSubjectId,
                            "subjectName" to selectedSubjectName, // Store denormalized name for easier display
                            "description" to description,
                            "dueDate" to dueDate,
                            "dueTime" to dueTime,
                            "userId" to user.uid
                        )

                        // Fire the request
                        db.collection("assignments").document(id).set(assignmentData)
                        
                        // Navigate back immediately
                        Toast.makeText(context, "Assignment saved", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
