package com.example.jetpackapploginmvvm.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackapploginmvvm.viewmodel.MascotaViewModel

@Composable
fun ScreenMascotaCrear(
    viewModel: MascotaViewModel,
    onMascotaCreada: () -> Unit,
    onBackClick: () -> Unit
) {
    val negroFondo = Color(0xFF121212)
    val grisCajas = Color(0xFF1E1E1E)
    val rojoDemonio = Color(0xFFB22222)

    // Estado local para escribir el nombre en el cuadro de texto
    var nombreTexto by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(negroFondo)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "RITUAL D'INVOCACIÓ",
            color = rojoDemonio,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Com vols que es digui la teva nova criatura de les tenebres?",
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Cuadro para escribir el nombre (con el nuevo diseño)
        OutlinedTextField(
            value = nombreTexto,
            onValueChange = { nombreTexto = it },
            label = { Text("Nom del demoni", color = Color.Gray) },
            shape = CutCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = grisCajas,
                unfocusedContainerColor = grisCajas,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = rojoDemonio,
                unfocusedIndicatorColor = Color.DarkGray,
                cursorColor = rojoDemonio
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para crear
        Button(
            onClick = {
                if (nombreTexto.isNotBlank()) {
                    viewModel.crearMascota(nombreTexto)
                    onMascotaCreada() // Esto nos llevará a la pantalla del juego
                }
            },
            shape = CutCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = rojoDemonio),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text("COMENÇAR AVENTURA!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para volver atrás
        TextButton(onClick = onBackClick) {
            Text("Cance·lar Ritual", color = Color.Gray, fontSize = 14.sp)
        }
    }
}