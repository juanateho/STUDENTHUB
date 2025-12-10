package com.example.studenthub.ui.teachers

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studenthub.ui.theme.STUDENTHUBTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherRegistrationScreen(
    modifier: Modifier = Modifier,
    teacherId: String? = null,
    onBack: () -> Unit = {}
) {
    var teacherName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = Firebase.firestore
    val auth = Firebase.auth

    LaunchedEffect(teacherId) {
        if (teacherId != null) {
            isLoading = true
            db.collection("teachers").document(teacherId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        teacherName = document.getString("name") ?: ""
                        email = document.getString("email") ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error loading teacher", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }

    if (showDeleteConfirmation && teacherId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Teacher") },
            text = { Text("Are you sure you want to delete this teacher? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        db.collection("teachers").document(teacherId).delete()
                        Toast.makeText(context, "Teacher deleted", Toast.LENGTH_SHORT).show()
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
                title = { Text(if (teacherId == null) "Add Teacher" else "Edit Teacher") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (teacherId != null) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Teacher")
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
            Text("Please enter the details of the teacher below")
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = teacherName,
                onValueChange = { teacherName = it },
                label = { Text("Teacher's name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        val user = auth.currentUser
                        if (user == null) {
                            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (teacherName.isBlank()) {
                            Toast.makeText(context, "Please enter a teacher name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val id = teacherId ?: UUID.randomUUID().toString()
                        val teacherData = hashMapOf(
                            "id" to id,
                            "name" to teacherName,
                            "email" to email,
                            "userId" to user.uid
                        )

                        db.collection("teachers").document(id).set(teacherData)
                        Toast.makeText(context, "Teacher saved", Toast.LENGTH_SHORT).show()
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

@Preview(showBackground = true)
@Composable
fun TeacherRegistrationScreenPreview() {
    STUDENTHUBTheme {
        TeacherRegistrationScreen()
    }
}
