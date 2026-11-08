package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DoorSliding
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hardware.HardwareMode
import com.example.ui.theme.BrotexCyanAccent
import com.example.ui.theme.BrotexCyanPrimary
import com.example.ui.theme.BrotexNavyDark
import com.example.ui.theme.BrotexNavySurface
import com.example.ui.theme.BrotexNavySurfaceVariant
import com.example.ui.theme.StatusLate
import com.example.ui.theme.StatusPresent

@Composable
fun BrotexRailVisualizer(
    carriagePositionMm: Int,
    railLengthMm: Int = 2400,
    currentMode: HardwareMode,
    isSpongeActive: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = (carriagePositionMm.toFloat() / railLengthMm.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 200),
        label = "carriageProgress"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BrotexNavySurfaceVariant, RoundedCornerShape(16.dp)),
        color = BrotexNavyDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PrecisionManufacturing,
                        contentDescription = "Riel BROTEX",
                        tint = BrotexCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Riel Superior de Pizarrón (2.40 m)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                val modeBadge = when (currentMode) {
                    HardwareMode.AUTONOMOUS_SCAN -> "Escaneo Facial" to BrotexCyanPrimary
                    HardwareMode.SENTINEL_DOOR -> "Centinela Puerta" to StatusPresent
                    HardwareMode.MANUAL_CLEANING -> "Borrando Pizarrón" to StatusLate
                    HardwareMode.HOMING -> "Home (0 mm)" to Color(0xFF64FFDA)
                    HardwareMode.STANDBY -> "En Espera" to Color.Gray
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(modeBadge.second.copy(alpha = 0.2f))
                        .border(1.dp, modeBadge.second.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = modeBadge.first,
                        color = modeBadge.second,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Whiteboard & Rail Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrotexNavySurface)
                    .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // Acrylic whiteboard surface background
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF132438), Color(0xFF0F1B2A))
                        ),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(12f, 12f)
                    )

                    // Top aluminum rail bar
                    val railTop = 18f
                    val railHeight = 14f
                    drawRoundRect(
                        color = Color(0xFF455A64),
                        topLeft = Offset(16f, railTop),
                        size = Size(w - 32f, railHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // Inner metallic groove
                    drawLine(
                        color = Color(0xFF90A4AE),
                        start = Offset(20f, railTop + 7f),
                        end = Offset(w - 20f, railTop + 7f),
                        strokeWidth = 3f
                    )

                    // Door Threshold ROI Marker at ~91% (2200mm / 2400mm)
                    val doorX = 16f + (w - 32f) * (2200f / 2400f)
                    drawLine(
                        color = Color(0xFF00E676).copy(alpha = 0.6f),
                        start = Offset(doorX, railTop + railHeight),
                        end = Offset(doorX, h - 8f),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )

                    // BROTEX Carriage Unit
                    val carriageWidth = 56f
                    val carriageHeight = 50f
                    val carriageX = 16f + (w - 32f - carriageWidth) * animatedProgress
                    val carriageY = railTop - 6f

                    // Carriage body
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF263238), Color(0xFF102027))
                        ),
                        topLeft = Offset(carriageX, carriageY),
                        size = Size(carriageWidth, carriageHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = BrotexCyanPrimary,
                        topLeft = Offset(carriageX, carriageY),
                        size = Size(carriageWidth, carriageHeight),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                    )

                    // Camera lens circle
                    val lensCenter = Offset(carriageX + carriageWidth / 2f, carriageY + 18f)
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 8f,
                        center = lensCenter
                    )
                    drawCircle(
                        color = Color(0xFF004D58),
                        radius = 4f,
                        center = lensCenter
                    )

                    // Bottom eraser sponge block
                    val spongeColor = if (isSpongeActive) Color(0xFFFFD600) else Color(0xFF78909C)
                    drawRoundRect(
                        color = spongeColor,
                        topLeft = Offset(carriageX + 4f, carriageY + carriageHeight - 4f),
                        size = Size(carriageWidth - 8f, 10f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )

                    // Camera FOV light cone beam if scanning or sentinel
                    if (currentMode == HardwareMode.AUTONOMOUS_SCAN || currentMode == HardwareMode.SENTINEL_DOOR) {
                        val beamColor = if (currentMode == HardwareMode.AUTONOMOUS_SCAN) {
                            BrotexCyanPrimary.copy(alpha = 0.25f)
                        } else {
                            Color(0xFF00E676).copy(alpha = 0.25f)
                        }
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(lensCenter.x, lensCenter.y + 8f)
                            lineTo(lensCenter.x - 30f, h - 6f)
                            lineTo(lensCenter.x + 30f, h - 6f)
                            close()
                        }
                        drawPath(path, beamColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rail coordinates & Markers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0 mm (Home)",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
                Text(
                    text = "Posición actual: $carriagePositionMm mm (${(progress * 100).toInt()}%)",
                    color = BrotexCyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DoorSliding,
                        contentDescription = "Puerta",
                        tint = StatusPresent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Puerta (2200 mm)",
                        color = StatusPresent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
