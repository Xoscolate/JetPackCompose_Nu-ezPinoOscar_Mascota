package com.example.jetpackapploginmvvm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.PopUpToBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jetpackapploginmvvm.view.ScreenLogin
import com.example.jetpackapploginmvvm.view.ScreenWelcome
import com.example.jetpackapploginmvvm.view.simon.ScreenSimon
import com.example.jetpackapploginmvvm.viewmodel.LoginViewModel
import com.example.jetpackapploginmvvm.view.ScreenMascotaCrear
import com.example.jetpackapploginmvvm.view.ScreenMascotaJoc
import com.example.jetpackapploginmvvm.view.ScreenMascotaMort
import com.example.jetpackapploginmvvm.view.ScreenPersonalizacion
import com.example.jetpackapploginmvvm.view.simon.ScreenMascotaDormir
import com.example.jetpackapploginmvvm.viewmodel.MascotaViewModel


fun setInclusiveTrue(builder: PopUpToBuilder) {
    builder.inclusive = true
}

fun configurarPopUpLogin(builder: NavOptionsBuilder) {
    builder.popUpTo(AppScreens.Login.route, ::setInclusiveTrue)
}

fun configurarArgUsername(builder: androidx.navigation.NavArgumentBuilder) {
    builder.type = NavType.StringType
}

@Composable
fun AppNavigation(
    onCloseApp: () -> Unit
){
    val navController = rememberNavController()
    val mascotaViewModel: MascotaViewModel = viewModel()

    fun ferLogout() = navController.navigate(AppScreens.Login.route, ::configurarPopUpLogin)
    fun anarASimon() = navController.navigate(AppScreens.Simon.route)
    fun tornarEnrere() = navController.popBackStack()

    fun anarACrearMascota() = navController.navigate(AppScreens.MascotaCrear.route)
    fun anarAlJocMascota() = navController.navigate(AppScreens.MascotaJoc.route)
    fun anarAMascotaMort() = navController.navigate(AppScreens.MascotaMort.route)

    fun processarRutaViewModelLogin(route: String) {
        navController.navigate(route, ::configurarPopUpLogin)
    }

    NavHost(navController = navController, startDestination = AppScreens.Login.route) {

        composable( route= AppScreens.Login.route ){
            val viewModel: LoginViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(key1 = true) {
                viewModel.navigationChannel.collect ( ::processarRutaViewModelLogin)
            }

            ScreenLogin(
                state = state,
                onUsernameChange = viewModel::onUsernameChange,
                onPasswordChange = viewModel::onPasswordChange,
                onRegisterClick = viewModel::onRegisterClick,
                onLoginClick = viewModel::onLoginClick,
                onCloseClick = onCloseApp
            )
        }

        composable(
            route = AppScreens.Welcome.route,
            arguments = listOf(navArgument("username", :: configurarArgUsername))
        ){ backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "Desconegut"

            val mascotaState by mascotaViewModel.mascota.collectAsState()

            LaunchedEffect(username) {
                mascotaViewModel.cargarMascotaDeUsuario(username)
            }

            ScreenWelcome(
                msgWelcome = "Hola, $username",
                onLogoutClick = ::ferLogout,
                onCloseClick = onCloseApp,
                onStartGame = {
                    // 🔥 2. Usamos mascotaState en lugar de mascota.value
                    if (mascotaState?.estaViva == true) {
                        anarAlJocMascota()
                    } else {
                        anarACrearMascota()
                    }
                }
            )
        }


        composable(route = AppScreens.MascotaCrear.route) {
            ScreenMascotaCrear(
                viewModel = mascotaViewModel,
                onMascotaCreada = ::anarAlJocMascota,
                onBackClick = ::tornarEnrere
            )
        }

        composable(route = AppScreens.MascotaJoc.route) {
            val mascotaState by mascotaViewModel.mascota.collectAsState()
            val username = mascotaState?.ownerUsername

            ScreenMascotaJoc(
                viewModel = mascotaViewModel,
                username = username,
                onMascotaMorta = ::anarAMascotaMort,
                onDormirClick = { navController.navigate(AppScreens.MascotaDormir.route) },
                onPersonalizarClick = { navController.navigate(AppScreens.Personalizacion.route) },
                onSimonClick = {
                    mascotaViewModel.entrarAlSimon()
                    navController.navigate(AppScreens.Simon.route)
                },
                onBackClick = {
                    navController.popBackStack(AppScreens.Welcome.route, inclusive = false)
                }
            )
        }
        composable(route = AppScreens.MascotaMort.route) {
            ScreenMascotaMort(
                viewModel = mascotaViewModel,
                onReiniciarJoc = {
                    navController.navigate(AppScreens.MascotaCrear.route) {
                        popUpTo(AppScreens.MascotaCrear.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = AppScreens.MascotaDormir.route) {
            ScreenMascotaDormir(
                viewModel = mascotaViewModel,
                onDespertar = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppScreens.Personalizacion.route) {
            ScreenPersonalizacion(
                viewModel = mascotaViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable (route = AppScreens.Simon.route){
            ScreenSimon(
                onBackClick = {
                    mascotaViewModel.salirDelSimon() // <-- Comprueba si ha pasado los 2 minutos
                    tornarEnrere()
                },
                onCloseClick = onCloseApp
            )
        }
    }
}