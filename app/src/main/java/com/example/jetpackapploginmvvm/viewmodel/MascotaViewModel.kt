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

    private val TIEMPO_HAMBRE = 10 * 60 * 1000L
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

    fun crearMascota(nombre: String) {
        val t = System.currentTimeMillis()
        _mascota.value = Mascota(
            nom = nombre,
            hambreActual = 1f,
            energiaActual = 1f,
            ultimaActualizacion = t,
            ultimoEspectro = t,
            espectresActius = emptyList(), // Inicializado como lista vacía
            tempsFiFelicitat = 0L,
            estaDormint = false,
            estaViva = true,
            fondoActual = 0
        )
    }

    fun jugarSimon() {
        val m = _mascota.value ?: return
        _mascota.value = m.copy(tempsFiFelicitat = System.currentTimeMillis() + (5 * 60 * 1000L))
    }

    fun cambiarFondo(nuevoFondo: Int) {
        val m = _mascota.value ?: return
        _mascota.value = m.copy(fondoActual = nuevoFondo)
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

    // --- CAMBIO CLAVE: Elimina exactamente la posición que has tocado ---
    fun eliminarEspectro(posicionId: Int) {
        val m = _mascota.value ?: return
        val nuevaLista = m.espectresActius.filter { it != posicionId }
        _mascota.value = m.copy(espectresActius = nuevaLista)
    }

    fun actualizarEstado() {
        actualizarCalculos()
    }

    private fun actualizarCalculos() {
        val m = _mascota.value ?: return
        if (!m.estaViva) return

        val t = System.currentTimeMillis()
        val diffTiempo = t - m.ultimaActualizacion

        val esTriste = m.espectresActius.size > 3
        val esFeliz = t < m.tempsFiFelicitat

        val multiplicador = when {
            esTriste -> 2f
            esFeliz -> 0.5f
            else -> 1f
        }

        var nHambre = m.hambreActual - ((diffTiempo.toFloat() / TIEMPO_HAMBRE) * multiplicador)

        var nEnergia = m.energiaActual
        if (m.estaDormint) {
            nEnergia += (diffTiempo.toFloat() / TIEMPO_RECUPERACION_SUENO)
        } else {
            nEnergia -= ((diffTiempo.toFloat() / TIEMPO_RECUPERACION_SUENO) * multiplicador)
        }

        nHambre = nHambre.coerceIn(0f, 1f)
        nEnergia = nEnergia.coerceIn(0f, 1f)

        // --- CÁLCULO DE ESPECTROS ACTUALIZADO ---
        val diffEspectro = t - m.ultimoEspectro
        val nuevosEspectros = (diffEspectro / TIEMPO_ESPECTRO).toInt()
        var nuevaListaEspectros = m.espectresActius.toMutableList()
        var nuevoRelojEspectros = m.ultimoEspectro

        if (nuevosEspectros > 0 && nuevaListaEspectros.size < 10) {
            // Buscamos qué posiciones de la 0 a la 9 están libres
            val posicionesLibres = (0..9).filter { !nuevaListaEspectros.contains(it) }.toMutableList()
            posicionesLibres.shuffle() // Mezclamos para que salgan en orden aleatorio

            // Añadimos tantos como toque, sin pasarnos del límite de huecos libres
            val aAnadir = minOf(nuevosEspectros, posicionesLibres.size)
            for (i in 0 until aAnadir) {
                nuevaListaEspectros.add(posicionesLibres[i])
            }
            nuevoRelojEspectros += nuevosEspectros * TIEMPO_ESPECTRO
        }

        val mascotaActualizada = m.copy(
            hambreActual = nHambre,
            energiaActual = nEnergia,
            ultimaActualizacion = t,
            ultimoEspectro = nuevoRelojEspectros,
            espectresActius = nuevaListaEspectros
        )

        if (nHambre <= 0f) {
            _mascota.value = mascotaActualizada.copy(estaViva = false)
            controlarMusica(false)
        } else {
            _mascota.value = mascotaActualizada
        }

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
}