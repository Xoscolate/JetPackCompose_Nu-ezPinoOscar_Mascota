package com.example.jetpackapploginmvvm.view

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackapploginmvvm.R
import com.example.jetpackapploginmvvm.viewmodel.MascotaViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image

@Composable
fun ScreenMascotaJoc(
    viewModel: MascotaViewModel,
    onMascotaMorta: () -> Unit,
    onDormirClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val mascota by viewModel.mascota.collectAsState()
    val estaComiendo by viewModel.estaComiendo.collectAsState()

    // RECOLECTAMOS LAS BARRAS
    val nivelHambre by viewModel.nivelHambre.collectAsState()
    val nivelSueno by viewModel.nivelSueno.collectAsState()

    var frameBase by remember { mutableIntStateOf(0) }
    var frameComiendo by remember { mutableIntStateOf(0) }

    // Animaciones
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

    // BUCLE PRINCIPAL DEL JUEGO: Actualiza las barras cada medio segundo
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.actualizarEstado()
            if (mascota?.estaViva == false) {
                onMascotaMorta()
            }
            delay(500) // Se actualiza rápido para que las barras se muevan fluido
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // 1. NOMBRE DE LA MASCOTA
        Text(
            text = mascota?.nom?.uppercase() ?: "DEMONIO",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFB22222),
            letterSpacing = 4.sp,
        )

        // --- NUEVO: BARRAS DE ESTADO ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            // Barra de Hambre
            Text("VITALITAT (Fám)", color = Color.White, fontSize = 12.sp)
            LinearProgressIndicator(
                progress = { nivelHambre },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = Color(0xFFB22222), // Rojo
                trackColor = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de Sueño
            Text("ENERGIA (Son)", color = Color.White, fontSize = 12.sp)
            LinearProgressIndicator(
                progress = { nivelSueno },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = Color(0xFF4B0082), // Morado oscuro (Energía oscura)
                trackColor = Color.DarkGray
            )
        }
        // -------------------------------

        // 2. EL DEMONIO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomCenter
                )
            }
        }

        // 3. LA BARRA BASE ROJA
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 6.dp,
            color = Color(0xFF8B0000)
        )

        // 4. MENÚ DE ACCIONES (2 Botones principales)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Botón Comer
            Button(
                onClick = { viewModel.darDeComer() },
                shape = CutCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000)),
                modifier = Modifier
                    .height(65.dp)
                    .fillMaxWidth(0.85f),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text("SACRIFICAR ALIMENT", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Dormir
            Button(
                onClick = {
                    viewModel.toggleDormir()
                    onDormirClick()
                },
                shape = CutCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082)), // Morado
                modifier = Modifier
                    .height(65.dp)
                    .fillMaxWidth(0.85f),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text("DESCANSAR EN LES OMBRES", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f)) // Empuja el botón de salir hacia abajo

            TextButton(
                onClick = onBackClick,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("ABANDONAR EL REGNE", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}