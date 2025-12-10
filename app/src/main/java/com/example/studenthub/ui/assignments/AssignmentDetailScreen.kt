package com.example.studenthub.ui.assignments

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studenthub.Screen
import com.example.studenthub.data.Assignment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailScreen(
    modifier: Modifier = Modifier,
    assignmentId: String,
    navController: NavController
) {
    var assignment by remember { mutableStateOf<Assignment?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(assignmentId) {
        val db = Firebase.firestore
        db.collection("assignments").document(assignmentId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    assignment = document.toObject<Assignment>()
                }
                isLoading = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading assignment details", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assignment Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AssignmentEdit.createRoute(assignmentId)) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Assignment")
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
            } else if (assignment != null) {
                Text(assignment!!.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Subject: ${assignment!!.subjectName}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(assignment!!.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Due Date: ${assignment!!.dueDate}", style = MaterialTheme.typography.bodyMedium)
                    Text("Time: ${assignment!!.dueTime}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Grade: ", style = MaterialTheme.typography.titleMedium)
                    if (assignment!!.grade == 0f) {
                        Text("Pending", color = Color.Red, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    } else {
                        Text(assignment!!.grade.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
