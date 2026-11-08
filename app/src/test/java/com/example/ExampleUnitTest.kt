package com.example

import com.example.data.model.AcademicRules
import com.example.data.model.AttendanceStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testMondayScheduleToleranceRules() {
        val mon = AcademicRules.MONDAY_SCHEDULE
        // 12:25 is within 10 min tolerance -> PRESENTE
        assertEquals(AttendanceStatus.PRESENTE, AcademicRules.calculateStatusForTime(mon, 12, 25))
        // 12:30 is exact tolerance limit -> PRESENTE
        assertEquals(AttendanceStatus.PRESENTE, AcademicRules.calculateStatusForTime(mon, 12, 30))
        // 12:35 is in retardo window (12:31-12:45) -> RETARDO
        assertEquals(AttendanceStatus.RETARDO, AcademicRules.calculateStatusForTime(mon, 12, 35))
        // 12:45 is exact retardo limit -> RETARDO
        assertEquals(AttendanceStatus.RETARDO, AcademicRules.calculateStatusForTime(mon, 12, 45))
        // 12:46 is after retardo window -> FALTA
        assertEquals(AttendanceStatus.FALTA, AcademicRules.calculateStatusForTime(mon, 12, 46))
    }

    @Test
    fun testTuesdayScheduleToleranceRules() {
        val tue = AcademicRules.TUESDAY_SCHEDULE
        // 10:45 is within 10 min tolerance -> PRESENTE
        assertEquals(AttendanceStatus.PRESENTE, AcademicRules.calculateStatusForTime(tue, 10, 45))
        // 10:50 is exact tolerance limit -> PRESENTE
        assertEquals(AttendanceStatus.PRESENTE, AcademicRules.calculateStatusForTime(tue, 10, 50))
        // 10:55 is in retardo window (10:51-11:05) -> RETARDO
        assertEquals(AttendanceStatus.RETARDO, AcademicRules.calculateStatusForTime(tue, 10, 55))
        // 11:05 is exact retardo limit -> RETARDO
        assertEquals(AttendanceStatus.RETARDO, AcademicRules.calculateStatusForTime(tue, 11, 5))
        // 11:10 is after retardo window -> FALTA
        assertEquals(AttendanceStatus.FALTA, AcademicRules.calculateStatusForTime(tue, 11, 10))
    }
}
