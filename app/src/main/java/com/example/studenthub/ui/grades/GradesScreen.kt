package com.example.studenthub.ui.grades

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studenthub.data.Assignment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(
    subjectId: String, 
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val auth = Firebase.auth
    val user = auth.currentUser

    var subjectName by remember { mutableStateOf("Loading...") }
    var assignments by remember { mutableStateOf<List<Assignment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAssignment by remember { mutableStateOf<Assignment?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<Assignment?>(null) }

    // Fetch Subject Name and Assignments
    LaunchedEffect(key1 = subjectId, key2 = user) {
        if (user != null) {
            // Get Subject Name
            db.collection("subjects").document(subjectId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        subjectName = document.getString("name") ?: "Unknown Subject"
                    }
                }

            // Get Assignments (Realtime updates)
            db.collection("assignments")
                .whereEqualTo("userId", user.uid)
                .whereEqualTo("subjectId", subjectId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(context, "Error loading assignments: ${e.message}", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        assignments = snapshot.documents.mapNotNull { it.toObject<Assignment>() }
                        isLoading = false
                    }
                }
        } else {
            isLoading = false
        }
    }

    if (showAddDialog || editingAssignment != null) {
        AssignmentDialog(
            initialAssignment = editingAssignment,
            onDismiss = { 
                showAddDialog = false
                editingAssignment = null 
            },
            onSave = { name, grade ->
                val validGrade = grade.coerceIn(0.0f, 5.0f)
                val id = editingAssignment?.id ?: UUID.randomUUID().toString()
                val assignmentData = hashMapOf(
                    "id" to id,
                    "name" to name,
                    "grade" to validGrade,
                    "subjectId" to subjectId,
                    "subjectName" to subjectName,
                    "userId" to user?.uid,
                    "description" to (editingAssignment?.description ?: ""),
                    "dueDate" to (editingAssignment?.dueDate ?: ""),
                    "dueTime" to (editingAssignment?.dueTime ?: "")
                )

                db.collection("assignments").document(id).set(assignmentData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Assignment saved", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                showAddDialog = false
                editingAssignment = null
            }
        )
    }

    if (showDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Delete Assignment") },
            text = { Text("Are you sure you want to delete '${showDeleteConfirmation?.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idToDelete = showDeleteConfirmation?.id
                        if (idToDelete != null) {
                            db.collection("assignments").document(idToDelete).delete()
                            Toast.makeText(context, "Assignment deleted", Toast.LENGTH_SHORT).show()
                        }
                        showDeleteConfirmation = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.displayName ?: "Student") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenNotifications) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)) {
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$subjectName grades", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "Add Grade")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Assignment name", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Grade", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(48.dp)) // Space for delete icon
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (assignments.isEmpty()) {
                Text("No assignments found.", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(assignments) { assignment ->
                        AssignmentGradeItem(
                            assignment = assignment,
                            onEdit = { editingAssignment = assignment },
                            onDelete = { showDeleteConfirmation = assignment }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentGradeItem(
    assignment: Assignment,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onEdit() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = assignment.name, modifier = Modifier.weight(1f))
        Text(text = assignment.grade.toString())
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun AssignmentDialog(
    initialAssignment: Assignment?,
    onDismiss: () -> Unit,
    onSave: (String, Float) -> Unit
) {
    var name by remember { mutableStateOf(initialAssignment?.name ?: "") }
    var gradeString by remember { mutableStateOf(initialAssignment?.grade?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialAssignment == null) "Add Grade" else "Edit Grade") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Assignment Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = gradeString,
                    onValueChange = { gradeString = it },
                    label = { Text("Grade (0.0 - 5.0)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val grade = gradeString.toFloatOrNull() ?: 0f
                    if (name.isNotBlank()) {
                        onSave(name, grade)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
