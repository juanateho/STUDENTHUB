package com.example.studenthub.ui.stats

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarStyle
import com.example.studenthub.data.Assignment
import com.example.studenthub.data.Subject
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen() {
    val context = LocalContext.current
    val auth = Firebase.auth
    val db = Firebase.firestore
    val user = auth.currentUser

    var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }
    var assignments by remember { mutableStateOf<List<Assignment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var expanded by remember { mutableStateOf(false) }
    var selectedSubjects by remember { mutableStateOf<List<Subject>>(emptyList()) }

    LaunchedEffect(user) {
        if (user != null) {
            isLoading = true
            // Load subjects
            db.collection("subjects").whereEqualTo("userId", user.uid).get()
                .addOnSuccessListener { subjectSnapshot ->
                    subjects = subjectSnapshot.documents.mapNotNull { it.toObject<Subject>() }
                    // Load assignments
                    db.collection("assignments").whereEqualTo("userId", user.uid).get()
                        .addOnSuccessListener { assignmentSnapshot ->
                            assignments = assignmentSnapshot.documents.mapNotNull { it.toObject<Assignment>() }
                            isLoading = false
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error loading assignments: ${e.message}", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error loading subjects: ${e.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Semester grades", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Sort by:")

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = if (selectedSubjects.isEmpty()) "All subjects" else selectedSubjects.joinToString { it.name },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter by subject") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    subjects.forEach { subject ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSubjects = if (selectedSubjects.contains(subject)) {
                                        selectedSubjects - subject
                                    } else {
                                        selectedSubjects + subject
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedSubjects.contains(subject),
                                onCheckedChange = null // The row is clickable
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(subject.name)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ChartContent(subjects = subjects, assignments = assignments, selectedSubjects = selectedSubjects)
        }
    }
}

@Composable
fun ChartContent(subjects: List<Subject>, assignments: List<Assignment>, selectedSubjects: List<Subject>) {
    // Phantom data point to create space at the start of the chart
    val phantomData = BarData(
        point = Point(0f, 0f),
        label = "",
        color = Color.Transparent
    )

    val barData: List<BarData> = when {
        selectedSubjects.size == 1 -> {
            val subject = selectedSubjects.first()
            val subjectAssignments = assignments.filter { it.subjectId == subject.id }
            if (subjectAssignments.isEmpty()) {
                emptyList()
            } else {
                 val realData = subjectAssignments.mapIndexed { index, assignment ->
                    BarData(
                        point = Point((index + 1).toFloat(), assignment.grade),
                        label = assignment.name.take(10), // Shorten name
                        color = Color(0xFF1976D2)
                    )
                }
                listOf(phantomData) + realData
            }
        }
        else -> {
            val subjectsToDisplay = if (selectedSubjects.isEmpty()) subjects else selectedSubjects
            val realData = subjectsToDisplay.mapIndexed { index, subject ->
                val subjectAssignments = assignments.filter { it.subjectId == subject.id }
                val average = if (subjectAssignments.isNotEmpty()) {
                    subjectAssignments.map { it.grade }.average().toFloat()
                } else {
                    0f
                }
                BarData(
                    point = Point((index + 1).toFloat(), average),
                    label = subject.name.take(10),
                    color = Color(0xFF1976D2)
                )
            }
            listOf(phantomData) + realData
        }
    }

    if (barData.size > 1) { // Greater than 1 to account for phantom data
        val scrollState = rememberScrollState()

        val xAxisData = AxisData.Builder()
            .axisStepSize(100.dp)
            .steps(barData.size - 1)
            .bottomPadding(80.dp)
            .labelData { index ->
                if (index < barData.size) barData[index].label else ""
            }
            .axisLabelAngle(90f)
            .axisLabelColor(MaterialTheme.colorScheme.onSurface)
            .build()

        val yAxisData = AxisData.Builder()
            .steps(5)
            .labelAndAxisLinePadding(20.dp)
            .labelData { index -> (index * (5.0 / 5.0)).toFloat().toString() }
            .axisLabelColor(MaterialTheme.colorScheme.onSurface)
            .build()

        val barChartData = BarChartData(
            chartData = barData,
            xAxisData = xAxisData,
            yAxisData = yAxisData,
            barStyle = BarStyle(
                barWidth = 35.dp
            ),
            backgroundColor = MaterialTheme.colorScheme.surface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            val chartWidth = (barData.size * 100).dp
            BarChart(
                modifier = Modifier
                    .width(chartWidth)
                    .height(400.dp),
                barChartData = barChartData
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data to display. Add assignments and grades first.")
        }
    }
}
