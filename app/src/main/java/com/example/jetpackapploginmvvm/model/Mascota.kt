package com.example.jetpackapploginmvvm.model

data class Mascota(
    val nom: String = "",
    val ultimaVegadaQueVaMenjar: Long = 0L,

    // --- NUEVO PARA EL SUEÑO EXACTO ---
    val ultimaActualitzacioEnergia: Long = 0L, // Controla el tiempo exacto que pasa
    val energiaActual: Float = 1f,             // Guarda el nivel de la barra (de 0.0 a 1.0)

    // --- PREPARADO PARA EL FUTURO (Espectros y Simón) ---
    val nivellFelicitat: Int = 100,
    val espectresActius: Int = 0,

    // --- ESTADOS ---
    val estaDormint: Boolean = false,
    val estaViva: Boolean = true
)