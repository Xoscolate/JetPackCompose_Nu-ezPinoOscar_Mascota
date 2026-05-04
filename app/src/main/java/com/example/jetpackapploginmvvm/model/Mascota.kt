package com.example.jetpackapploginmvvm.model

data class Mascota(
    val ownerUsername: String, // Nombre de usuario del dueño
    val nom: String = "",
    val fechaCreacion: Long = 0L,
    val hambreActual: Float = 1f,
    val energiaActual: Float = 1f,
    val ultimaActualizacion: Long = 0L,
    val ultimoEspectro: Long = 0L,
    val espectresActius: List<Int> = emptyList(), //Lista de espectros que guarda su posicion
    val tempsFiFelicitat: Long = 0L,
    val notificacioFamEnviada: Boolean = false,
    val estaDormint: Boolean = false,
    val estaViva: Boolean = true,
    val fondoActual: Int = 0 //Id del fondo
)