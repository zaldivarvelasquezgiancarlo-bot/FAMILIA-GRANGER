package com.example.hardware

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED_HARDWARE,
    SIMULATED_ONLINE
}

enum class HardwareMode {
    STANDBY,
    AUTONOMOUS_SCAN,
    SENTINEL_DOOR,
    MANUAL_CLEANING,
    HOMING
}

data class BrotexTelemetry(
    val connectionStatus: ConnectionStatus = ConnectionStatus.SIMULATED_ONLINE,
    val esp32Ip: String = "192.168.4.1",
    val batteryPercent: Int = 89,
    val batteryVoltage: Float = 12.3f,
    val wifiRssi: Int = -62,
    val carriagePositionMm: Int = 2200, // Door ROI coordinate
    val railLengthMm: Int = 2400,
    val isSpongeMotorOn: Boolean = false,
    val isStepperMoving: Boolean = false,
    val currentMode: HardwareMode = HardwareMode.SENTINEL_DOOR,
    val lastPingTimestamp: Long = System.currentTimeMillis()
) {
    val carriagePercent: Float
        get() = (carriagePositionMm.toFloat() / railLengthMm.toFloat()).coerceIn(0f, 1f)
}

data class FaceDetectionEvent(
    val studentId: String,
    val studentName: String,
    val confidence: Float,
    val railPositionMm: Int,
    val timeFormatted: String
)

data class SentinelMotionEvent(
    val eventType: String, // "ENTRADA" or "SALIDA"
    val studentId: String,
    val studentName: String,
    val timestamp: Long,
    val timeFormatted: String,
    val confidence: Float
)

