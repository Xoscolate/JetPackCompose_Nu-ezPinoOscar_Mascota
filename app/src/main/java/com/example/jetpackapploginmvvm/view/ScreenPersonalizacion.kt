package com.example.jetpackapploginmvvm.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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

@Composable
fun ScreenPersonalizacion(
    viewModel: MascotaViewModel,
    onBackClick: () -> Unit
) {
    val mascota by viewModel.mascota.collectAsState()
    val fondoSeleccionado = mascota?.fondoActual ?: 0

    // Lista con las referencias a tus 6 fondos
    val fondos = listOf(
        R.drawable.fondo_0,
        R.drawable.fondo_1,
        R.drawable.fondo_02,
        R.drawable.fondo_03,
        R.drawable.fondo_04,
        R.drawable.fondo_05

    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            "PERSONALITZACIÓ",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFB22222),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Tria el fons del teu regne:", color = Color.Gray, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(24.dp))

        // Cuadrícula de fondos
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 columnas
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(fondos.size) { index ->
                val isSelected = fondoSeleccionado == index

                Box(
                    modifier = Modifier
                        .aspectRatio(0.8f) // Forma un poco rectangular
                        .clickable { viewModel.cambiarFondo(index) }
                        .border(
                            width = if (isSelected) 4.dp else 1.dp,
                            color = if (isSelected) Color.Green else Color.DarkGray,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Image(
                        painter = painterResource(id = fondos[index]),
                        contentDescription = "Fondo $index",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("ACTIU", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onBackClick,
            shape = CutCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            modifier = Modifier.fillMaxWidth(0.8f).height(60.dp)
        ) {
            Text("TORNAR AL JOC", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}