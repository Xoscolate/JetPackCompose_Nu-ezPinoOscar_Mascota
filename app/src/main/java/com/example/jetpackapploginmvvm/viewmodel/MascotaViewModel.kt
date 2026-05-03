package com.example.jetpackapploginmvvm.viewmodel

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackapploginmvvm.R
import com.example.jetpackapploginmvvm.model.Mascota
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class MascotaViewModel(application: Application) : AndroidViewModel(application) {

    // --- VARIABLES DE ESTADO ---
    private var currentUser: String = ""
    private val TIEMPO_HAMBRE = 10 * 1000L
    private val TIEMPO_RECUPERACION_SUENO = 10 * 60 * 1000L
    private val TIEMPO_ESPECTRO = 10 * 1000L

    private var mediaPlayer: MediaPlayer? = null
    private val _mascota = MutableStateFlow<Mascota?>(null)
    val mascota = _mascota.asStateFlow()

    private val _estaComiendo = MutableStateFlow(false)
    val estaComiendo = _estaComiendo.asStateFlow()

    private val _nivelHambre = MutableStateFlow(1f)
    val nivelHambre = _nivelHambre.asStateFlow()

    private val _nivelSueno = MutableStateFlow(1f)
    val nivelSueno = _nivelSueno.asStateFlow()

    private var tiempoInicioSimon = 0L

    // --- FUNCIÓN QUE TE DABA ERROR EN LA PANTALLA DE MUERTE ---
    fun resetearJuego() {
        _mascota.value = null
        _nivelHambre.value = 1f
        _nivelSueno.value = 1f
        controlarMusica(false)
    }

    // --- LÓGICA DE CARGA Y CREACIÓN (SIN ROOM PARA DEMONIO) ---
    fun cargarMascotaDeUsuario(username: String) {
        currentUser = username
    }

    fun crearMascota(nombre: String) {
        val t = System.currentTimeMillis()
        _mascota.value = Mascota(
            ownerUsername = currentUser,
            nom = nombre,
            hambreActual = 1f,
            energiaActual = 1f,
            ultimaActualizacion = t,
            ultimoEspectro = t
        )
        _nivelHambre.value = 1f
        _nivelSueno.value = 1f
    }

    // --- FUNCIONES DEL SIMÓN ---
    fun entrarAlSimon() {
        tiempoInicioSimon = System.currentTimeMillis()
    }

    fun salirDelSimon() {
        val m = _mascota.value ?: return
        val tiempoJugado = System.currentTimeMillis() - tiempoInicioSimon
        if (tiempoJugado >= 2 * 60 * 1000L) {
            _mascota.value = m.copy(tempsFiFelicitat = System.currentTimeMillis() + (5 * 60 * 1000L))
        }
    }

    fun jugarSimon() {
        val m = _mascota.value ?: return
        _mascota.value = m.copy(tempsFiFelicitat = System.currentTimeMillis() + (5 * 60 * 1000L))
    }

    // --- LÓGICA DE JUEGO ---
    fun darDeComer() {
        val m = _mascota.value ?: return
        if (_nivelSueno.value <= 0f || m.estaDormint) return
        actualizarCalculos()
        _mascota.value = _mascota.value?.copy(hambreActual = 1f, notificacioFamEnviada = false)
        viewModelScope.launch {
            _estaComiendo.value = true
            delay(2400)
            _estaComiendo.value = false
        }
    }

    fun toggleDormir() {
        actualizarCalculos()
        val m = _mascota.value ?: return
        _mascota.value = m.copy(estaDormint = !m.estaDormint)
        controlarMusica(!m.estaDormint)
    }

    fun actualizarEstado() {
        actualizarCalculos()
    }

    private fun actualizarCalculos() {
        val m = _mascota.value ?: return
        if (!m.estaViva) return

        val t = System.currentTimeMillis()
        val diffTiempo = t - m.ultimaActualizacion

        var nHambre = (m.hambreActual - (diffTiempo.toFloat() / TIEMPO_HAMBRE)).coerceIn(0f, 1f)
        var nEnergia = if (m.estaDormint) {
            (m.energiaActual + (diffTiempo.toFloat() / TIEMPO_RECUPERACION_SUENO)).coerceIn(0f, 1f)
        } else {
            (m.energiaActual - (diffTiempo.toFloat() / TIEMPO_RECUPERACION_SUENO)).coerceIn(0f, 1f)
        }

        _mascota.value = m.copy(
            hambreActual = nHambre,
            energiaActual = nEnergia,
            ultimaActualizacion = t,
            estaViva = nHambre > 0f
        )
        _nivelHambre.value = nHambre
        _nivelSueno.value = nEnergia
    }

    // --- FUNCIONES EXTRA PARA EVITAR ERRORES DE NAVEGACIÓN ---
    fun cambiarFondo(nuevoFondo: Int) {
        val m = _mascota.value ?: return
        _mascota.value = m.copy(fondoActual = nuevoFondo)
    }

    fun eliminarEspectro(posicionId: Int) {
        val m = _mascota.value ?: return
        val nuevaLista = m.espectresActius.filter { it != posicionId }
        _mascota.value = m.copy(espectresActius = nuevaLista)
    }

    fun guardarPartida() {
        // Función vacía para que la navegación no falle al llamarla
    }

    fun comprobarSiSigueViva(): Boolean = _mascota.value?.estaViva == true

    private fun controlarMusica(reproducir: Boolean) {
        if (reproducir) {
            try {
                mediaPlayer = MediaPlayer.create(getApplication(), R.raw.musica_dormir).apply {
                    isLooping = true; start()
                }
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }
}