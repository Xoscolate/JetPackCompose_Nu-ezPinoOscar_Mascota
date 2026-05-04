package com.example.jetpackapploginmvvm.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackapploginmvvm.R
import com.example.jetpackapploginmvvm.viewmodel.MascotaViewModel
import kotlinx.coroutines.delay
import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler // 🔥 AÑADIDO PARA LA FLECHA DEL MÓVIL
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import kotlin.math.sqrt

@Composable
fun ScreenMascotaJoc(
    viewModel: MascotaViewModel,
    username: String?,
    onMascotaMorta: () -> Unit,
    onDormirClick: () -> Unit,
    onPersonalizarClick: () -> Unit,
    onSimonClick: () -> Unit,
    onBackClick: () -> Unit
) {
    // 🔥 CONTROL DE LA FLECHA FÍSICA (Si el usuario le da atrás en el móvil)
    BackHandler {
        viewModel.pausarJuego(true) // Paramos música y pausamos antes de salir
        onBackClick() // Ejecutamos la salida
    }

    // Gestion permisos
    val launcherPermiso = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) {  }

    // 2. carga inicial
    LaunchedEffect(username) {
        // Esto lo utilizo para pedirle permisos al usuario
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            launcherPermiso.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // cargar datos
        username?.let {
            viewModel.cargarMascotaDeUsuario(it)
            // 🔥 SIEMPRE que entramos, forzamos parada de música y quitamos pausa
            viewModel.pausarJuego(true)
            viewModel.pausarJuego(false)
        }
    }

    // Cronòmetre de la partida (temps en segons)
    val mascota by viewModel.mascota.collectAsState()
    var segonsPartida by remember { mutableIntStateOf(0) }

    // 2. Cronómetro Inmortal (Timestamp)
    LaunchedEffect(mascota?.fechaCreacion, mascota?.estaViva) {
        while (mascota?.estaViva == true) {
            val ahora = System.currentTimeMillis()
            val inicio = mascota?.fechaCreacion ?: ahora
            segonsPartida = ((ahora - inicio) / 1000).toInt()
            delay(1000)
        }
    }

    // 3. Actualización de constantes (Hambre/Sueño) y Muerte (UNIFICADO)
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.actualizarEstado() // Esto solo resta vida si NO está pausado
            if (mascota?.estaViva == false && mascota != null) {
                onMascotaMorta()
            }
            delay(1000)
        }
    }

    //El guardado si cierras la app
    DisposableEffect(Unit) {
        onDispose {
            viewModel.guardarPartida()
        }
    }

    val estaComiendo by viewModel.estaComiendo.collectAsState()

    val nivelHambre by viewModel.nivelHambre.collectAsState()
    val nivelSueno by viewModel.nivelSueno.collectAsState()
    val context = LocalContext.current
    val juegoPausado by viewModel.juegoPausado.collectAsState()

    // ANIMACIONS SUAUS PER A LES BARRES (animate*AsState)
    val animatedHambre by animateFloatAsState(
        targetValue = nivelHambre,
        animationSpec = tween(durationMillis = 1000), label = "anim_hambre"
    )
    val animatedSueno by animateFloatAsState(
        targetValue = nivelSueno,
        animationSpec = tween(durationMillis = 1000), label = "anim_sueno"
    )

    // Canvi de color suau si el bicho té gana o son
    val colorHambre by animateColorAsState(
        targetValue = if (nivelHambre < 0.25f) Color.Red else Color(0xFFB22222),
        animationSpec = tween(durationMillis = 500), label = "color_hambre"
    )

    var frameBase by remember { mutableIntStateOf(0) }
    var frameComiendo by remember { mutableIntStateOf(0) }

    // --- LOGICA DEL SHAKE (ACELEROMETRO) ---
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastShakeTime: Long = 0

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Calculamos la fuerza del movimiento
                val gForce = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
                if (gForce > 12) { // Umbral de sacudida fuerte
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastShakeTime > 1000) { // Evita que se dispare 100 veces por segundo
                        viewModel.sacudirLimpiarEspectros()
                        lastShakeTime = currentTime
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    DisposableEffect(context) {
        val activity = context as? ComponentActivity
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.pausarJuego(true) // Apaga música si minimizas la App
            }
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.pausarJuego(false) // Quita el cartel al volver
            }
        }
        activity?.lifecycle?.addObserver(observer)
        onDispose {
            activity?.lifecycle?.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(125)
            frameBase = if (frameBase >= 3) 0 else frameBase + 1
        }
    }

    LaunchedEffect(estaComiendo) {
        if (estaComiendo) {
            frameComiendo = 0
            for (i in 0..18) {
                frameComiendo = i
                delay(125) //Este delay corresponde a 8 frames en animacion
            }
        }
    }

    val fondoElegidoId = when(mascota?.fondoActual) { //Esto es el id de los fondos para que lo identique dentro de su modelo y asi lo muestre
        0 -> R.drawable.fondo_0
        1 -> R.drawable.fondo_1
        2 -> R.drawable.fondo_02
        3 -> R.drawable.fondo_03
        4 -> R.drawable.fondo_04
        5 -> R.drawable.fondo_05
        else -> R.drawable.fondo_0
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f),
                contentAlignment = Alignment.BottomCenter
            ) {
                //Este es el fondo
                Image(
                    painter = painterResource(id = fondoElegidoId),
                    contentDescription = "Fondo Seleccionado",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = mascota?.nom?.uppercase() ?: "DEMONIO",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB22222),
                        letterSpacing = 4.sp,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // CRONÒMETRE DE PARTIDA
                    Text(
                        text = "TEMPS SOBREVISCUT: ${segonsPartida}s",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    //Esto es la mecnica de la felicidad de la mascota
                    val listaEspectros = mascota?.espectresActius ?: emptyList() //La lista vacia de espectros que se ira llenando
                    val numEspectros = listaEspectros.size //esto es para mirar cuantos espectros hay porque luego compararemos si hay mas de 3
                    val esFelic = System.currentTimeMillis() < (mascota?.tempsFiFelicitat ?: 0L)

                    if (numEspectros > 3) { //Si hay mas de 3 espectros en pantalla el demonio esta triste
                        Row(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("😢", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TRIST: Fam i Son x2!", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (esFelic) { //Si esta feliz (esto es cuando ha jugado al simon dice 2 min o mas)
                        Row(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🤩", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CONTENT: Desgast x0.5", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row( //estado normal
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("😐", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Neutral", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("VITALITAT (HAMBRE)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { animatedHambre },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = colorHambre, trackColor = Color.DarkGray.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("ENERGIA (SUEÑO)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { animatedSueno },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = Color(0xFF4B0082), trackColor = Color.DarkGray.copy(alpha = 0.8f)
                        )
                    }
                }

                // Imagen del demonio
                val imagenId = if (estaComiendo) {
                    when (frameComiendo) {
                        0 -> R.drawable.fotograma_demonio_comiendo0000
                        1 -> R.drawable.fotograma_demonio_comiendo0001
                        2 -> R.drawable.fotograma_demonio_comiendo0002
                        3 -> R.drawable.fotograma_demonio_comiendo0003
                        4 -> R.drawable.fotograma_demonio_comiendo0004
                        5 -> R.drawable.fotograma_demonio_comiendo0005
                        6 -> R.drawable.fotograma_demonio_comiendo0006
                        7 -> R.drawable.fotograma_demonio_comiendo0007
                        8 -> R.drawable.fotograma_demonio_comiendo0008
                        9 -> R.drawable.fotograma_demonio_comiendo0009
                        10 -> R.drawable.fotograma_demonio_comiendo0010
                        11 -> R.drawable.fotograma_demonio_comiendo0011
                        12 -> R.drawable.fotograma_demonio_comiendo0012
                        13 -> R.drawable.fotograma_demonio_comiendo0013
                        14 -> R.drawable.fotograma_demonio_comiendo0014
                        15 -> R.drawable.fotograma_demonio_comiendo0015
                        16 -> R.drawable.fotograma_demonio_comiendo0016
                        17 -> R.drawable.fotograma_demonio_comiendo0017
                        18 -> R.drawable.fotograma_demonio_comiendo0018
                        else -> R.drawable.fotograma_demonio_comiendo0018
                    }
                } else {
                    when (frameBase) {
                        0 -> R.drawable.fotograma_demonio_base0000
                        1 -> R.drawable.fotograma_demonio_base0001
                        2 -> R.drawable.fotograma_demonio_base0002
                        3 -> R.drawable.fotograma_demonio_base0003
                        else -> R.drawable.fotograma_demonio_base0000
                    }
                }

                Image(
                    painter = painterResource(id = imagenId),
                    contentDescription = "Mascota",
                    modifier = Modifier.fillMaxWidth().height(380.dp),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomCenter
                )

                // espectros
                val listaEspectrosParaDibujar = mascota?.espectresActius ?: emptyList()
                val posiciones = listOf(
                    BiasAlignment(-0.7f, -0.2f), BiasAlignment(0.6f, -0.1f),
                    BiasAlignment(-0.4f, 0.3f), BiasAlignment(0.5f, 0.4f),
                    BiasAlignment(-0.8f, 0.1f), BiasAlignment(0.8f, 0.2f),
                    BiasAlignment(0.1f, -0.3f), BiasAlignment(-0.2f, 0.5f),
                    BiasAlignment(0.7f, 0.5f), BiasAlignment(-0.6f, 0.4f)
                )

                listaEspectrosParaDibujar.forEach { posicionId ->
                    val pos = posiciones[posicionId]
                    Image(
                        painter = painterResource(id = R.drawable.espectro),
                        contentDescription = "Espectro molesto",
                        modifier = Modifier
                            .align(pos)
                            .size(50.dp)
                            .clickable { viewModel.eliminarEspectro(posicionId) }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 6.dp,
                color = Color(0xFF8B0000)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .background(Color(0xFF121212))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.darDeComer() },
                        shape = CutCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000)),
                        modifier = Modifier.weight(1f).height(60.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("ALIMENTAR", fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }

                    Button(
                        onClick = {
                            viewModel.toggleDormir()
                            onDormirClick()
                        },
                        shape = CutCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082)),
                        modifier = Modifier.weight(1f).height(60.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("DESCANSAR", fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onPersonalizarClick,
                        shape = CutCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E8B57)),
                        modifier = Modifier.weight(1f).height(60.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("FONDS", fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }

                    Button(
                        onClick = onSimonClick,
                        shape = CutCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD2691E)),
                        modifier = Modifier.weight(1f).height(60.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("SIMÓN INFERNAL", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = {
                        // 🔥 Forzamos apagado de música antes de volver al menú
                        viewModel.pausarJuego(true)
                        onBackClick()
                    },
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text("ABANDONAR EL REGNE", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        // Overlay de pausa
        if (juegoPausado) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = true, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "JOC EN PAUSA",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.pausarJuego(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB22222)),
                        shape = CutCornerShape(10.dp)
                    ) {
                        Text("RESUME JOC", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}