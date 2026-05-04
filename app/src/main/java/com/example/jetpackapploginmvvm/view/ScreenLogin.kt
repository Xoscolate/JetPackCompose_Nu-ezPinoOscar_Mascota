package com.example.jetpackapploginmvvm.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackapploginmvvm.viewmodel.LoginUiState

@Composable
fun ScreenLogin(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onCloseClick: () -> Unit
){
    val negroFondo = Color(0xFF121212)
    val grisCajas = Color(0xFF1E1E1E)
    val rojoDemonio = Color(0xFFB22222)
    val lilaOscuro = Color(0xFF4B0082)

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(negroFondo)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PORTA DE L'INFERN",
            color = rojoDemonio,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (state.message.isNotEmpty()) {
            Text(
                text = state.message,
                color = Color.Green,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color(0xFF003300), CutCornerShape(8.dp))
                    .padding(12.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChange,
            label = { Text("Usuari", color = Color.Gray) },
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

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text("Contrasenya", color = Color.Gray) },
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

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onRegisterClick,
                shape = CutCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = lilaOscuro),
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp)
            ) {
                Text("CREAR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Button(
                onClick = onLoginClick,
                shape = CutCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = rojoDemonio),
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp)
            ) {
                Text("ENTRAR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.errorMsg.isNotEmpty()) {
            Text(
                text = state.errorMsg,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color(0xFF8B0000), CutCornerShape(8.dp))
                    .padding(12.dp)
                    .fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // BOTÓN SALIR
        TextButton(onClick = onCloseClick) {
            Text("FUGIR COVARDAMENT", color = Color.Gray, fontSize = 14.sp)
        }
    }
}