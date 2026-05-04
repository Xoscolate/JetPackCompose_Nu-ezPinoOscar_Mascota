package com.example.jetpackapploginmvvm.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "mascotas")
data class Mascota(
    @PrimaryKey val ownerUsername: String, // Nombre de usuario del dueño
    val nom: String = "",
    val fechaCreacion: Long = 0L,
    val hambreActual: Float = 1f,
    val energiaActual: Float = 1f,
    val ultimaActualizacion: Long = 0L,
    val ultimoEspectro: Long = 0L,           // Recuperado para que no pete
    val tempsFiFelicitat: Long = 0L,         // Recuperado para que no pete
    val notificacioFamEnviada: Boolean = false, // Recuperado para que no pete
    val estaDormint: Boolean = false,
    val estaViva: Boolean = true,
    val fondoActual: Int = 0,

    @Ignore val espectresActius: List<Int> = emptyList() // Ignorado por la Base de Datos
) {
    // Constructor secundario obligatorio para que Room ignore la lista
    constructor(
        ownerUsername: String, nom: String, fechaCreacion: Long, hambreActual: Float,
        energiaActual: Float, ultimaActualizacion: Long, ultimoEspectro: Long,
        tempsFiFelicitat: Long, notificacioFamEnviada: Boolean, estaDormint: Boolean,
        estaViva: Boolean, fondoActual: Int
    ) : this(
        ownerUsername, nom, fechaCreacion, hambreActual, energiaActual,
        ultimaActualizacion, ultimoEspectro, tempsFiFelicitat, notificacioFamEnviada,
        estaDormint, estaViva, fondoActual, emptyList()
    )
}