package com.example.studenthub.ui.assignments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studenthub.Screen

@Composable
fun AssignmentsScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(5) { index -> // Dummy data
                AssignmentListItem(assignmentId = "assignment-$index", navController = navController)
            }
        }
    }
}

@Composable
fun AssignmentListItem(
    modifier: Modifier = Modifier,
    assignmentId: String,
    navController: NavController
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Assignment name", fontWeight = FontWeight.Bold)
                Text("Subject", style = MaterialTheme.typography.bodySmall)
                Text("Description", style = MaterialTheme.typography.bodySmall)
                Row {
                    Text("Due Date", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Due Time", style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("4.5/5.0", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(onClick = { navController.navigate(Screen.AssignmentEdit.createRoute(assignmentId)) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Assignment")
                }
            }
        }
    }
}
