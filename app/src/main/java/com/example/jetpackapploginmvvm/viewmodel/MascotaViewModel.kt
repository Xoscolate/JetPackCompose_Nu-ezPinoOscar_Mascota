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

    private val TIEMPO_HAMBRE = 60 * 1000L // 1 minuto (Ajustar más adelante si es muy rápido)
    private val TIEMPO_RECUPERACION_SUENO = 10 * 60 * 1000L // 10 minutos

    // --- CAMBIO AQUÍ: Ahora aparece 1 espectro cada 10 segundos ---
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

    fun crearMascota(nombre: String) {
        val t = System.currentTimeMillis()
        _mascota.value = Mascota(
            nom = nombre,
            hambreActual = 1f,
            energiaActual = 1f,
            ultimaActualizacion = t,
            ultimoEspectro = t,
            espectresActius = 0,
            nivellFelicitat = 100,
            estaDormint = false,
            estaViva = true
        )
    }

    fun darDeComer() {
        val m = _mascota.value ?: return
        if (_nivelSueno.value <= 0f || m.estaDormint) return

        actualizarCalculos()
        _mascota.value = _mascota.value?.copy(hambreActual = 1f)

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

    fun eliminarEspectro() {
        val m = _mascota.value ?: return
        if (m.espectresActius > 0) {
            _mascota.value = m.copy(espectresActius = m.espectresActius - 1)
        }
    }

    fun actualizarEstado() {
        actualizarCalculos()
    }

    private fun actualizarCalculos() {
        val m = _mascota.value ?: return
        if (!m.estaViva) return

        val t = System.currentTimeMillis()
        val diffTiempo = t - m.ultimaActualizacion

        val penalizacion = if (m.espectresActius > 3) 2f else 1f

        // --- CALCULO HAMBRE Y SUEÑO ---
        var nHambre = m.hambreActual - ((diffTiempo.toFloat() / TIEMPO_HAMBRE) * penalizacion)

        var nEnergia = m.energiaActual
        if (m.estaDormint) {
            nEnergia += (diffTiempo.toFloat() / TIEMPO_RECUPERACION_SUENO)
        } else {
            nEnergia -= ((diffTiempo.toFloat() / TIEMPO_RECUPERACION_SUENO) * penalizacion)
        }

        nHambre = nHambre.coerceIn(0f, 1f)
        nEnergia = nEnergia.coerceIn(0f, 1f)

        // --- CALCULO APARICIÓN DE ESPECTROS (Funciona en 2º plano) ---
        val diffEspectro = t - m.ultimoEspectro
        val nuevosEspectros = (diffEspectro / TIEMPO_ESPECTRO).toInt()

        var totalEspectros = m.espectresActius
        var nuevoRelojEspectros = m.ultimoEspectro

        if (nuevosEspectros > 0 && totalEspectros < 10) {
            totalEspectros += nuevosEspectros
            if (totalEspectros > 10) totalEspectros = 10 // LÍMITE MÁXIMO DE 10 ESPECTROS
            nuevoRelojEspectros += nuevosEspectros * TIEMPO_ESPECTRO
        }

        // GUARDAR TODO
        val mascotaActualizada = m.copy(
            hambreActual = nHambre,
            energiaActual = nEnergia,
            ultimaActualizacion = t,
            ultimoEspectro = nuevoRelojEspectros,
            espectresActius = totalEspectros
        )

        // Comprobación de muerte
        if (nHambre <= 0f) {
            _mascota.value = mascotaActualizada.copy(estaViva = false)
            controlarMusica(false)
        } else {
            _mascota.value = mascotaActualizada
        }

        // Actualizar visuales
        _nivelHambre.value = nHambre
        _nivelSueno.value = nEnergia
    }

    fun comprobarSiSigueViva() = _mascota.value?.estaViva == true

    fun resetearJuego() {
        _mascota.value = null
        controlarMusica(false)
    }

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
        mediaPlayer = null
    }

    fun cambiarFondo(nuevoFondo: Int) {
        val m = _mascota.value ?: return
        _mascota.value = m.copy(fondoActual = nuevoFondo)
    }
}