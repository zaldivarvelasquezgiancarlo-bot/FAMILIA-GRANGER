package com.example.data.model

import java.util.Calendar

data class ClassSchedule(
    val dayOfWeek: Int, // Calendar.MONDAY or Calendar.TUESDAY
    val dayName: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val toleranceMinutes: Int = 10,
    val retardoMinutesMax: Int = 25 // 10 min tolerance + 15 min retardo
) {
    fun getToleranceEndMinuteFormatted(): String {
        val totalMinutes = startHour * 60 + startMinute + toleranceMinutes
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return "%02d:%02d".format(h, m)
    }

    fun getRetardoLimitFormatted(): String {
        val totalMinutes = startHour * 60 + startMinute + retardoMinutesMax
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return "%02d:%02d".format(h, m)
    }

    fun getScheduleTimeFormatted(): String {
        return "%02d:%02d - %02d:%02d hrs".format(startHour, startMinute, endHour, endMinute)
    }
}

object AcademicRules {
    const val SCHOOL_NAME = "CBTIS No. 227"
    const val GROUP = "IA5AM"
    const val SUBJECT = "La energía en los procesos de la vida"
    const val HARDWARE_PROTOTYPE = "BROTEX TH0.0 (ESP32-CAM)"

    val MONDAY_SCHEDULE = ClassSchedule(
        dayOfWeek = Calendar.MONDAY,
        dayName = "Lunes",
        startHour = 12,
        startMinute = 20,
        endHour = 14,
        endMinute = 0,
        toleranceMinutes = 10,
        retardoMinutesMax = 25 // Closes regular at 12:30, retardo up to 12:45
    )

    val TUESDAY_SCHEDULE = ClassSchedule(
        dayOfWeek = Calendar.TUESDAY,
        dayName = "Martes",
        startHour = 10,
        startMinute = 40,
        endHour = 12,
        endMinute = 20,
        toleranceMinutes = 10,
        retardoMinutesMax = 25 // Closes regular at 10:50, retardo up to 11:05
    )

    /**
     * Determines status according to schedule rules for a given detected time (hour, minute)
     */
    fun calculateStatusForTime(schedule: ClassSchedule, hour: Int, minute: Int): AttendanceStatus {
        val currentTotalMinutes = hour * 60 + minute
        val startTotalMinutes = schedule.startHour * 60 + schedule.startMinute
        val toleranceLimit = startTotalMinutes + schedule.toleranceMinutes
        val retardoLimit = startTotalMinutes + schedule.retardoMinutesMax

        return when {
            currentTotalMinutes <= toleranceLimit -> AttendanceStatus.PRESENTE
            currentTotalMinutes <= retardoLimit -> AttendanceStatus.RETARDO
            else -> AttendanceStatus.FALTA
        }
    }
}
