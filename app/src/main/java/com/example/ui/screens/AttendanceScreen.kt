package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentEntity
import com.example.ui.AttendanceSummary
import com.example.ui.components.StudentOverrideDialog
import com.example.ui.theme.BrotexCyanAccent
import com.example.ui.theme.BrotexCyanPrimary
import com.example.ui.theme.BrotexNavyDark
import com.example.ui.theme.BrotexNavySurface
import com.example.ui.theme.BrotexNavySurfaceVariant
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusJustified
import com.example.ui.theme.StatusLate
import com.example.ui.theme.StatusPresent

@Composable
fun AttendanceScreen(
    attendanceRecords: List<AttendanceRecordEntity>,
    students: List<StudentEntity>,
    summary: AttendanceSummary,
    isScanning: Boolean,
    scanProgress: Float,
    lastDetectedFaceName: String?,
    onUpdateAttendanceStatus: (recordId: Long, newStatus: AttendanceStatus, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<AttendanceStatus?>(null) }
    var editingRecord by remember { mutableStateOf<AttendanceRecordEntity?>(null) }

    // Map student ID to student entity for initials & colors
    val studentMap = remember(students) { students.associateBy { it.studentId } }

    val filteredRecords = remember(attendanceRecords, searchQuery, selectedFilter) {
        attendanceRecords.filter { record ->
            val matchesSearch = searchQuery.isBlank() ||
                    record.studentName.contains(searchQuery, ignoreCase = true) ||
                    record.studentId.contains(searchQuery, ignoreCase = true)

            val matchesFilter = selectedFilter == null || record.status == selectedFilter?.name

            matchesSearch && matchesFilter
        }
    }

    if (editingRecord != null) {
        StudentOverrideDialog(
            record = editingRecord!!,
            onSaveStatus = { id, status, notes ->
                onUpdateAttendanceStatus(id, status, notes)
                editingRecord = null
            },
            onDismiss = { editingRecord = null }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Metrics Card
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
                                text = "TABLA DINÁMICA DE ASISTENCIA",
                                color = BrotexCyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Grupo IA5AM (30 Alumnos)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        // Percentage badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrotexCyanPrimary.copy(alpha = 0.15f))
                                .border(1.dp, BrotexCyanPrimary, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "%.1f%% Asistencia".format(summary.attendancePercentage),
                                color = BrotexCyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Color code counter row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Present
                        MetricCounterBox(
                            label = "Presente",
                            count = summary.presentCount,
                            color = StatusPresent,
                            modifier = Modifier.weight(1f)
                        )
                        // Late
                        MetricCounterBox(
                            label = "Retardo",
                            count = summary.lateCount,
                            color = StatusLate,
                            modifier = Modifier.weight(1f)
                        )
                        // Absent
                        MetricCounterBox(
                            label = "Falta",
                            count = summary.absentCount,
                            color = StatusAbsent,
                            modifier = Modifier.weight(1f)
                        )
                        // Justified
                        MetricCounterBox(
                            label = "Justificado",
                            count = summary.justifiedCount,
                            color = StatusJustified,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Active scan notification banner if scanning
        if (isScanning) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, BrotexCyanPrimary, RoundedCornerShape(14.dp)),
                    color = BrotexNavySurface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Escaneo",
                                tint = BrotexCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Escaneo Facial en Progreso (${(scanProgress * 100).toInt()}%)",
                                color = BrotexCyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        if (lastDetectedFaceName != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lastDetectedFaceName,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { scanProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = BrotexCyanPrimary,
                            trackColor = BrotexNavyDark
                        )
                    }
                }
            }
        }

        // Search & Filter Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nombre o matrícula...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrotexCyanPrimary,
                        unfocusedBorderColor = BrotexNavySurfaceVariant,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.4f)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_search_input")
                )

                // Filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("Todos (${attendanceRecords.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrotexCyanPrimary,
                                selectedLabelColor = BrotexNavyDark,
                                containerColor = BrotexNavyDark,
                                labelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == null,
                                borderColor = BrotexNavySurfaceVariant,
                                selectedBorderColor = BrotexCyanPrimary
                            )
                        )
                    }

                    items(AttendanceStatus.values()) { status ->
                        val isSelected = selectedFilter == status
                        val badgeColor = status.getBadgeColor()
                        val count = when (status) {
                            AttendanceStatus.PRESENTE -> summary.presentCount
                            AttendanceStatus.RETARDO -> summary.lateCount
                            AttendanceStatus.FALTA -> summary.absentCount
                            AttendanceStatus.JUSTIFICADO -> summary.justifiedCount
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedFilter = if (isSelected) null else status
                            },
                            label = { Text("${status.label} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = badgeColor,
                                selectedLabelColor = BrotexNavyDark,
                                containerColor = BrotexNavyDark,
                                labelColor = badgeColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = BrotexNavySurfaceVariant,
                                selectedBorderColor = badgeColor
                            )
                        )
                    }
                }
            }
        }

        // Student List Items
        if (filteredRecords.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron alumnos con los criterios seleccionados.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(filteredRecords, key = { it.id }) { record ->
                val student = studentMap[record.studentId]
                val currentStatus = try {
                    AttendanceStatus.valueOf(record.status)
                } catch (_: Exception) {
                    AttendanceStatus.FALTA
                }

                StudentAttendanceCard(
                    record = record,
                    student = student,
                    status = currentStatus,
                    onEditClick = { editingRecord = record }
                )
            }
        }
    }
}

@Composable
fun MetricCounterBox(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$count",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StudentAttendanceCard(
    record: AttendanceRecordEntity,
    student: StudentEntity?,
    status: AttendanceStatus,
    onEditClick: () -> Unit
) {
    val badgeColor = status.getBadgeColor()
    val initials = student?.avatarInitials ?: "AL"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
            .testTag("student_card_${record.studentId}"),
        colors = CardDefaults.cardColors(containerColor = BrotexNavyDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrotexNavySurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(student?.avatarColorHex ?: 0xFF00E5FF).copy(alpha = 0.25f))
                    .border(1.5.dp, Color(student?.avatarColorHex ?: 0xFF00E5FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Student Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.studentName,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(
                    text = "Matrícula: ${record.studentId}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (record.timeRecorded != "--:--:--" && record.timeRecorded.isNotEmpty()) {
                    Text(
                        text = "Hora de registro: ${record.timeRecorded} • Coord: ${record.detectionCoordinateMm} mm",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
                if (record.notes.isNotEmpty()) {
                    Text(
                        text = record.notes,
                        color = BrotexCyanAccent.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status Badge (Verde, Amarillo, Rojo, Azul)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(status.getContainerColor())
                    .border(1.dp, badgeColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = status.label,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
