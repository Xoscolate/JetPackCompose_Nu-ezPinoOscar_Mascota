package com.example.jetpackapploginmvvm.view.simon

import androidx.activity.compose.BackHandler
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
    BackHandler {
        if (viewModel.mascota.value?.estaDormint == true) {
            viewModel.toggleDormir()
        }
        onDespertar()
    }

    val nivelSueno by viewModel.nivelSueno.collectAsState()
    val nivelHambre by viewModel.nivelHambre.collectAsState()
    var frameDormir by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            frameDormir = if (frameDormir >= 4) 0 else frameDormir + 1
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.actualizarEstado()
            delay(500)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.fondo_dormir),
                contentDescription = "Fondo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )


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
                    .height(350.dp),
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 6.dp,
            color = Color(0xFF4B0082)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF121212))
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