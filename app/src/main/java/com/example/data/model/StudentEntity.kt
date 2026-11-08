package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val studentId: String, // Matrícula e.g. "22315050227001"
    val fullName: String,
    val group: String = "IA5AM",
    val biometricVectorId: String,
    val avatarInitials: String,
    val avatarColorHex: Long = 0xFF00E5FF,
    val registeredDate: String = "2026-10-01"
)
