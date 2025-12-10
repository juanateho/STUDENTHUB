package com.example.studenthub.ui.subjects

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studenthub.Screen
import com.example.studenthub.data.Subject
import com.example.studenthub.data.Teacher
import com.example.studenthub.ui.theme.STUDENTHUBTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

@Composable
fun SubjectsScreen(modifier: Modifier = Modifier, navController: NavController? = null) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val context = LocalContext.current
    var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }
    var teachers by remember { mutableStateOf<List<Teacher>>(emptyList()) }
    var isLoadingSubjects by remember { mutableStateOf(true) }
    var isLoadingTeachers by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        val user = auth.currentUser
        if (user != null) {
            // Load Subjects
            db.collection("subjects")
                .whereEqualTo("userId", user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(context, "Error loading subjects: ${e.message}", Toast.LENGTH_SHORT).show()
                        isLoadingSubjects = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        subjects = snapshot.documents.mapNotNull { it.toObject<Subject>() }
                        isLoadingSubjects = false
                    }
                }

            // Load Teachers
            db.collection("teachers")
                .whereEqualTo("userId", user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(context, "Error loading teachers: ${e.message}", Toast.LENGTH_SHORT).show()
                        isLoadingTeachers = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        teachers = snapshot.documents.mapNotNull { it.toObject<Teacher>() }
                        isLoadingTeachers = false
                    }
                }
        } else {
            isLoadingSubjects = false
            isLoadingTeachers = false
        }
    }

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
        
        if (isLoadingSubjects) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(250.dp) 
            ) {
                items(subjects) { subject -> 
                    SubjectListItem(navController = navController, subject = subject)
                }
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
        
        if (isLoadingTeachers) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(250.dp) 
            ) {
                items(teachers) { teacher ->
                    TeacherListItem(teacher = teacher, navController = navController)
                }
            }
        }
    }
}

@Composable
fun SubjectListItem(modifier: Modifier = Modifier, navController: NavController?, subject: Subject) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(subject.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            
            // Format schedule text
            val scheduleText = if (subject.schedule.isNotEmpty()) {
                subject.schedule.joinToString(", ") { "${it.day} ${it.startTime}" }
            } else {
                "No schedule"
            }
            Text("Schedule: $scheduleText", style = MaterialTheme.typography.bodySmall, maxLines = 1)
            
            Text("Teacher: ${subject.teacherName}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController?.navigate(Screen.Grades.createRoute(subject.id)) }) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = "Grade", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = { navController?.navigate(Screen.SubjectEdit.createRoute(subject.id)) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
            }
        }
    }
}

@Composable
fun TeacherListItem(modifier: Modifier = Modifier, teacher: Teacher, navController: NavController?) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(teacher.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(teacher.email, style = MaterialTheme.typography.bodySmall)
            // TODO: Podríamos buscar la materia que enseña este profesor si la tuviéramos vinculada
            Text("Teacher", style = MaterialTheme.typography.bodySmall) 
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController?.navigate(Screen.TeacherEdit.createRoute(teacher.id)) }) {
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
