package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusJustified
import com.example.ui.theme.StatusJustifiedBg
import com.example.ui.theme.StatusLate
import com.example.ui.theme.StatusLateBg
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg

enum class AttendanceStatus(val label: String, val code: String) {
    PRESENTE("Presente", "P"),
    RETARDO("Retardo", "R"),
    FALTA("Falta", "F"),
    JUSTIFICADO("Justificado", "J");

    fun getBadgeColor(): Color = when (this) {
        PRESENTE -> StatusPresent
        RETARDO -> StatusLate
        FALTA -> StatusAbsent
        JUSTIFICADO -> StatusJustified
    }

    fun getContainerColor(): Color = when (this) {
        PRESENTE -> StatusPresentBg
        RETARDO -> StatusLateBg
        FALTA -> StatusAbsentBg
        JUSTIFICADO -> StatusJustifiedBg
    }
}
