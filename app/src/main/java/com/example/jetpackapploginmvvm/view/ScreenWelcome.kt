package com.example.jetpackapploginmvvm.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackapploginmvvm.viewmodel.MascotaViewModel

@Composable
fun ScreenWelcome(
    viewModel: MascotaViewModel, // 🔥 AÑADIDO EL VIEWMODEL PARA LA API
    msgWelcome: String,
    onLogoutClick: () -> Unit,
    onCloseClick: () -> Unit,
    onStartGame: () -> Unit,
) {
    // Colores demonio
    val negroFondo = Color(0xFF1A1A1A)
    val rojoDemonio = Color(0xFFD32F2F)

    // 🔥 Escuchamos el texto del oráculo desde el ViewModel
    val oraculoText by viewModel.mensajeOraculo.collectAsState()

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(negroFondo)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = msgWelcome,
            color = rojoDemonio,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(16.dp)
        )

        Text(
            text = "Cuida al teu demonio",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Boton para comenzar
        Button(
            onClick = onStartGame,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(
                containerColor = rojoDemonio,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "CUIDAR AL DEMONI",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.consultarOraculo() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082)),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("🔮 CONSULTAR L'ORACLE", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (oraculoText.isNotEmpty()) {
            Text(
                text = oraculoText,
                // Si es un error de conexión, se pone rojo. Si no, cian.
                color = if (oraculoText.contains("ERROR")) Color.Red else Color.Cyan,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .background(Color.DarkGray, CutCornerShape(8.dp))
                    .padding(12.dp)
                    .fillMaxWidth(0.9f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Canviar Usuari")
        }

        Button(
            onClick = onCloseClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Tancar Aplicació", color = Color.Gray)
        }
    }
}