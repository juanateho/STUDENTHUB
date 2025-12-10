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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studenthub.Screen
import com.example.studenthub.data.Assignment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

@Composable
fun AssignmentsScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val context = LocalContext.current
    var assignments by remember { mutableStateOf<List<Assignment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("assignments")
                .whereEqualTo("userId", user.uid)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Assignments List", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { navController.navigate(Screen.AssignmentRegistration.route) }) {
                Icon(Icons.Filled.AddCircle, contentDescription = "Add Assignment")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(assignments) { assignment -> 
                    AssignmentListItem(assignment = assignment, navController = navController)
                }
            }
        }
    }
}

@Composable
fun AssignmentListItem(
    modifier: Modifier = Modifier,
    assignment: Assignment,
    navController: NavController
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(assignment.name, fontWeight = FontWeight.Bold)
                // Use subjectName if available, else fallback to subjectId
                val subjectDisplayName = if (assignment.subjectName.isNotEmpty()) assignment.subjectName else assignment.subjectId
                Text("Subject: $subjectDisplayName", style = MaterialTheme.typography.bodySmall)
                Text("Grade: ${assignment.grade}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(assignment.grade.toString(), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(onClick = { navController.navigate(Screen.AssignmentEdit.createRoute(assignment.id)) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Assignment")
                }
            }
        }
    }
}
