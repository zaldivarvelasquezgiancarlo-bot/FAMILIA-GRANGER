package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val studentName: String,
    val sessionDate: String, // e.g. "2026-11-09"
    val dayName: String, // "Lunes" or "Martes"
    val timeRecorded: String, // e.g. "12:28:15"
    val status: String, // PRESENTE, RETARDO, FALTA, JUSTIFICADO
    val facialConfidence: Float = 0.0f, // e.g. 98.4%
    val detectionCoordinateMm: Int = 0, // e.g. 1420mm along the rail
    val scanMode: String = "AUTONOMOUS_RAIL", // AUTONOMOUS_RAIL, MANUAL, SENTINEL_DOOR
    val notes: String = ""
)
