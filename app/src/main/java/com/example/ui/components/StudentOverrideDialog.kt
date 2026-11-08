package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.BrotexCyanPrimary
import com.example.ui.theme.BrotexNavyDark
import com.example.ui.theme.BrotexNavySurface
import com.example.ui.theme.BrotexNavySurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentOverrideDialog(
    record: AttendanceRecordEntity,
    onSaveStatus: (recordId: Long, newStatus: AttendanceStatus, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStatus by remember {
        mutableStateOf(
            try {
                AttendanceStatus.valueOf(record.status)
            } catch (_: Exception) {
                AttendanceStatus.PRESENTE
            }
        )
    }
    var notesText by remember { mutableStateOf(record.notes) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, BrotexNavySurfaceVariant, RoundedCornerShape(20.dp)),
            color = BrotexNavyDark
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Asistencia",
                            tint = BrotexCyanPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ajuste Manual de Asistencia",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = record.studentName,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Matrícula: ${record.studentId} • CBTIS 227 IA5AM",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Seleccionar nuevo estatus:",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AttendanceStatus.values().forEach { status ->
                        val isSelected = status == selectedStatus
                        val badgeColor = status.getBadgeColor()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) status.getContainerColor() else BrotexNavySurface)
                                .border(
                                    1.5.dp,
                                    if (isSelected) badgeColor else BrotexNavySurfaceVariant,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedStatus = status }
                                .padding(vertical = 10.dp)
                                .testTag("status_option_${status.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = status.label,
                                color = if (isSelected) badgeColor else Color.White.copy(alpha = 0.8f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Motivo / Observaciones (Opcional)") },
                    placeholder = { Text("Ej. Justificante médico o permiso escolar") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrotexCyanPrimary,
                        unfocusedBorderColor = BrotexNavySurfaceVariant,
                        focusedLabelColor = BrotexCyanPrimary,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("override_notes_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSaveStatus(record.id, selectedStatus, notesText)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_override_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrotexCyanPrimary,
                        contentColor = BrotexNavyDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Actualizar Estatus", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
