package com.example.jetpackapploginmvvm.view.simon

import androidx.compose.foundation.Image
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

@Composable
fun ScreenMascotaDormir(viewModel: MascotaViewModel, onDespertar: () -> Unit) {
    val nivelSueno by viewModel.nivelSueno.collectAsState()
    val nivelHambre by viewModel.nivelHambre.collectAsState()
    var frameDormir by remember { mutableIntStateOf(0) }

    // Animación de dormir a 2 FPS en bucle
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            frameDormir = if (frameDormir >= 4) 0 else frameDormir + 1
        }
    }

    // Actualización de barras
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.actualizarEstado()
            delay(500)
        }
    }

    // Usamos Column principal para apilar la parte de arriba (con fondo) y la de abajo (oscura)
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. MITAD SUPERIOR (Fondo, Barras y Demonio) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f), // Ocupa un poco más de la mitad, igual que en el juego principal
            contentAlignment = Alignment.BottomCenter // El demonio se pegará abajo del todo
        ) {
            // Fondo de la habitación (solo ocupa esta mitad)
            Image(
                painter = painterResource(id = R.drawable.fondo_dormir),
                contentDescription = "Fondo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Recorta para llenar el espacio
            )

            // Barras de Estado (Pegadas arriba)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp, start = 32.dp, end = 32.dp)
            ) {
                Text("VITALITAT (Fám)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { nivelHambre },
                    modifier = Modifier.fillMaxWidth().height(12.dp),
                    color = Color(0xFFB22222), // Rojo
                    trackColor = Color.DarkGray.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("ENERGIA (Son)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { nivelSueno },
                    modifier = Modifier.fillMaxWidth().height(12.dp),
                    color = Color(0xFF4B0082), // Morado oscuro
                    trackColor = Color.DarkGray.copy(alpha = 0.7f)
                )
            }

            // Animación del demonio durmiendo (Pegado a la raya)
            val imagenDormir = when(frameDormir) {
                0 -> R.drawable.fotograma0000
                1 -> R.drawable.fotograma0001
                2 -> R.drawable.fotograma0002
                3 -> R.drawable.fotograma0003
                4 -> R.drawable.fotograma0004
                else -> R.drawable.fotograma0000
            }

            Image(
                painter = painterResource(id = imagenDormir),
                contentDescription = "Zzz",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp), // Ajustado al mismo tamaño que el demonio despierto
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter // Garantiza que toque la línea
            )
        }

        // --- 2. LA LÍNEA DIVISORIA ("SUELO") ---
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 6.dp,
            color = Color(0xFF4B0082) // La he puesto morada por la temática del sueño, cámbiala a 0xFF8B0000 si la quieres roja
        )

        // --- 3. MITAD INFERIOR (Fondo liso y botón) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF121212)) // Fondo negro/gris oscuro
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Button(
                onClick = {
                    viewModel.toggleDormir()
                    onDespertar()
                },
                shape = CutCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082)),
                modifier = Modifier
                    .height(80.dp) // Botón grande, como el de comer
                    .fillMaxWidth(0.85f),
                elevation = ButtonDefaults.buttonElevation(12.dp)
            ) {
                Text(
                    "DESPERTAR",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}