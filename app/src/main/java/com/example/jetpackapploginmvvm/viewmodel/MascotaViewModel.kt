package com.example.jetpackapploginmvvm.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackapploginmvvm.R
import com.example.jetpackapploginmvvm.model.Mascota
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.jetpackapploginmvvm.dao.AppDatabase

class MascotaViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()
    // --- VARIABLES DE ESTADO ---
    private var currentUser: String = ""
    private val TIEMPO_HAMBRE = 5 * 60 * 1000L
    private val TIEMPO_RECUPERACION_SUENO = 10 * 60 * 1000L
    private val TIEMPO_ESPECTRO = 5 * 1000L
    private var mediaPlayer: MediaPlayer? = null

    // Configuración de SoundPool para efectos cortos
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Cargamos el ID del sonido de comer
    private val soundIdComer: Int = soundPool.load(application, R.raw.sonido_comer, 1)

    private val _mascota = MutableStateFlow<Mascota?>(null)
    val mascota = _mascota.asStateFlow()

    private val _estaComiendo = MutableStateFlow(false)
    val estaComiendo = _estaComiendo.asStateFlow()

    private val _nivelHambre = MutableStateFlow(1f)
    val nivelHambre = _nivelHambre.asStateFlow()

    private val _nivelSueno = MutableStateFlow(1f)
    val nivelSueno = _nivelSueno.asStateFlow()

    // Estado para la pausa del ciclo de vida
    private val _juegoPausado = MutableStateFlow(false)
    val juegoPausado = _juegoPausado.asStateFlow()

    private var tiempoInicioSimon = 0L

    fun resetearJuego() {
        _mascota.value = null
        _nivelHambre.value = 1f
        _nivelSueno.value = 1f
        controlarMusica(false)
    }

    fun cargarMascotaDeUsuario(username: String) {
        currentUser = username
        viewModelScope.launch {
            val mDB = appDao.getMascota(username)
            if (mDB != null) {
                _mascota.value = mDB
                actualizarCalculos()
            }
        }
    }
    fun crearMascota(nombre: String) {
        val t = System.currentTimeMillis()
        val nueva = Mascota(
            ownerUsername = currentUser,
            nom = nombre,
            fechaCreacion = t,
            hambreActual = 1f,
            energiaActual = 1f,
            ultimaActualizacion = t,
            estaViva = true
        )
        _mascota.value = nueva
        guardarPartida() // Guardamos la mascota virgen en la DB
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

    fun darDeComer() {
        val mOriginal = _mascota.value ?: return

        // 1. Si duerme, lo despertamos y paramos música
        if (mOriginal.estaDormint) {
            despertarYPararMusica()
        }

        // 2. ¡IMPORTANTE!: Volvemos a pedir el valor para tener la mascota despertada
        val m = _mascota.value ?: return

        // Ahora m.estaDormint ya es false, así que el 'if' no nos echará
        if (_nivelSueno.value <= 0f || m.estaDormint) return

        soundPool.play(soundIdComer, 1f, 1f, 0, 0, 1f)
        actualizarCalculos()

        _mascota.value = _mascota.value?.copy(hambreActual = 1f, estaDormint = false)
        _nivelHambre.value = 1f

        guardarPartida() // Asegúrate de llamar a tu función de guardado aquí

        viewModelScope.launch {
            _estaComiendo.value = true
            delay(2400)
            _estaComiendo.value = false
        }
    }

    // Esta función es la que limpia el MediaPlayer
    private fun despertarYPararMusica() {
        val m = _mascota.value ?: return
        _mascota.value = m.copy(estaDormint = false)
        controlarMusica(false)
    }

    fun pausarJuego(pausar: Boolean) {
        _juegoPausado.value = pausar
        // 🔥 Si pausamos (salir con flecha, minimizar, etc.), SILENCIO TOTAL
        if (pausar) {
            controlarMusica(false)
        }
        // ❌ He quitado el bloque 'else' que re-encendía la música solo
        // así la música solo suena cuando tú le das a DORMIR explícitamente.
    }

    fun toggleDormir() {
        actualizarCalculos()
        val m = _mascota.value ?: return
        val nuevoEstadoDormir = !m.estaDormint
        _mascota.value = m.copy(estaDormint = nuevoEstadoDormir)
        controlarMusica(nuevoEstadoDormir)
    }

    fun actualizarEstado() {
        // Solo actualizamos si el juego NO está pausado por el ciclo de vida
        if (!_juegoPausado.value) {
            actualizarCalculos()
        }
    }

    private fun actualizarCalculos() {
        val m = _mascota.value ?: return
        if (!m.estaViva) return

        val t = System.currentTimeMillis()
        val diffTiempo = t - m.ultimaActualizacion

        val esFelic = t < (m.tempsFiFelicitat ?: 0L)
        val factorDesgaste = if (esFelic) 0.5f else 1.0f

        var nHambre = (m.hambreActual - (diffTiempo.toFloat() / TIEMPO_HAMBRE * factorDesgaste)).coerceIn(0f, 1f)
        var nEnergia = if (m.estaDormint) {
            (m.energiaActual + (diffTiempo.toFloat() / TIEMPO_RECUPERACION_SUENO)).coerceIn(0f, 1f)
        } else {
            (m.energiaActual - (diffTiempo.toFloat() / TIEMPO_RECUPERACION_SUENO * factorDesgaste)).coerceIn(0f, 1f)
        }

        val nuevaListaEspectros = m.espectresActius.toMutableList()
        var nuevoTiempoEspectro = m.ultimoEspectro

        if (t - m.ultimoEspectro >= TIEMPO_ESPECTRO) {
            val nuevaPos = (0..9).random()
            if (!nuevaListaEspectros.contains(nuevaPos)) {
                nuevaListaEspectros.add(nuevaPos)
            }
            nuevoTiempoEspectro = t
        }

        _mascota.value = m.copy(
            hambreActual = nHambre,
            energiaActual = nEnergia,
            ultimaActualizacion = t,
            ultimoEspectro = nuevoTiempoEspectro,
            espectresActius = nuevaListaEspectros,
            estaViva = nHambre > 0f
        )
        _nivelHambre.value = nHambre
        _nivelSueno.value = nEnergia
    }

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
        val m = _mascota.value ?: return
        viewModelScope.launch {
            appDao.insertMascota(m.copy(ultimaActualizacion = System.currentTimeMillis()))
        }
    }

    private fun controlarMusica(reproducir: Boolean) {
        if (reproducir) {
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(getApplication(), R.raw.musica_dormir).apply {
                        isLooping = true; start()
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            // 🔥 Limpieza profunda para asegurar que se detiene
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) it.stop()
                    it.release()
                }
            } catch (e: Exception) { e.printStackTrace() }
            mediaPlayer = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        soundPool.release()
    }

    fun sacudirLimpiarEspectros() {
        val m = _mascota.value ?: return
        if (m.espectresActius.isNotEmpty()) {
            _mascota.value = m.copy(espectresActius = emptyList())
        }
    }
}