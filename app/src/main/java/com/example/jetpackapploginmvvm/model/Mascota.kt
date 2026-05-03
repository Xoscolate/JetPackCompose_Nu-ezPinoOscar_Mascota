package com.example.jetpackapploginmvvm.model

data class Mascota(
    val ownerUsername: String, // Puedes dejarlo por si lo usas en la UI
    val nom: String = "",
    val hambreActual: Float = 1f,
    val energiaActual: Float = 1f,
    val ultimaActualizacion: Long = 0L,
    val ultimoEspectro: Long = 0L,
    val espectresActius: List<Int> = emptyList(),
    val tempsFiFelicitat: Long = 0L,
    val notificacioFamEnviada: Boolean = false,
    val estaDormint: Boolean = false,
    val estaViva: Boolean = true,
    val fondoActual: Int = 0
)