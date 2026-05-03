package com.example.jetpackapploginmvvm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackapploginmvvm.model.AppDatabase
import com.example.jetpackapploginmvvm.model.User
import com.example.jetpackapploginmvvm.navigation.AppScreens
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Estat de la UI de Login
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val message: String = "",
    val errorMsg: String = "",
    val isLoading: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Instanciem el DAO per poder parlar amb Room
    private val dao = AppDatabase.getDatabase(application).appDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationChannel = Channel<String>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    fun onUsernameChange(input: String) {
        _uiState.value = _uiState.value.copy(username = input, message = "", errorMsg = "")
    }

    fun onPasswordChange(input: String) {
        _uiState.value = _uiState.value.copy(password = input, message = "", errorMsg = "")
    }

    fun onRegisterClick() {
        val current = _uiState.value
        if (current.username.isNotBlank() && current.password.isNotBlank()) {
            // Room requereix que les operacions es facin en una corrutina
            viewModelScope.launch {
                // Comprovem si l'usuari ja existeix a la base de dades
                val existingUser = dao.getUser(current.username)

                if (existingUser == null) {
                    // Si no existeix, el guardem
                    dao.insertUser(User(current.username, current.password))
                    _uiState.value = current.copy(
                        message = "Usuari registrat correctament !!",
                        username = "",
                        password = "",
                        errorMsg = ""
                    )
                } else {
                    _uiState.value = current.copy(
                        errorMsg = "ERROR: L'usuari ja existeix !!",
                        message = ""
                    )
                }
            }
        }
    }

    fun onLoginClick() {
        val current = _uiState.value
        if (current.username.isBlank() || current.password.isBlank()) {
            _uiState.value = current.copy(errorMsg = "ERROR: Omple tots els camps !!")
            return
        }

        viewModelScope.launch {
            // Busquem l'usuari a Room
            val storedUser = dao.getUser(current.username)

            if (storedUser == null) {
                _uiState.value = current.copy(errorMsg = "ERROR: L'usuari no existeix !!", message = "")
            } else {
                if (storedUser.password == current.password) {
                    // Si la contrasenya coincideix, naveguem a la Welcome Screen
                    _navigationChannel.send(AppScreens.Welcome.createRoute(current.username))
                    _uiState.value = LoginUiState() // Netegem l'estat
                } else {
                    _uiState.value = current.copy(message = "", errorMsg = "ERROR: Credencials invàlides !!")
                }
            }
        }
    }
}