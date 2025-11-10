package com.example.studenthub.ui.assignments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    var dueDate by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var subjectExpanded by remember { mutableStateOf(false) }

    // Dummy subject list
    val subjects = listOf("Mathematics", "Physics", "Chemistry", "History")

    LaunchedEffect(assignmentId) {
        if (assignmentId != null) {
            // TODO: Load assignment data from repository
            assignmentName = "Existing Assignment"
            subjectName = "Mathematics"
            description = "Chapter 5 exercises from the main book."
            dueDate = "12/25/2024"
            dueTime = "10:00 PM"
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = dueTime,
                    onValueChange = { dueTime = it },
                    label = { Text("Due Time") },
                    modifier = Modifier.weight(1f)
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
