package com.example.logisticareparto.data.models

import java.time.LocalTime
import java.time.format.DateTimeFormatter

sealed class ScheduleState {
    object Open24h : ScheduleState()
    data class OpenNow(val closeTime: String) : ScheduleState()
    data class ClosedNow(val openTime: String) : ScheduleState()
    object NotSpecified : ScheduleState()
    data class Range(val openTime: String, val closeTime: String) : ScheduleState()
}

data class Client(
    val id: String = "",
    val codigoCliente: String = "",
    val cliente: String = "",
    val direccion: String = "",
    val dias: List<String> = emptyList(),
    val reparto: Int = 0,
    val cuil: List<String> = emptyList(),
    val contacto: List<String> = emptyList(),
    val apertura: String = "",
    val cierre: String = "",
    val es24: Boolean = false,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val imagenUrl: String = ""
) {
    fun getScheduleState(): ScheduleState {
        if (es24) return ScheduleState.Open24h
        if (apertura.isEmpty() || cierre.isEmpty()) return ScheduleState.NotSpecified

        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val ahora = LocalTime.now()
            val inicio = LocalTime.parse(apertura, formatter)
            val fin = LocalTime.parse(cierre, formatter)

            val estaAbierto = if (inicio.isBefore(fin)) {
                ahora.isAfter(inicio) && ahora.isBefore(fin)
            } else {
                ahora.isAfter(inicio) || ahora.isBefore(fin)
            }

            if (estaAbierto) {
                ScheduleState.OpenNow(cierre)
            } else {
                ScheduleState.ClosedNow(apertura)
            }
        } catch (e: Exception) {
            ScheduleState.Range(apertura, cierre)
        }
    }

    @Deprecated("Use getScheduleState instead")
    fun getEstadoHorario(): Pair<String, Boolean> {
        val state = getScheduleState()
        return when (state) {
            is ScheduleState.Open24h -> Pair("Abierto 24hs", true)
            is ScheduleState.OpenNow -> Pair("Abierto ahora (Cierra ${state.closeTime})", true)
            is ScheduleState.ClosedNow -> Pair("Cerrado ahora (Abre ${state.openTime})", false)
            is ScheduleState.NotSpecified -> Pair("Horario no especificado", false)
            is ScheduleState.Range -> Pair("Horario: ${state.openTime} - ${state.closeTime}", false)
        }
    }
}
