package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ClassSchedule
import com.example.hardware.BrotexTelemetry
import com.example.hardware.ConnectionStatus
import com.example.hardware.HardwareMode
import com.example.ui.ScheduleSimulationMode
import com.example.ui.components.BrotexRailVisualizer
import com.example.ui.components.ToleranceTimerCard
import com.example.ui.theme.BrotexCyanAccent
import com.example.ui.theme.BrotexCyanPrimary
import com.example.ui.theme.BrotexNavyDark
import com.example.ui.theme.BrotexNavySurface
import com.example.ui.theme.BrotexNavySurfaceVariant
import com.example.ui.theme.StatusLate
import com.example.ui.theme.StatusPresent

@Composable
fun StatusDashboardScreen(
    telemetry: BrotexTelemetry,
    clockDisplay: String,
    toleranceCountdown: String,
    isToleranceActive: Boolean,
    currentSchedule: ClassSchedule,
    activeSimulation: ScheduleSimulationMode,
    isScanning: Boolean,
    scanProgress: Float,
    lastDetectedFaceName: String?,
    onOpenSimulationDialog: () -> Unit,
    onOpenIpConfig: () -> Unit,
    onStartScan: () -> Unit,
    onToggleSentinel: (Boolean) -> Unit,
    onTriggerTestSentinel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Mechatronics Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, BrotexNavySurfaceVariant, RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_brotex_hero),
                    contentDescription = "Prototipo BROTEX TH0.0",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    BrotexNavyDark.copy(alpha = 0.85f),
                                    BrotexNavyDark
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrotexCyanPrimary)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BROTEX TH0.0",
                                color = BrotexNavyDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CBTIS No. 227 • GRUPO IA5AM",
                            color = BrotexCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "La energía en los procesos de la vida",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Control Remoto y Pase de Lista Facial Autónomo",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Telemetry Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Connection Card (Wi-Fi ESP32-CAM)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenIpConfig() }
                        .testTag("telemetry_wifi_card"),
                    colors = CardDefaults.cardColors(containerColor = BrotexNavyDark),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrotexNavySurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val wifiColor = when (telemetry.connectionStatus) {
                                ConnectionStatus.CONNECTED_HARDWARE -> StatusPresent
                                ConnectionStatus.SIMULATED_ONLINE -> BrotexCyanPrimary
                                else -> Color(0xFFFFD600)
                            }
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Wi-Fi",
                                tint = wifiColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configurar IP",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = telemetry.esp32Ip,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (telemetry.connectionStatus == ConnectionStatus.CONNECTED_HARDWARE) "Hardware Real" else "ESP32-CAM Online",
                            color = BrotexCyanPrimary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Battery Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = BrotexNavyDark),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrotexNavySurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = "Batería",
                                tint = StatusPresent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${telemetry.batteryVoltage} V",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${telemetry.batteryPercent}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Batería LiPo 3S",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Motor Stepper Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = BrotexNavyDark),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrotexNavySurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PrecisionManufacturing,
                                contentDescription = "NEMA 17",
                                tint = BrotexCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            if (telemetry.isStepperMoving) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(StatusPresent)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${telemetry.carriagePositionMm} mm",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Motor NEMA 17",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Academic Tolerance Countdown & Rules
        item {
            ToleranceTimerCard(
                clockDisplay = clockDisplay,
                toleranceCountdown = toleranceCountdown,
                isToleranceActive = isToleranceActive,
                currentSchedule = currentSchedule,
                activeSimulation = activeSimulation,
                onOpenSimulationDialog = onOpenSimulationDialog
            )
        }

        // Animated Rail Visualizer
        item {
            BrotexRailVisualizer(
                carriagePositionMm = telemetry.carriagePositionMm,
                railLengthMm = telemetry.railLengthMm,
                currentMode = telemetry.currentMode,
                isSpongeActive = telemetry.isSpongeMotorOn
            )
        }

        // Autonomous Scan Action Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BrotexNavyDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isScanning) BrotexCyanPrimary else BrotexNavySurfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MÓDULO DE PASE DE LISTA AUTÓNOMO",
                                color = BrotexCyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Escaneo Facial sobre Riel (OpenCV)",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Cámara",
                            tint = BrotexCyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "El motor NEMA 17 desplaza el carro a lo largo del riel capturando los rostros del grupo IA5AM y asignando estatus de acuerdo a la tolerancia académica.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isScanning) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = lastDetectedFaceName ?: "Escaneando...",
                                    color = BrotexCyanAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(scanProgress * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { scanProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = BrotexCyanPrimary,
                                trackColor = BrotexNavySurface
                            )
                        }
                    } else {
                        Button(
                            onClick = onStartScan,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_scan_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrotexCyanPrimary,
                                contentColor = BrotexNavyDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Iniciar",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Iniciar Pase de Lista Autónomo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Sentinel Mode Card & Door Simulation Triggers
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BrotexNavyDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrotexNavySurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Centinela",
                                tint = StatusPresent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MODO CENTINELA (PUERTA)",
                                    color = StatusPresent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Vigilancia de Umbral de Acceso",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        val isSentinelActive = telemetry.currentMode == HardwareMode.SENTINEL_DOOR
                        Switch(
                            checked = isSentinelActive,
                            onCheckedChange = { onToggleSentinel(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BrotexNavyDark,
                                checkedTrackColor = StatusPresent,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = BrotexNavySurface
                            ),
                            modifier = Modifier.testTag("sentinel_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Fija la cámara en la coordenada de la puerta (2200 mm) con sustracción de fondo y ROI para detectar salidas o ingresos en tiempo real con alertas flotantes.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Simular detección inmediata de puerta (Docente):",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onTriggerTestSentinel("ENTRADA") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sim_entry_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusPresent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = "Entrada",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simular Entrada", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onTriggerTestSentinel("SALIDA") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sim_exit_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusLate),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Salida",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simular Salida", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
