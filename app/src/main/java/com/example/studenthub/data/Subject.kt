package com.example.studenthub.data

data class Subject(
    val id: String = "",
    val name: String = "",
    val schedule: List<ScheduleItem> = emptyList(), 
    val teacherName: String = "", // This will now store the teacher's name
    val teacherId: String = "", // Added teacherId to link to Teacher entity
    val userId: String = ""
)

data class ScheduleItem(
    val day: String = "",
    val startTime: String = "",
    val endTime: String = ""
)
