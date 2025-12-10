package com.example.studenthub.ui.grades

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studenthub.data.Assignment
import com.example.studenthub.data.Subject
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(
    modifier: Modifier = Modifier,
    subjectId: String?,
    onBack: () -> Unit
) {
    var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }
    var assignments by remember { mutableStateOf<Map<String, List<Assignment>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    val user = Firebase.auth.currentUser
    val context = LocalContext.current

    LaunchedEffect(user) {
        if (user != null) {
            val db = Firebase.firestore
            val subjectsQuery = if (subjectId != null) {
                db.collection("subjects").whereEqualTo("id", subjectId)
            } else {
                db.collection("subjects").whereEqualTo("userId", user.uid)
            }

            subjectsQuery.addSnapshotListener { subjectSnapshot, _ ->
                if (subjectSnapshot != null) {
                    subjects = subjectSnapshot.documents.mapNotNull { it.toObject(Subject::class.java) }
                    val subjectIds = subjects.map { it.id }
                    if (subjectIds.isNotEmpty()) {
                        db.collection("assignments")
                            .whereIn("subjectId", subjectIds)
                            .addSnapshotListener { assignmentSnapshot, _ ->
                                if (assignmentSnapshot != null) {
                                    val allAssignments = assignmentSnapshot.documents.mapNotNull { it.toObject(Assignment::class.java) }
                                    assignments = allAssignments.groupBy { it.subjectId }
                                }
                                isLoading = false
                            }
                    } else {
                        isLoading = false
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grades") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(subjects) { subject ->
                        SubjectGradesCard(subject = subject, assignments = assignments[subject.id] ?: emptyList()) {
                            updatedAssignment ->
                            val db = Firebase.firestore
                            db.collection("assignments").document(updatedAssignment.id)
                                .set(updatedAssignment)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Grade updated", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectGradesCard(
    subject: Subject,
    assignments: List<Assignment>,
    onGradeChange: (Assignment) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(subject.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (assignments.isEmpty()) {
                Text("No assignments for this subject.")
            } else {
                assignments.forEach { assignment ->
                    var grade by remember { mutableStateOf(assignment.grade.toString()) }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(assignment.name, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = grade,
                            onValueChange = { grade = it },
                            label = { Text("Grade") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(100.dp)
                        )
                        Button(onClick = { onGradeChange(assignment.copy(grade = grade.toFloatOrNull() ?: 0f)) }) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
