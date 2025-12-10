package com.example.studenthub.data

data class Assignment(
    val id: String = "",
    val subjectId: String = "",
    val name: String = "",
    val description: String = "",
    val dueDate: String = "", // Example format: "2023-11-20"
    val dueTime: String = "", // Example format: "14:30"
    val grade: Float = 0f,
    val userId: String = ""
)
