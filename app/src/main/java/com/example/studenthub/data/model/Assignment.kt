package com.example.studenthub.data.model

import java.util.Date

data class Assignment(
    val id: String,
    val name: String,
    val subjectId: String,
    val dueDate: Date,
    val grade: Float
)
