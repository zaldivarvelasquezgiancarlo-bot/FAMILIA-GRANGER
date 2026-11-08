package com.example.ui

import android.app.Application
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.BrotexDatabase
import com.example.data.model.AcademicRules
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassSchedule
import com.example.data.model.SentinelLogEntity
import com.example.data.model.StudentEntity
import com.example.data.repository.BrotexRepository
import com.example.hardware.BrotexHardwareManager
import com.example.hardware.BrotexTelemetry
import com.example.hardware.ConnectionStatus
import com.example.hardware.HardwareMode
import com.example.hardware.SentinelMotionEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ScheduleSimulationMode(
    val id: String,
    val label: String,
    val dayOfWeek: Int, // Calendar.MONDAY / Calendar.TUESDAY
    val dayName: String,
    val simulatedHour: Int,
    val simulatedMinute: Int
)

data class SentinelAlertBanner(
    val id: Long,
    val eventType: String,
    val studentName: String,
    val studentId: String,
    val timeFormatted: String,
    val confidence: Float
)

data class AttendanceSummary(
    val total: Int = 0,
    val presentCount: Int = 0,
    val lateCount: Int = 0,
    val absentCount: Int = 0,
    val justifiedCount: Int = 0,
    val attendancePercentage: Float = 0f
)

class BrotexViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BrotexDatabase.getDatabase(application, viewModelScope)
    val repository = BrotexRepository(
        database.studentDao(),
        database.attendanceDao(),
        database.sentinelLogDao()
    )
    val hardwareManager = BrotexHardwareManager(viewModelScope)

    // Current date formatted "yyyy-MM-dd"
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val _currentSessionDate = MutableStateFlow(dateFormatter.format(Date()))
    val currentSessionDate: StateFlow<String> = _currentSessionDate.asStateFlow()

    // Simulation / Schedule selection
    val simulationPresets = listOf(
        ScheduleSimulationMode("REAL", "Tiempo Real del Sistema", Calendar.MONDAY, "Automático", -1, -1),
        ScheduleSimulationMode("MON_TOLERANCE", "Lunes 12:25 (Tolerancia Activa)", Calendar.MONDAY, "Lunes", 12, 25),
        ScheduleSimulationMode("MON_LIMIT", "Lunes 12:30 (Límite Tolerancia)", Calendar.MONDAY, "Lunes", 12, 30),
        ScheduleSimulationMode("MON_LATE", "Lunes 12:38 (Periodo Retardo)", Calendar.MONDAY, "Lunes", 12, 38),
        ScheduleSimulationMode("TUE_TOLERANCE", "Martes 10:45 (Tolerancia Activa)", Calendar.TUESDAY, "Martes", 10, 45),
        ScheduleSimulationMode("TUE_LATE", "Martes 10:55 (Periodo Retardo)", Calendar.TUESDAY, "Martes", 10, 55)
    )

    private val _activeSimulation = MutableStateFlow(simulationPresets[1]) // Default to Lunes 12:25 for rich demo!
    val activeSimulation: StateFlow<ScheduleSimulationMode> = _activeSimulation.asStateFlow()

    // Live clock & countdown
    private val _clockDisplay = MutableStateFlow("12:25:00")
    val clockDisplay: StateFlow<String> = _clockDisplay.asStateFlow()

    private val _toleranceCountdown = MutableStateFlow("05:00")
    val toleranceCountdown: StateFlow<String> = _toleranceCountdown.asStateFlow()

    private val _isToleranceActive = MutableStateFlow(true)
    val isToleranceActive: StateFlow<Boolean> = _isToleranceActive.asStateFlow()

    private val _currentSchedule = MutableStateFlow(AcademicRules.MONDAY_SCHEDULE)
    val currentSchedule: StateFlow<ClassSchedule> = _currentSchedule.asStateFlow()

    // Telemetry from hardware
    val telemetry: StateFlow<BrotexTelemetry> = hardwareManager.telemetry

    // Scan progress
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _lastDetectedFaceName = MutableStateFlow<String?>(null)
    val lastDetectedFaceName: StateFlow<String?> = _lastDetectedFaceName.asStateFlow()

    // Floating Sentinel Alert Banner
    private val _sentinelAlert = MutableStateFlow<SentinelAlertBanner?>(null)
    val sentinelAlert: StateFlow<SentinelAlertBanner?> = _sentinelAlert.asStateFlow()

    // Students list
    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Attendance filter & search
    val statusFilter = MutableStateFlow<AttendanceStatus?>(null)
    val searchQuery = MutableStateFlow("")

    // Attendance records for current date
    val attendanceRecords: StateFlow<List<AttendanceRecordEntity>> = _currentSessionDate
        .combine(repository.getAttendanceForDate(_currentSessionDate.value)) { _, list -> list }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sentinel logs
    val sentinelLogs: StateFlow<List<SentinelLogEntity>> = repository.allSentinelLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary stats
    val attendanceSummary: StateFlow<AttendanceSummary> = attendanceRecords
        .combine(allStudents) { records, students ->
            val total = if (records.isNotEmpty()) records.size else students.size
            val present = records.count { it.status == AttendanceStatus.PRESENTE.name }
            val late = records.count { it.status == AttendanceStatus.RETARDO.name }
            val absent = records.count { it.status == AttendanceStatus.FALTA.name }
            val justified = records.count { it.status == AttendanceStatus.JUSTIFICADO.name }
            val percentage = if (total > 0) ((present + late).toFloat() / total.toFloat()) * 100f else 0f
            AttendanceSummary(
                total = total,
                presentCount = present,
                lateCount = late,
                absentCount = absent,
                justifiedCount = justified,
                attendancePercentage = percentage
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AttendanceSummary())

    private var clockJob: Job? = null
    private var simulatedSecondsPassed = 0

    init {
        viewModelScope.launch {
            repository.ensureStudentsSeeded()
            repository.prepareSessionIfEmpty(_currentSessionDate.value, "Lunes")
        }
        startClockLoop()
        observeHardwareEvents()
    }

    fun setSimulationMode(mode: ScheduleSimulationMode) {
        _activeSimulation.value = mode
        simulatedSecondsPassed = 0
        if (mode.dayOfWeek == Calendar.TUESDAY) {
            _currentSchedule.value = AcademicRules.TUESDAY_SCHEDULE
        } else {
            _currentSchedule.value = AcademicRules.MONDAY_SCHEDULE
        }
        updateClockAndTolerance()
    }

    private fun startClockLoop() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                simulatedSecondsPassed++
                updateClockAndTolerance()
            }
        }
    }

    private fun updateClockAndTolerance() {
        val sim = _activeSimulation.value
        val schedule = _currentSchedule.value

        val (hour, minute, second) = if (sim.id == "REAL") {
            val cal = Calendar.getInstance()
            Triple(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))
        } else {
            val baseSeconds = (sim.simulatedHour * 3600) + (sim.simulatedMinute * 60) + simulatedSecondsPassed
            val h = (baseSeconds / 3600) % 24
            val m = (baseSeconds % 3600) / 60
            val s = baseSeconds % 60
            Triple(h, m, s)
        }

        _clockDisplay.value = "%02d:%02d:%02d".format(hour, minute, second)

        // Calculate tolerance countdown
        val currentTotalSeconds = hour * 3600 + minute * 60 + second
        val toleranceEndTotalSeconds = (schedule.startHour * 60 + schedule.startMinute + schedule.toleranceMinutes) * 60

        val diffSeconds = toleranceEndTotalSeconds - currentTotalSeconds
        if (diffSeconds > 0) {
            _isToleranceActive.value = true
            val remMin = diffSeconds / 60
            val remSec = diffSeconds % 60
            _toleranceCountdown.value = "%02d:%02d".format(remMin, remSec)
        } else {
            _isToleranceActive.value = false
            _toleranceCountdown.value = "00:00 (Cerrada)"
        }
    }

    private fun observeHardwareEvents() {
        // Face detection stream during scan
        viewModelScope.launch {
            hardwareManager.faceDetectionEvents.collect { faceEvent ->
                _lastDetectedFaceName.value = faceEvent.studentName

                // Determine attendance status based on current time & schedule rules
                val schedule = _currentSchedule.value
                val (hour, minute) = parseCurrentHourMinute()
                val calculatedStatus = AcademicRules.calculateStatusForTime(schedule, hour, minute)

                val timeStr = _clockDisplay.value
                repository.saveScannedAttendance(
                    date = _currentSessionDate.value,
                    dayName = schedule.dayName,
                    studentId = faceEvent.studentId,
                    status = calculatedStatus,
                    timeRecorded = timeStr,
                    confidence = faceEvent.confidence,
                    coordinateMm = faceEvent.railPositionMm,
                    notes = "Detectado automáticamente por BROTEX TH0.0 en riel"
                )
            }
        }

        // Sentinel door movement events
        viewModelScope.launch {
            hardwareManager.sentinelEvents.collect { motionEvent ->
                handleSentinelTrigger(motionEvent)
            }
        }
    }

    private fun handleSentinelTrigger(event: SentinelMotionEvent) {
        viewModelScope.launch {
            // Vibrate if available for floating alert
            try {
                val context = getApplication<Application>().applicationContext
                val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    manager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
                vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } catch (_: Exception) {}

            // Save to Room
            val logEntity = SentinelLogEntity(
                timeFormatted = event.timeFormatted,
                sessionDate = _currentSessionDate.value,
                eventType = event.eventType,
                studentId = event.studentId,
                studentName = event.studentName,
                roiZone = "UMBRAL_PUERTA",
                motionScore = event.confidence / 100f,
                alertTriggered = true,
                notes = "BROTEX TH0.0 Centinela: ${event.eventType} detectada"
            )
            val logId = repository.insertSentinelEvent(logEntity)

            // Show floating alert banner in app
            _sentinelAlert.value = SentinelAlertBanner(
                id = logId,
                eventType = event.eventType,
                studentName = event.studentName,
                studentId = event.studentId,
                timeFormatted = event.timeFormatted,
                confidence = event.confidence
            )

            // Auto dismiss alert banner after 6 seconds
            delay(6000)
            if (_sentinelAlert.value?.id == logId) {
                _sentinelAlert.value = null
            }
        }
    }

    fun dismissSentinelAlert() {
        _sentinelAlert.value = null
    }

    fun startAutonomousAttendanceScan() {
        if (_isScanning.value) return
        val studentsList = allStudents.value
        _isScanning.value = true
        _scanProgress.value = 0f
        _lastDetectedFaceName.value = "Iniciando recorrido sobre riel..."

        hardwareManager.runAutonomousFaceScan(
            students = studentsList,
            onProgress = { progress ->
                _scanProgress.value = progress
            },
            onFaceDetected = { student, confidence, posMm ->
                _lastDetectedFaceName.value = "${student.fullName} (Confianza: %.1f%%)".format(confidence)
            },
            onScanComplete = {
                _isScanning.value = false
                _lastDetectedFaceName.value = null
                // Ensure remaining undetected students remain marked as Falta
                viewModelScope.launch {
                    val schedule = _currentSchedule.value
                    repository.prepareSessionIfEmpty(_currentSessionDate.value, schedule.dayName)
                }
            }
        )
    }

    fun toggleSentinelMode(enable: Boolean) {
        hardwareManager.toggleSentinelMode(enable, allStudents.value)
    }

    fun triggerTestSentinelMovement(eventType: String) {
        val student = allStudents.value.randomOrNull()
        val name = student?.fullName ?: "Alumno Carlos Molina"
        val id = student?.studentId ?: "22315050227021"
        hardwareManager.triggerManualSentinelEvent(eventType, name, id)
    }

    fun manualMoveRail(stepsMm: Int) {
        hardwareManager.manualMove(stepsMm)
    }

    fun homeRail() {
        hardwareManager.startHoming()
    }

    fun moveCarriageToDoor() {
        hardwareManager.moveToDoorSentinelCoordinate()
    }

    fun toggleSpongeMotor() {
        hardwareManager.toggleSpongeMotor()
    }

    fun startCleanRoutine(fullBoard: Boolean, onDone: () -> Unit = {}) {
        hardwareManager.startCleaningRoutine(
            fullBoard = fullBoard,
            onProgress = { /* animated via telemetry */ },
            onComplete = onDone
        )
    }

    fun updateStudentAttendance(recordId: Long, newStatus: AttendanceStatus, notes: String) {
        viewModelScope.launch {
            repository.updateAttendanceRecord(recordId, newStatus, notes)
        }
    }

    fun setEsp32Ip(ip: String) {
        hardwareManager.setEsp32Ip(ip)
    }

    fun testHardwareConnection() {
        hardwareManager.testConnection()
    }

    fun generateReportText(): String {
        val summary = attendanceSummary.value
        val records = attendanceRecords.value
        val schedule = currentSchedule.value
        val logs = sentinelLogs.value

        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("REPORTE OFICIAL DE ASISTENCIA Y CENTINELA")
        sb.appendLine("INSTITUCIÓN: CBTIS No. 227")
        sb.appendLine("ASIGNATURA: La energía en los procesos de la vida")
        sb.appendLine("GRUPO: IA5AM | FECHA: ${_currentSessionDate.value}")
        sb.appendLine("HORARIO: ${schedule.getScheduleTimeFormatted()} (${schedule.dayName})")
        sb.appendLine("SISTEMA: BROTEX TH0.0 (ESP32-CAM + Riel Automatizado)")
        sb.appendLine("==========================================")
        sb.appendLine()
        sb.appendLine("ESTADÍSTICAS GENERALES:")
        sb.appendLine("- Total de alumnos: ${summary.total}")
        sb.appendLine("- Presentes: ${summary.presentCount}")
        sb.appendLine("- Retardos: ${summary.lateCount}")
        sb.appendLine("- Faltas: ${summary.absentCount}")
        sb.appendLine("- Justificados: ${summary.justifiedCount}")
        sb.appendLine("- % de Asistencia: %.1f%%".format(summary.attendancePercentage))
        sb.appendLine()
        sb.appendLine("LISTA DE ASISTENCIA:")
        records.forEachIndexed { i, r ->
            sb.appendLine("${i + 1}. [${r.status}] ${r.studentName} | Hora: ${r.timeRecorded} | Conf: %.1f%%".format(r.facialConfidence))
        }
        sb.appendLine()
        sb.appendLine("REGISTRO DE ENTRADAS Y SALIDAS (MODO CENTINELA):")
        if (logs.isEmpty()) {
            sb.appendLine("Sin eventos de puerta registrados.")
        } else {
            logs.take(15).forEach { log ->
                sb.appendLine("- ${log.timeFormatted} [${log.eventType}] ${log.studentName} (Zona: ${log.roiZone})")
            }
        }
        sb.appendLine("==========================================")
        return sb.toString()
    }

    private fun parseCurrentHourMinute(): Pair<Int, Int> {
        val clock = _clockDisplay.value
        return try {
            val parts = clock.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (_: Exception) {
            Pair(12, 25)
        }
    }
}
