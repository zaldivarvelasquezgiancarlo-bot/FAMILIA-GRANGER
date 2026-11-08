package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hardware.ConnectionStatus
import com.example.ui.BrotexViewModel
import com.example.ui.components.FloatingSentinelAlert
import com.example.ui.components.IpConfigDialog
import com.example.ui.components.SimulationSelectorDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.CleanerScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.StatusDashboardScreen
import com.example.ui.theme.BrotexCyanAccent
import com.example.ui.theme.BrotexCyanPrimary
import com.example.ui.theme.BrotexDarkBackground
import com.example.ui.theme.BrotexNavyDark
import com.example.ui.theme.BrotexNavySurface
import com.example.ui.theme.BrotexNavySurfaceVariant
import com.example.ui.theme.BrotexTheme
import com.example.ui.theme.StatusPresent

class MainActivity : ComponentActivity() {

    private val viewModel: BrotexViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrotexTheme(darkTheme = true) {
                BrotexMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrotexMainApp(viewModel: BrotexViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showIpDialog by remember { mutableStateOf(false) }
    var showSimulationDialog by remember { mutableStateOf(false) }

    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val clockDisplay by viewModel.clockDisplay.collectAsStateWithLifecycle()
    val toleranceCountdown by viewModel.toleranceCountdown.collectAsStateWithLifecycle()
    val isToleranceActive by viewModel.isToleranceActive.collectAsStateWithLifecycle()
    val currentSchedule by viewModel.currentSchedule.collectAsStateWithLifecycle()
    val activeSimulation by viewModel.activeSimulation.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val lastDetectedFaceName by viewModel.lastDetectedFaceName.collectAsStateWithLifecycle()
    val sentinelAlert by viewModel.sentinelAlert.collectAsStateWithLifecycle()

    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val attendanceRecords by viewModel.attendanceRecords.collectAsStateWithLifecycle()
    val summary by viewModel.attendanceSummary.collectAsStateWithLifecycle()
    val sentinelLogs by viewModel.sentinelLogs.collectAsStateWithLifecycle()
    val currentDate by viewModel.currentSessionDate.collectAsStateWithLifecycle()

    // Dialogs
    if (showIpDialog) {
        IpConfigDialog(
            currentIp = telemetry.esp32Ip,
            connectionStatus = telemetry.connectionStatus,
            onSaveIp = { viewModel.setEsp32Ip(it) },
            onTestConnection = { viewModel.testHardwareConnection() },
            onDismiss = { showIpDialog = false }
        )
    }

    if (showSimulationDialog) {
        SimulationSelectorDialog(
            presets = viewModel.simulationPresets,
            activeSimulation = activeSimulation,
            onSelectPreset = { viewModel.setSimulationMode(it) },
            onDismiss = { showSimulationDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BrotexDarkBackground),
        containerColor = BrotexDarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = BrotexNavyDark,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Estado
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = stringResource(R.string.tab_status)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_status), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrotexNavyDark,
                        selectedTextColor = BrotexCyanPrimary,
                        indicatorColor = BrotexCyanPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_tab_status")
                )

                // Tab 1: Asistencia
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.People else Icons.Outlined.People,
                            contentDescription = stringResource(R.string.tab_attendance)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_attendance), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrotexNavyDark,
                        selectedTextColor = BrotexCyanPrimary,
                        indicatorColor = BrotexCyanPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_tab_attendance")
                )

                // Tab 2: Limpieza
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.CleaningServices else Icons.Outlined.CleaningServices,
                            contentDescription = stringResource(R.string.tab_cleaner)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_cleaner), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrotexNavyDark,
                        selectedTextColor = BrotexCyanPrimary,
                        indicatorColor = BrotexCyanPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_tab_cleaner")
                )

                // Tab 3: Reportes
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                            contentDescription = stringResource(R.string.tab_reports)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_reports), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrotexNavyDark,
                        selectedTextColor = BrotexCyanPrimary,
                        indicatorColor = BrotexCyanPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_tab_reports")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Header
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars),
                    color = BrotexNavyDark,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "BROTEX",
                                    color = BrotexCyanPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TH0.0",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "CBTIS 227 • Grupo IA5AM",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 11.sp
                            )
                        }

                        // Status connection pill
                        val wifiColor = when (telemetry.connectionStatus) {
                            ConnectionStatus.CONNECTED_HARDWARE -> StatusPresent
                            ConnectionStatus.SIMULATED_ONLINE -> BrotexCyanPrimary
                            else -> Color(0xFFFFD600)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(BrotexNavySurface)
                                .border(1.dp, wifiColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                .clickable { showIpDialog = true }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("top_status_pill")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(wifiColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = telemetry.esp32Ip,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Main screen body
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> StatusDashboardScreen(
                            telemetry = telemetry,
                            clockDisplay = clockDisplay,
                            toleranceCountdown = toleranceCountdown,
                            isToleranceActive = isToleranceActive,
                            currentSchedule = currentSchedule,
                            activeSimulation = activeSimulation,
                            isScanning = isScanning,
                            scanProgress = scanProgress,
                            lastDetectedFaceName = lastDetectedFaceName,
                            onOpenSimulationDialog = { showSimulationDialog = true },
                            onOpenIpConfig = { showIpDialog = true },
                            onStartScan = { viewModel.startAutonomousAttendanceScan() },
                            onToggleSentinel = { viewModel.toggleSentinelMode(it) },
                            onTriggerTestSentinel = { viewModel.triggerTestSentinelMovement(it) }
                        )

                        1 -> AttendanceScreen(
                            attendanceRecords = attendanceRecords,
                            students = students,
                            summary = summary,
                            isScanning = isScanning,
                            scanProgress = scanProgress,
                            lastDetectedFaceName = lastDetectedFaceName,
                            onUpdateAttendanceStatus = { id, status, notes ->
                                viewModel.updateStudentAttendance(id, status, notes)
                            }
                        )

                        2 -> CleanerScreen(
                            telemetry = telemetry,
                            onManualMove = { viewModel.manualMoveRail(it) },
                            onHome = { viewModel.homeRail() },
                            onMoveToDoor = { viewModel.moveCarriageToDoor() },
                            onToggleSponge = { viewModel.toggleSpongeMotor() },
                            onStartCleanRoutine = { viewModel.startCleanRoutine(it) }
                        )

                        3 -> ReportsScreen(
                            currentDate = currentDate,
                            currentSchedule = currentSchedule,
                            summary = summary,
                            attendanceRecords = attendanceRecords,
                            sentinelLogs = sentinelLogs,
                            onGenerateReportText = { viewModel.generateReportText() }
                        )
                    }
                }
            }

            // Floating Push Alert Banner at top
            FloatingSentinelAlert(
                alert = sentinelAlert,
                onDismiss = { viewModel.dismissSentinelAlert() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 50.dp)
            )
        }
    }
}
