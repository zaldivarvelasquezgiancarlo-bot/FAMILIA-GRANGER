package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DoorSliding
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hardware.BrotexTelemetry
import com.example.hardware.HardwareMode
import com.example.ui.components.BrotexRailVisualizer
import com.example.ui.theme.BrotexCyanAccent
import com.example.ui.theme.BrotexCyanPrimary
import com.example.ui.theme.BrotexNavyDark
import com.example.ui.theme.BrotexNavySurface
import com.example.ui.theme.BrotexNavySurfaceVariant
import com.example.ui.theme.StatusLate
import com.example.ui.theme.StatusPresent

@Composable
fun CleanerScreen(
    telemetry: BrotexTelemetry,
    onManualMove: (Int) -> Unit,
    onHome: () -> Unit,
    onMoveToDoor: () -> Unit,
    onToggleSponge: () -> Unit,
    onStartCleanRoutine: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var stepSizeMm by remember { mutableFloatStateOf(100f) }

    // Sponge rotation animation if active
    val infiniteTransition = rememberInfiniteTransition(label = "spongeSpin")
    val spongeRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cleaner_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, BrotexNavySurfaceVariant, RoundedCornerShape(16.dp)),
                color = BrotexNavyDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MÓDULO DE LIMPIEZA DE PIZARRÓN",
                                color = BrotexCyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Borrador Inteligente BROTEX TH0.0",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = "Limpieza",
                            tint = BrotexCyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Control del rodillo limpiador y accionamiento del motor NEMA 17 para el borrado automático o manual del pizarrón de acrílico en el aula del CBTIS No. 227.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Live Rail Visualizer
        item {
            BrotexRailVisualizer(
                carriagePositionMm = telemetry.carriagePositionMm,
                railLengthMm = telemetry.railLengthMm,
                currentMode = telemetry.currentMode,
                isSpongeActive = telemetry.isSpongeMotorOn
            )
        }

        // Automatic Cleaning Modes Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BrotexNavyDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrotexNavySurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MODOS AUTOMÁTICOS DE BORRADO",
                        color = BrotexCyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Full Board Wipe
                        Button(
                            onClick = { onStartCleanRoutine(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("clean_full_board_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrotexCyanPrimary,
                                contentColor = BrotexNavyDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Limpieza Total",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Limpieza Total (100%)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Quick Central Wipe
                        OutlinedButton(
                            onClick = { onStartCleanRoutine(false) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("clean_quick_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrotexCyanAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Limpieza Rápida",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Limpieza Rápida", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Sponge Motor Control Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BrotexNavyDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrotexNavySurfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (telemetry.isSpongeMotorOn) StatusLate.copy(alpha = 0.2f) else BrotexNavySurface)
                                .border(
                                    1.dp,
                                    if (telemetry.isSpongeMotorOn) StatusLate else BrotexNavySurfaceVariant,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Rodillo",
                                tint = if (telemetry.isSpongeMotorOn) StatusLate else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(if (telemetry.isSpongeMotorOn) spongeRotation else 0f)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Motor de Rodillo Limpiador",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (telemetry.isSpongeMotorOn) "Rodillo de borrado GIRANDO" else "Rodillo APAGADO",
                                color = if (telemetry.isSpongeMotorOn) StatusLate else Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Switch(
                        checked = telemetry.isSpongeMotorOn,
                        onCheckedChange = { onToggleSponge() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BrotexNavyDark,
                            checkedTrackColor = StatusLate,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = BrotexNavySurface
                        ),
                        modifier = Modifier.testTag("sponge_motor_switch")
                    )
                }
            }
        }

        // Stepper Motor Jog / Manual Movement Card
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
                        Text(
                            text = "DESPLAZAMIENTO MANUAL (NEMA 17)",
                            color = BrotexCyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Paso: ${stepSizeMm.toInt()} mm",
                            color = BrotexCyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step size slider
                    Slider(
                        value = stepSizeMm,
                        onValueChange = { stepSizeMm = it },
                        valueRange = 20f..300f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = BrotexCyanPrimary,
                            activeTrackColor = BrotexCyanPrimary,
                            inactiveTrackColor = BrotexNavySurface
                        ),
                        modifier = Modifier.testTag("step_size_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Jog Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Move Left
                        FilledTonalButton(
                            onClick = { onManualMove(-stepSizeMm.toInt()) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("jog_left_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Izquierda",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Izquierda")
                        }

                        // Move Right
                        FilledTonalButton(
                            onClick = { onManualMove(stepSizeMm.toInt()) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("jog_right_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Derecha")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Derecha",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Presets Home & Door
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onHome,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64FFDA)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ir a Home (0 mm)", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onMoveToDoor,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("move_door_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusPresent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoorSliding,
                                contentDescription = "Puerta",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ir a Puerta (Centinela)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
