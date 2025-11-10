package com.example.studenthub.ui.teachers

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studenthub.ui.theme.STUDENTHUBTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherRegistrationScreen(
    modifier: Modifier = Modifier,
    teacherId: String? = null, // Add teacherId parameter
    onBack: () -> Unit = {}
) {
    var teacherName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // If teacherId is not null, it means we are editing an existing teacher.
    // You would typically load the teacher's data from a ViewModel or repository here.
    LaunchedEffect(teacherId) {
        if (teacherId != null) {
            // TODO: Load teacher data from repository
            // For now, we'll just populate with some dummy data
            teacherName = "Mr. Smith"
            email = "mr.smith@example.com"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (teacherId == null) "Add Teacher" else "Edit Teacher") },
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
            Button(
                onClick = { /*TODO: Save or update teacher*/ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
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