class BrotexHardwareManager(
    private val scope: CoroutineScope
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(1500, TimeUnit.MILLISECONDS)
        .readTimeout(1500, TimeUnit.MILLISECONDS)
        .build()

    private val _telemetry = MutableStateFlow(BrotexTelemetry())
    val telemetry: StateFlow<BrotexTelemetry> = _telemetry.asStateFlow()

    private val _faceDetectionEvents = MutableSharedFlow<FaceDetectionEvent>()
    val faceDetectionEvents: SharedFlow<FaceDetectionEvent> = _faceDetectionEvents.asSharedFlow()

    private val _sentinelEvents = MutableSharedFlow<SentinelMotionEvent>()
    val sentinelEvents: SharedFlow<SentinelMotionEvent> = _sentinelEvents.asSharedFlow()

    private var sentinelJob: Job? = null
    private var scanJob: Job? = null
    private var isSimulatingSentinel = false

    fun setEsp32Ip(ip: String) {
        _telemetry.value = _telemetry.value.copy(esp32Ip = ip)
        testConnection()
    }

    fun testConnection() {
        scope.launch(Dispatchers.IO) {
            _telemetry.value = _telemetry.value.copy(connectionStatus = ConnectionStatus.CONNECTING)
            val ip = _telemetry.value.esp32Ip
            try {
                val request = Request.Builder()
                    .url("http://$ip/status")
                    .build()
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    _telemetry.value = _telemetry.value.copy(
                        connectionStatus = ConnectionStatus.CONNECTED_HARDWARE,
                        lastPingTimestamp = System.currentTimeMillis()
                    )
                } else {
                    activateSimulation()
                }
            } catch (e: Exception) {
                // If physical hardware not in LAN, activate responsive simulation
                activateSimulation()
            }
        }
    }

    private fun activateSimulation() {
        _telemetry.value = _telemetry.value.copy(
            connectionStatus = ConnectionStatus.SIMULATED_ONLINE,
            lastPingTimestamp = System.currentTimeMillis()
        )
    }

    fun startHoming() {
        if (_telemetry.value.currentMode == HardwareMode.AUTONOMOUS_SCAN) return
        scope.launch {
            _telemetry.value = _telemetry.value.copy(
                currentMode = HardwareMode.HOMING,
                isStepperMoving = true
            )
            try {
                sendCommand("/motor/home")
            } catch (_: Exception) {}

            // Animate moving to 0mm
            val startMm = _telemetry.value.carriagePositionMm
            val steps = 20
            for (i in 1..steps) {
                delay(40)
                val newPos = (startMm - (startMm * i / steps)).coerceAtLeast(0)
                _telemetry.value = _telemetry.value.copy(carriagePositionMm = newPos)
            }
            _telemetry.value = _telemetry.value.copy(
                carriagePositionMm = 0,
                isStepperMoving = false,
                currentMode = HardwareMode.STANDBY
            )
        }
    }

    fun moveToDoorSentinelCoordinate() {
        scope.launch {
            _telemetry.value = _telemetry.value.copy(isStepperMoving = true)
            val targetMm = 2200 // Door ROI coordinate
            val currentMm = _telemetry.value.carriagePositionMm
            val steps = 20
            val delta = targetMm - currentMm
            for (i in 1..steps) {
                delay(40)
                val newPos = currentMm + (delta * i / steps)
                _telemetry.value = _telemetry.value.copy(carriagePositionMm = newPos)
            }
            _telemetry.value = _telemetry.value.copy(
                carriagePositionMm = targetMm,
                isStepperMoving = false
            )
        }
    }

    fun manualMove(stepsMm: Int) {
        scope.launch {
            val currentMm = _telemetry.value.carriagePositionMm
            val maxMm = _telemetry.value.railLengthMm
            val newMm = (currentMm + stepsMm).coerceIn(0, maxMm)
            _telemetry.value = _telemetry.value.copy(carriagePositionMm = newMm)
            try {
                val dir = if (stepsMm > 0) "right" else "left"
                sendCommand("/motor/move?dir=$dir&steps=${Math.abs(stepsMm)}")
            } catch (_: Exception) {}
        }
    }

    fun toggleSpongeMotor(enable: Boolean? = null) {
        val newState = enable ?: !_telemetry.value.isSpongeMotorOn
        _telemetry.value = _telemetry.value.copy(isSpongeMotorOn = newState)
        scope.launch {
            try {
                val stateStr = if (newState) "on" else "off"
                sendCommand("/clean/sponge?state=$stateStr")
            } catch (_: Exception) {}
        }
    }

    fun startCleaningRoutine(fullBoard: Boolean, onProgress: (Float) -> Unit, onComplete: () -> Unit) {
        scope.launch {
            _telemetry.value = _telemetry.value.copy(
                currentMode = HardwareMode.MANUAL_CLEANING,
                isSpongeMotorOn = true,
                isStepperMoving = true
            )
            try {
                sendCommand("/clean/start?mode=${if (fullBoard) "full" else "quick"}")
            } catch (_: Exception) {}

            val totalSteps = if (fullBoard) 60 else 30
            val maxRailMm = if (fullBoard) 2400 else 1600

            // Forward wipe
            for (i in 0..totalSteps) {
                delay(60)
                val pos = (maxRailMm * i / totalSteps)
                _telemetry.value = _telemetry.value.copy(carriagePositionMm = pos)
                onProgress(i.toFloat() / (totalSteps * 2))
            }
            // Return wipe
            for (i in 0..totalSteps) {
                delay(50)
                val pos = (maxRailMm * (totalSteps - i) / totalSteps)
                _telemetry.value = _telemetry.value.copy(carriagePositionMm = pos)
                onProgress(0.5f + (i.toFloat() / (totalSteps * 2)))
            }

            _telemetry.value = _telemetry.value.copy(
                carriagePositionMm = 0,
                isSpongeMotorOn = false,
                isStepperMoving = false,
                currentMode = HardwareMode.STANDBY
            )
            onComplete()
        }
    }

    fun runAutonomousFaceScan(
        students: List<com.example.data.model.StudentEntity>,
        onProgress: (Float) -> Unit,
        onFaceDetected: suspend (com.example.data.model.StudentEntity, Float, Int) -> Unit,
        onScanComplete: () -> Unit
    ) {
        if (_telemetry.value.currentMode == HardwareMode.AUTONOMOUS_SCAN) return
        scanJob?.cancel()

        scanJob = scope.launch {
            _telemetry.value = _telemetry.value.copy(
                currentMode = HardwareMode.AUTONOMOUS_SCAN,
                isStepperMoving = true,
                isSpongeMotorOn = false
            )
            try {
                sendCommand("/motor/scan_start")
            } catch (_: Exception) {}

            // Start from 0mm
            _telemetry.value = _telemetry.value.copy(carriagePositionMm = 0)
            val railTotalMm = 2400
            val stepsCount = 100

            // Distribute students along the rail to detect them during carriage traversal
            val detectionPositions = students.mapIndexed { index, student ->
                val mm = ((index + 1).toFloat() / (students.size + 1) * railTotalMm).toInt()
                mm to student
            }.toMap()

            val detectedStudentIds = mutableSetOf<String>()

            for (step in 0..stepsCount) {
                delay(70)
                val currentMm = (railTotalMm * step / stepsCount)
                _telemetry.value = _telemetry.value.copy(carriagePositionMm = currentMm)
                onProgress(step / 100f)

                // Check if any student face is within camera FOV
                detectionPositions.forEach { (posMm, student) ->
                    if (Math.abs(currentMm - posMm) < 25 && student.studentId !in detectedStudentIds) {
                        detectedStudentIds.add(student.studentId)
                        val confidence = 95.0f + Random.nextFloat() * 4.9f
                        val event = FaceDetectionEvent(
                            studentId = student.studentId,
                            studentName = student.fullName,
                            confidence = confidence,
                            railPositionMm = currentMm,
                            timeFormatted = "detectado en riel"
                        )
                        _faceDetectionEvents.emit(event)
                        onFaceDetected(student, confidence, currentMm)
                    }
                }
            }

            // Reposition automatically to Door Sentinel coordinate (2200mm)
            delay(300)
            moveToDoorSentinelCoordinate()
            delay(500)

            _telemetry.value = _telemetry.value.copy(
                currentMode = HardwareMode.SENTINEL_DOOR,
                isStepperMoving = false
            )
            startSentinelMonitoring(students)
            onScanComplete()
        }
    }

    fun toggleSentinelMode(enable: Boolean, students: List<com.example.data.model.StudentEntity>) {
        if (enable) {
            moveToDoorSentinelCoordinate()
            _telemetry.value = _telemetry.value.copy(currentMode = HardwareMode.SENTINEL_DOOR)
            startSentinelMonitoring(students)
        } else {
            sentinelJob?.cancel()
            isSimulatingSentinel = false
            _telemetry.value = _telemetry.value.copy(currentMode = HardwareMode.STANDBY)
        }
    }

    private fun startSentinelMonitoring(students: List<com.example.data.model.StudentEntity>) {
        sentinelJob?.cancel()
        isSimulatingSentinel = true
        sentinelJob = scope.launch {
            try {
                sendCommand("/sentinel?mode=start&roi=door")
            } catch (_: Exception) {}

            // Periodically check or simulate door movement trigger
            while (isSimulatingSentinel) {
                // Generates realistic door crossing events every 35-50 seconds
                delay(Random.nextLong(30000, 48000))
                if (!isSimulatingSentinel) break

                val sampleStudent = if (students.isNotEmpty()) students.random() else null
                val eventType = if (Random.nextBoolean()) "SALIDA" else "ENTRADA"
                val studentName = sampleStudent?.fullName ?: "Alumno no registrado"
                val studentId = sampleStudent?.studentId ?: "EXTERNO"
                val confidence = 94.0f + Random.nextFloat() * 5.5f

                val timeNow = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date())

                val event = SentinelMotionEvent(
                    eventType = eventType,
                    studentId = studentId,
                    studentName = studentName,
                    timestamp = System.currentTimeMillis(),
                    timeFormatted = timeNow,
                    confidence = confidence
                )
                _sentinelEvents.emit(event)
            }
        }
    }

    /**
     * Helper to trigger a direct sentinel event for test / demonstration by the teacher
     */
    fun triggerManualSentinelEvent(
        eventType: String,
        studentName: String,
        studentId: String
    ) {
        scope.launch {
            val timeNow = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            val event = SentinelMotionEvent(
                eventType = eventType,
                studentId = studentId,
                studentName = studentName,
                timestamp = System.currentTimeMillis(),
                timeFormatted = timeNow,
                confidence = 98.7f
            )
            _sentinelEvents.emit(event)
        }
    }

    private suspend fun sendCommand(endpoint: String): String = withContext(Dispatchers.IO) {
        val ip = _telemetry.value.esp32Ip
        val request = Request.Builder()
            .url("http://$ip$endpoint")
            .build()
        try {
            httpClient.newCall(request).execute().use { response ->
                response.body?.string() ?: ""
            }
        } catch (e: IOException) {
            "SIMULATED_OK"
        }
    }
}
