package com.example.studenthub.ui.subjects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studenthub.Screen
import com.example.studenthub.ui.theme.STUDENTHUBTheme

@Composable
fun SubjectsScreen(modifier: Modifier = Modifier, navController: NavController? = null) {
    Column(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Subjects List", style = MaterialTheme.typography.titleMedium)
            Row {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Filled.BarChart, contentDescription = "Stats")
                }
                IconButton(onClick = { navController?.navigate(Screen.SubjectRegistration.route) }) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "Add Subject")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(250.dp) // Added height to avoid nested scroll issues
        ) {
            items(4) { index -> // Dummy data for preview
                SubjectListItem(navController = navController, subjectId = "subject-$index")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Teachers List", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { navController?.navigate(Screen.TeacherRegistration.route) }) {
                Icon(Icons.Filled.AddCircle, contentDescription = "Add Teacher")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(250.dp) // Added height to avoid nested scroll issues
        ) {
            items(4) { index -> // Dummy data for preview
                TeacherListItem(teacherId = "teacher-$index", navController = navController)
            }
        }
    }
}

@Composable
fun SubjectListItem(modifier: Modifier = Modifier, navController: NavController?, subjectId: String) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Subject name", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Schedule", style = MaterialTheme.typography.bodySmall)
            Text("Teacher's name", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = "Grade", tint = MaterialTheme.colorScheme.secondary)
                IconButton(onClick = { navController?.navigate(Screen.SubjectEdit.createRoute(subjectId)) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
            }
        }
    }
}

@Composable
fun TeacherListItem(modifier: Modifier = Modifier, teacherId: String, navController: NavController?) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Teacher's name", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("E-mail", style = MaterialTheme.typography.bodySmall)
            Text("Subject", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController?.navigate(Screen.TeacherEdit.createRoute(teacherId)) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubjectsScreenPreview() {
    STUDENTHUBTheme {
        SubjectsScreen()
    }
}
