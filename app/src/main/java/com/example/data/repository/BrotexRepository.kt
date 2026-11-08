package com.example.data.repository

import com.example.data.db.AttendanceDao
import com.example.data.db.SentinelLogDao
import com.example.data.db.StudentDao
import com.example.data.model.AcademicRules
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.SentinelLogEntity
import com.example.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BrotexRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val sentinelLogDao: SentinelLogDao
) {
    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    val allSentinelLogs: Flow<List<SentinelLogEntity>> = sentinelLogDao.getAllLogs()

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecordEntity>> {
        return attendanceDao.getAttendanceForDate(date)
    }

    suspend fun ensureStudentsSeeded() {
        if (studentDao.getStudentCount() == 0) {
            val initialStudents = listOf(
                StudentEntity("22315050227001", "Alonso Vega Valeria", "IA5AM", "VEC_VAL_01", "AV", 0xFF00E5FF),
                StudentEntity("22315050227002", "Álvarez Morales Brandon", "IA5AM", "VEC_BRA_02", "AM", 0xFF00B0FF),
                StudentEntity("22315050227003", "Barrera Castillo Gael", "IA5AM", "VEC_GAE_03", "BC", 0xFF1DE9B6),
                StudentEntity("22315050227004", "Bautista Reyes Jimena", "IA5AM", "VEC_JIM_04", "BR", 0xFFFF4081),
                StudentEntity("22315050227005", "Cabrera Fuentes Emilio", "IA5AM", "VEC_EMI_05", "CF", 0xFF7C4DFF),
                StudentEntity("22315050227006", "Camacho Rivera Mariana", "IA5AM", "VEC_MAR_06", "CR", 0xFF00E676),
                StudentEntity("22315050227007", "Cárdenas Ruiz Santiago", "IA5AM", "VEC_SAN_07", "CR", 0xFFFF9100),
                StudentEntity("22315050227008", "Corona Delgado Andrea", "IA5AM", "VEC_AND_08", "CD", 0xFFE040FB),
                StudentEntity("22315050227009", "Domínguez Ortiz Leonardo", "IA5AM", "VEC_LEO_09", "DO", 0xFF00B8D4),
                StudentEntity("22315050227010", "Estrada Flores Regina", "IA5AM", "VEC_REG_10", "EF", 0xFFFF5252),
                StudentEntity("22315050227011", "Fuentes Mendoza Diego", "IA5AM", "VEC_DIE_11", "FM", 0xFF448AFF),
                StudentEntity("22315050227012", "García Navarro Sofia", "IA5AM", "VEC_SOF_12", "GN", 0xFF69F0AE),
                StudentEntity("22315050227013", "Gómez Serrano Mateo", "IA5AM", "VEC_MAT_13", "GS", 0xFFFFAB40),
                StudentEntity("22315050227014", "Hernández Cruz Camila", "IA5AM", "VEC_CAM_14", "HC", 0xFFFF6E40),
                StudentEntity("22315050227015", "Ibáñez Romero Sebastián", "IA5AM", "VEC_SEB_15", "IR", 0xFF536DFE),
                StudentEntity("22315050227016", "Juárez Pacheco Daniela", "IA5AM", "VEC_DAN_16", "JP", 0xFFFF80AB),
                StudentEntity("22315050227017", "Lara Villanueva Rodrigo", "IA5AM", "VEC_ROD_17", "LV", 0xFF64FFDA),
                StudentEntity("22315050227018", "López Aguilar Valentina", "IA5AM", "VEC_VAL_18", "LA", 0xFFB388FF),
                StudentEntity("22315050227019", "Martínez Solís Alejandro", "IA5AM", "VEC_ALE_19", "MS", 0xFF00E5FF),
                StudentEntity("22315050227020", "Medina Ramos Fernanda", "IA5AM", "VEC_FER_20", "MR", 0xFFFFD740),
                StudentEntity("22315050227021", "Molina Vargas Carlos", "IA5AM", "VEC_CAR_21", "MV", 0xFF40C4FF),
                StudentEntity("22315050227022", "Navarro Paredes Renata", "IA5AM", "VEC_REN_22", "NP", 0xFFFF5252),
                StudentEntity("22315050227023", "Orozco Peña Emiliano", "IA5AM", "VEC_EMI_23", "OP", 0xFF7C4DFF),
                StudentEntity("22315050227024", "Pérez Benítez Ximena", "IA5AM", "VEC_XIM_24", "PB", 0xFF00E676),
                StudentEntity("22315050227025", "Ramírez Guzmán Daniel", "IA5AM", "VEC_DAN_25", "RG", 0xFFFF9100),
                StudentEntity("22315050227026", "Ríos Valencia Natalia", "IA5AM", "VEC_NAT_26", "RV", 0xFF1DE9B6),
                StudentEntity("22315050227027", "Sánchez Trejo Ángel", "IA5AM", "VEC_ANG_27", "ST", 0xFF00B0FF),
                StudentEntity("22315050227028", "Torres Márquez Paula", "IA5AM", "VEC_PAU_28", "TM", 0xFFFF4081),
                StudentEntity("22315050227029", "Vázquez Luna Ian", "IA5AM", "VEC_IAN_29", "VL", 0xFF64FFDA),
                StudentEntity("22315050227030", "Zúñiga Morales Estefanía", "IA5AM", "VEC_EST_30", "ZM", 0xFFFFD740)
            )
            studentDao.insertStudents(initialStudents)
        }
    }

    suspend fun prepareSessionIfEmpty(date: String, dayName: String) {
        val existing = attendanceDao.getAttendanceForDate(date).first()
        if (existing.isEmpty()) {
            val students = studentDao.getAllStudents().first()
            val initialList = students.map { student ->
                AttendanceRecordEntity(
                    studentId = student.studentId,
                    studentName = student.fullName,
                    sessionDate = date,
                    dayName = dayName,
                    timeRecorded = "--:--:--",
                    status = AttendanceStatus.FALTA.name, // Defaults to Falta until scanned or updated
                    facialConfidence = 0.0f,
                    detectionCoordinateMm = 0,
                    scanMode = "PENDING",
                    notes = "Sin registro aún"
                )
            }
            attendanceDao.insertRecords(initialList)
        }
    }

    suspend fun updateAttendanceRecord(recordId: Long, status: AttendanceStatus, notes: String) {
        attendanceDao.updateStatus(recordId, status.name, notes)
    }

    suspend fun saveScannedAttendance(
        date: String,
        dayName: String,
        studentId: String,
        status: AttendanceStatus,
        timeRecorded: String,
        confidence: Float,
        coordinateMm: Int,
        notes: String
    ) {
        val existing = attendanceDao.getRecordForStudent(date, studentId)
        val student = studentDao.getStudentById(studentId)
        val name = student?.fullName ?: "Alumno #$studentId"

        if (existing != null) {
            val updated = existing.copy(
                status = status.name,
                timeRecorded = timeRecorded,
                facialConfidence = confidence,
                detectionCoordinateMm = coordinateMm,
                scanMode = "AUTONOMOUS_RAIL",
                notes = notes
            )
            attendanceDao.updateRecord(updated)
        } else {
            attendanceDao.insertRecord(
                AttendanceRecordEntity(
                    studentId = studentId,
                    studentName = name,
                    sessionDate = date,
                    dayName = dayName,
                    timeRecorded = timeRecorded,
                    status = status.name,
                    facialConfidence = confidence,
                    detectionCoordinateMm = coordinateMm,
                    scanMode = "AUTONOMOUS_RAIL",
                    notes = notes
                )
            )
        }
    }

    suspend fun insertSentinelEvent(log: SentinelLogEntity): Long {
        return sentinelLogDao.insertLog(log)
    }

    suspend fun clearSentinelLogs() {
        sentinelLogDao.clearLogs()
    }
}
