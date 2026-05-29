package com.example.logisticareparto.data.models

import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Client(
    val id: String = "",
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
    fun getEstadoHorario(): Pair<String, Boolean> {
        if (es24) return Pair("Abierto 24hs", true)
        if (apertura.isEmpty() || cierre.isEmpty()) return Pair("Horario no especificado", false)

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
                Pair("Abierto ahora (Cierra $cierre)", true)
            } else {
                Pair("Cerrado ahora (Abre $apertura)", false)
            }
        } catch (e: Exception) {
            Pair("Horario: $apertura - $cierre", false)
        }
    }
}
