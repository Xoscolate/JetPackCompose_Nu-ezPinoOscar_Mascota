package com.example.jetpackapploginmvvm.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackapploginmvvm.viewmodel.MascotaViewModel

@Composable
fun ScreenMascotaMort(
    viewModel: MascotaViewModel,
    onReiniciarJoc: () -> Unit
) {
    val nomMascota = viewModel.mascota.value?.nom?: "El teu demoni" //lo tipico de comprobar si es null (puede no tener nombre)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(20.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "RIP",

            color = Color(0xFFD32F2F),
            fontSize = 80.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "$nomMascota ha mort de fam",
            color = Color.White,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                viewModel.resetearJuego() // Borra los datos antiguos
                onReiniciarJoc()         // Nos lleva a la pantalla de crear el demonio
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("INVOCAR UN NOU DEMONI :)", color = Color.White)
        }
    }
}