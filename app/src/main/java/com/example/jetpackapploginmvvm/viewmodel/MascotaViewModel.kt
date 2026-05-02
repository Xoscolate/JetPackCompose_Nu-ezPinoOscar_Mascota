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
            espectresActius = emptyList(),
            tempsFiFelicitat = 0L,
            estaDormint = false,
            estaViva = true,
            fondoActual = 0,
            notificacioFamEnviada = false // Aseguramos que empiece en false al crear
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
        // Reseteamos el hambre y volvemos a poner la notificación en false
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

        val diffEspectro = t - m.ultimoEspectro
        val nuevosEspectros = (diffEspectro / TIEMPO_ESPECTRO).toInt()
        var nuevaListaEspectros = m.espectresActius.toMutableList()
        var nuevoRelojEspectros = m.ultimoEspectro

        if (nuevosEspectros > 0 && nuevaListaEspectros.size < 10) {
            val posicionesLibres = (0..9).filter { !nuevaListaEspectros.contains(it) }.toMutableList()
            posicionesLibres.shuffle()

            val aAnadir = minOf(nuevosEspectros, posicionesLibres.size)
            for (i in 0 until aAnadir) {
                nuevaListaEspectros.add(posicionesLibres[i])
            }
            nuevoRelojEspectros += nuevosEspectros * TIEMPO_ESPECTRO
        }

        var mascotaActualizada = m.copy(
            hambreActual = nHambre,
            energiaActual = nEnergia,
            ultimaActualizacion = t,
            ultimoEspectro = nuevoRelojEspectros,
            espectresActius = nuevaListaEspectros
        )

        // --- COMPROBACIÓN DE MUERTE Y NOTIFICACIONES ---
        if (nHambre <= 0f) {
            if (!m.notificacioFamEnviada) {
                enviarNotificacionFam()
                mascotaActualizada = mascotaActualizada.copy(notificacioFamEnviada = true)
            }
            _mascota.value = mascotaActualizada.copy(estaViva = false)
            controlarMusica(false)
        } else {
            _mascota.value = mascotaActualizada
        }

        _nivelHambre.value = nHambre
        _nivelSueno.value = nEnergia
    }

    private fun enviarNotificacionFam() {
        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "canal_dimoni_fam"

        // 1. Android 8.0 o superior exige crear un "Canal" de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Avisos de Fam",
                NotificationManager.IMPORTANCE_HIGH // Aquí sí es IMPORTANCE
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Construir la notificación visual
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.demonio) // El icono que saldrá en la barra superior
            .setContentTitle("EL TEU DIMONI ES MOR!")
            .setContentText("Tinc gana! El meu nivell de vitalitat ha arribat a 0.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // <--- ¡AQUÍ ESTABA EL ERROR! Es PRIORITY_HIGH
            .setAutoCancel(true)

        // 3. Lanzarla al móvil
        notificationManager.notify(1, builder.build())
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