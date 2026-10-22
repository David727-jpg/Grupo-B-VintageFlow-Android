package com.vintageflow.app.ui.navigation

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

/** Icono de recibo (contorno) para la opción Reservas; no viene en material-icons-core. */
val IconoReservas: ImageVector by lazy {
    materialIcon(name = "Reservas") {
        materialPath {
            moveTo(6f, 3f)
            horizontalLineTo(18f)
            verticalLineTo(21f)
            lineTo(15f, 19f)
            lineTo(12f, 21f)
            lineTo(9f, 19f)
            lineTo(6f, 21f)
            close()
            moveTo(8f, 5f)
            verticalLineTo(17.9f)
            lineTo(9f, 17.3f)
            lineTo(12f, 19.3f)
            lineTo(15f, 17.3f)
            lineTo(16f, 17.9f)
            verticalLineTo(5f)
            close()
            moveTo(9.5f, 7f)
            horizontalLineTo(14.5f)
            verticalLineTo(8.5f)
            horizontalLineTo(9.5f)
            close()
            moveTo(9.5f, 10f)
            horizontalLineTo(14.5f)
            verticalLineTo(11.5f)
            horizontalLineTo(9.5f)
            close()
        }
    }
}
