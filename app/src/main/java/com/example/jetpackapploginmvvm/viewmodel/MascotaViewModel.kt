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

class MascotaViewModel(application: Application) : AndroidViewModel(application) {

    // Configuración de tiempos
    private val TIEMPO_HAMBRE = 60 * 1000L // 1 minuto para morir (ajustar luego)
    private val TIEMPO_RECUPERACION_SUENO = 10 * 60 * 1000L // 10 minutos para 0% -> 100%

    private var mediaPlayer: MediaPlayer? = null

    private val _mascota = MutableStateFlow<Mascota?>(null)
    val mascota = _mascota.asStateFlow()

    private val _estaComiendo = MutableStateFlow(false)
    val estaComiendo = _estaComiendo.asStateFlow()

    // Flujos para las barras de la UI
    private val _nivelHambre = MutableStateFlow(1f)
    val nivelHambre = _nivelHambre.asStateFlow()

    private val _nivelSueno = MutableStateFlow(1f)
    val nivelSueno = _nivelSueno.asStateFlow()

    fun crearMascota(nombre: String) {
        val tiempoActual = System.currentTimeMillis()
        _mascota.value = Mascota(
            nom = nombre,
            ultimaVegadaQueVaMenjar = tiempoActual,
            ultimaActualitzacioEnergia = tiempoActual,
            energiaActual = 1f, // Empieza a tope
            nivellFelicitat = 100,
            espectresActius = 0,
            estaDormint = false,
            estaViva = true
        )
    }

    fun darDeComer() {
        val mActual = _mascota.value ?: return
        // No come si no tiene energía o si ya está durmiendo
        if (_nivelSueno.value <= 0f || mActual.estaDormint) return

        _mascota.value = mActual.copy(ultimaVegadaQueVaMenjar = System.currentTimeMillis())

        viewModelScope.launch {
            _estaComiendo.value = true
            delay(2400)
            _estaComiendo.value = false
        }
    }

    // FUNCIÓN CLAVE: Calcula la energía exacta antes de cambiar el estado (Dormir/Despertar)
    fun toggleDormir() {
        val m = _mascota.value ?: return
        val tiempoActual = System.currentTimeMillis()

        // 1. Calculamos cuánta energía ha ganado o perdido desde el último "tick"
        val diffTiempo = tiempoActual - m.ultimaActualitzacioEnergia
        var energiaCalculada = m.energiaActual

        if (m.estaDormint) {
            energiaCalculada += diffTiempo.toFloat() / TIEMPO_RECUPERACION_SUENO
        } else {
            energiaCalculada -= diffTiempo.toFloat() / TIEMPO_RECUPERACION_SUENO
        }

        val nuevoEstado = !m.estaDormint

        // 2. Guardamos la foto fija de la energía en este momento
        _mascota.value = m.copy(
            estaDormint = nuevoEstado,
            energiaActual = energiaCalculada.coerceIn(0f, 1f),
            ultimaActualitzacioEnergia = tiempoActual
        )

        controlarMusica(nuevoEstado)
    }

    private fun controlarMusica(reproducir: Boolean) {
        if (reproducir) {
            try {
                mediaPlayer = MediaPlayer.create(getApplication(), R.raw.musica_dormir).apply {
                    isLooping = true
                    start()
                }
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Se ejecuta cada 500ms desde la UI
    fun actualizarEstado() {
        val m = _mascota.value ?: return
        if (!m.estaViva) return

        val tiempoActual = System.currentTimeMillis()

        // CÁLCULO DE HAMBRE (Basado en la última comida)
        val diffHambre = tiempoActual - m.ultimaVegadaQueVaMenjar
        val nuevoHambre = (1f - (diffHambre.toFloat() / TIEMPO_HAMBRE)).coerceAtLeast(0f)

        // CÁLCULO DE ENERGÍA (Basado en acumulación/delta)
        val diffEnergia = tiempoActual - m.ultimaActualitzacioEnergia
        var energiaDelta = diffEnergia.toFloat() / TIEMPO_RECUPERACION_SUENO

        var nuevaEnergia = if (m.estaDormint) {
            m.energiaActual + energiaDelta // Suma si duerme
        } else {
            m.energiaActual - energiaDelta // Resta si está despierto
        }
        nuevaEnergia = nuevaEnergia.coerceIn(0f, 1f)

        // Actualizamos el modelo con los nuevos valores
        val mascotaActualizada = m.copy(
            energiaActual = nuevaEnergia,
            ultimaActualitzacioEnergia = tiempoActual
        )

        if (nuevoHambre <= 0f) {
            _mascota.value = mascotaActualizada.copy(estaViva = false)
            controlarMusica(false)
        } else {
            _mascota.value = mascotaActualizada
        }

        // Actualizamos las barras de la pantalla
        _nivelHambre.value = nuevoHambre
        _nivelSueno.value = nuevaEnergia
    }

    fun comprobarSiSigueViva() = _mascota.value?.estaViva == true

    fun resetearJuego() {
        _mascota.value = null
        controlarMusica(false)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}