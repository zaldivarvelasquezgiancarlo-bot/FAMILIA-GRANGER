package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sentinel_logs")
data class SentinelLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val timeFormatted: String, // e.g. "12:38:42"
    val sessionDate: String, // e.g. "2026-11-09"
    val eventType: String, // "ENTRADA" or "SALIDA"
    val studentId: String = "",
    val studentName: String,
    val roiZone: String = "DOOR_THRESHOLD",
    val motionScore: Float = 0.94f,
    val alertTriggered: Boolean = true,
    val notes: String = ""
)
