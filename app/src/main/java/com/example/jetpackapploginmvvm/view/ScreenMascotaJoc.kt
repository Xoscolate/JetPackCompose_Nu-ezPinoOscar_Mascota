package com.example.jetpackapploginmvvm.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackapploginmvvm.R
import com.example.jetpackapploginmvvm.viewmodel.MascotaViewModel
import kotlinx.coroutines.delay

@Composable
fun ScreenMascotaJoc(
    viewModel: MascotaViewModel,
    onMascotaMorta: () -> Unit,
    onBackClick: () -> Unit
) {
    val mascota = viewModel.mascota
    var frameActual by remember { mutableIntStateOf(0) }

    // Animación a 10 FPS
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            frameActual = if (frameActual >= 3) 0 else frameActual + 1
        }
    }

    // Comprobación de muerte
    LaunchedEffect(Unit) {
        while (true) {
            if (!viewModel.comprobarSiSigueViva()) {
                onMascotaMorta()
            }
            delay(5000)
        }
    }

    // Usamos Box para el fondo completo
    Box(modifier = Modifier.fillMaxSize()) {

        // --- NUEVO: Usamos un Box contenedor para la imagen con margen superior ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp) // <-- Este margen baja la imagen y salva los cuernos
        ) {
            val imagenId = if (viewModel.estaComiendo) {
                R.drawable.demonio_comiendo
            } else {
                when (frameActual) {
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
                modifier = Modifier.fillMaxSize(), // Ocupa todo el espacio *del contenedor*
                contentScale = ContentScale.Fit // Ajusta sin deformar
            )
        }
        // ---------------------------------------------------------------------------

        // NOMBRE DE LA MASCOTA (ARRIBA)
        Text(
            text = mascota?.nom ?: "Demonio",
            fontSize = 40.sp,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        )

        // MENÚ DE ACCIONES (ABAJO)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.darDeComer() },
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth(0.8f),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text("DONAR DE MENJAR", fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onBackClick
            ) {
                Text("Sortir al menú", fontSize = 18.sp)
            }
        }
    }
}