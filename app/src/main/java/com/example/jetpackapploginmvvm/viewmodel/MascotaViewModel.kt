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

    // --- CAMBIADO: El hambre ahora tarda 10 minutos ---
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

    fun crearMascota(nombre: String) {
        val t = System.currentTimeMillis()
        _mascota.value = Mascota(
            nom = nombre,
            hambreActual = 1f,
            energiaActual = 1f,
            ultimaActualizacion = t,
            ultimoEspectro = t,
            espectresActius = 0,
            tempsFiFelicitat = 0L,
            estaDormint = false,
            estaViva = true,
            fondoActual = 0
        )
    }

    // --- NUEVA FUNCIÓN: Se llamará al jugar al Simón ---
    private var tiempoInicioSimon = 0L

    fun entrarAlSimon() {
        // Guardamos la hora exacta a la que entra a jugar
        tiempoInicioSimon = System.currentTimeMillis()
    }

    fun salirDelSimon() {
        val m = _mascota.value ?: return

        // Calculamos cuánto tiempo ha pasado desde que entró
        val tiempoJugado = System.currentTimeMillis() - tiempoInicioSimon

        // Comprobamos si han pasado al menos 2 minutos (2 * 60 * 1000 milisegundos)
        // Para hacer pruebas rápidas ahora mismo, puedes cambiar el "2" por "0" o por menos tiempo.
        if (tiempoJugado >= 2 * 60 * 1000L) {
            // Solo si ha jugado más de 2 minutos, le damos los 5 minutos de felicidad
            _mascota.value = m.copy(tempsFiFelicitat = System.currentTimeMillis() + (5 * 60 * 1000L))
        }
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

        // --- LÓGICA DE MULTIPLICADORES ---
        val esTriste = m.espectresActius > 3
        val esFeliz = t < m.tempsFiFelicitat // Es feliz si aún no se ha acabado el tiempo

        val multiplicador = when {
            esTriste -> 2f    // La suciedad gana: va el doble de rápido
            esFeliz -> 0.5f   // Si está feliz, gasta a la MITAD de velocidad
            else -> 1f        // Neutral
        }

        // --- CÁLCULO HAMBRE Y SUEÑO ---
        var nHambre = m.hambreActual - ((diffTiempo.toFloat() / TIEMPO_HAMBRE) * multiplicador)

        var nEnergia = m.energiaActual
        if (m.estaDormint) {
            nEnergia += (diffTiempo.toFloat() / TIEMPO_RECUPERACION_SUENO)
        } else {
            nEnergia -= ((diffTiempo.toFloat() / TIEMPO_RECUPERACION_SUENO) * multiplicador)
        }

        nHambre = nHambre.coerceIn(0f, 1f)
        nEnergia = nEnergia.coerceIn(0f, 1f)

        // --- CÁLCULO ESPECTROS ---
        val diffEspectro = t - m.ultimoEspectro
        val nuevosEspectros = (diffEspectro / TIEMPO_ESPECTRO).toInt()

        var totalEspectros = m.espectresActius
        var nuevoRelojEspectros = m.ultimoEspectro

        if (nuevosEspectros > 0 && totalEspectros < 10) {
            totalEspectros += nuevosEspectros
            if (totalEspectros > 10) totalEspectros = 10
            nuevoRelojEspectros += nuevosEspectros * TIEMPO_ESPECTRO
        }

        val mascotaActualizada = m.copy(
            hambreActual = nHambre,
            energiaActual = nEnergia,
            ultimaActualizacion = t,
            ultimoEspectro = nuevoRelojEspectros,
            espectresActius = totalEspectros
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