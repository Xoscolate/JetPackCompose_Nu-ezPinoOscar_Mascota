package com.example.jetpackapploginmvvm.view

import androidx.compose.animation.Crossfade
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

@Composable
fun ScreenMascotaJoc(
    viewModel: MascotaViewModel,
    onMascotaMorta: () -> Unit,
    onDormirClick: () -> Unit,
    onPersonalizarClick: () -> Unit,
    onSimonClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val mascota by viewModel.mascota.collectAsState()
    val estaComiendo by viewModel.estaComiendo.collectAsState()

    val nivelHambre by viewModel.nivelHambre.collectAsState()
    val nivelSueno by viewModel.nivelSueno.collectAsState()

    var frameBase by remember { mutableIntStateOf(0) }
    var frameComiendo by remember { mutableIntStateOf(0) }

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
                delay(125)
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.actualizarEstado()
            if (mascota?.estaViva == false) {
                onMascotaMorta()
            }
            delay(500)
        }
    }

    val fondoElegidoId = when(mascota?.fondoActual) {
        0 -> R.drawable.fondo_0
        1 -> R.drawable.fondo_1
        2 -> R.drawable.fondo_02
        3 -> R.drawable.fondo_03
        4 -> R.drawable.fondo_04
        5 -> R.drawable.fondo_05
        else -> R.drawable.fondo_0
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // --- 1. MITAD SUPERIOR (Fondo, Demonio, Espectros y Textos) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f),
            contentAlignment = Alignment.BottomCenter
        ) {
            // A. IMAGEN DE FONDO
            Image(
                painter = painterResource(id = fondoElegidoId),
                contentDescription = "Fondo Seleccionado",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // B. UI SUPERIOR (Nombres y Barras subidas)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // NOMBRE DE LA MASCOTA
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

                Spacer(modifier = Modifier.height(6.dp))

                // --- INDICADOR DE ESTADO (Adaptado a List<Int>) ---
                val listaEspectros = mascota?.espectresActius ?: emptyList()
                val numEspectros = listaEspectros.size
                val esFelic = System.currentTimeMillis() < (mascota?.tempsFiFelicitat ?: 0L)

                if (numEspectros > 3) {
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
                } else if (esFelic) {
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
                    Row(
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

                // BARRAS DE ESTADO
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("VITALITAT (Fám)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { nivelHambre },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = Color(0xFFB22222), trackColor = Color.DarkGray.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("ENERGIA (Son)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { nivelSueno },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = Color(0xFF4B0082), trackColor = Color.DarkGray.copy(alpha = 0.8f)
                    )
                }
            }

            // C. EL DEMONIO
            Crossfade(targetState = estaComiendo, label = "crossfade_demon") { comiendo ->
                val imagenId = if (comiendo) {
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
            }

            // D. ESPECTROS (Dibujado usando los IDs concretos)
            val listaEspectrosParaDibujar = mascota?.espectresActius ?: emptyList()
            val posiciones = listOf(
                BiasAlignment(-0.7f, -0.2f), BiasAlignment(0.6f, -0.1f),
                BiasAlignment(-0.4f, 0.3f), BiasAlignment(0.5f, 0.4f),
                BiasAlignment(-0.8f, 0.1f), BiasAlignment(0.8f, 0.2f),
                BiasAlignment(0.1f, -0.3f), BiasAlignment(-0.2f, 0.5f),
                BiasAlignment(0.7f, 0.5f), BiasAlignment(-0.6f, 0.4f)
            )

            // Recorremos la lista de activos y ponemos la imagen en su posición
            listaEspectrosParaDibujar.forEach { posicionId ->
                val pos = posiciones[posicionId]
                Image(
                    painter = painterResource(id = R.drawable.espectro),
                    contentDescription = "Espectro molesto",
                    modifier = Modifier
                        .align(pos)
                        .size(50.dp)
                        .clickable { viewModel.eliminarEspectro(posicionId) } // <-- Pasa la ID exacta
                )
            }
        }

        // --- 2. LA BARRA BASE ROJA ---
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 6.dp,
            color = Color(0xFF8B0000)
        )

        // --- 3. MENÚ DE ACCIONES INFERIOR (GRILLA 2x2) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .background(Color(0xFF121212))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            // PRIMERA FILA DE BOTONES
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

            // SEGUNDA FILA DE BOTONES
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

                // --- BOTÓN DEL SIMÓN ---
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

            // BOTÓN DE SALIR
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text("ABANDONAR EL REGNE", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}