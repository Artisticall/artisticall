package com.dadm.artisticall

import LobbyScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dadm.artisticall.gamemodes.GameAdivinaScreen
import com.dadm.artisticall.gamemodes.GameBatallaPincelesScreen
import com.dadm.artisticall.gamemodes.GameColaborativoScreen
import com.dadm.artisticall.gamemodes.GameDesafioScreen
import com.dadm.artisticall.gamemodes.GameEraseUnaVezScreen
import com.dadm.artisticall.gamemodes.GameLibreScreen
import com.dadm.artisticall.gamemodes.GameOjoDeAguilaScreen
import com.dadm.artisticall.gamemodes.GamePonleTituloScreen
import com.dadm.artisticall.gamemodes.GameQueEsEstoScreen
import com.dadm.artisticall.gamemodes.GameSoloScreen
import com.dadm.artisticall.gamemodes.GuessScreen
import com.dadm.artisticall.lobby.PointsScreen
import com.dadm.artisticall.login.LoginScreen
import com.dadm.artisticall.ui.theme.ArtisticallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtisticallTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "login_screen",
                    builder = {
                        composable("login_screen") {
                            LoginScreen(
                                navController = navController
                            )
                        }
                        composable("lobby_screen") {
                            LobbyScreen(
                                navController = navController
                            )
                        }
                        composable("game_normal_screen") {
                            GameBatallaPincelesScreen(navController)
                        }
                        composable("guess_screen/{filePath}") { backStackEntry ->
                            val filePath = backStackEntry.arguments?.getString("filePath") ?: ""
                            GuessScreen(filePath, navController) // Pasar la ruta del archivo a GuessScreen
                        }
                        composable("points_screen") {
                            PointsScreen(navController)
                        }
                        composable("game_solo_screen") {
                            GameSoloScreen(navController)
                        }
                        composable("game_adivina_screen") {
                            GameAdivinaScreen(navController)
                        }
                        composable("game_que_es_esto_screen") {
                            GameQueEsEstoScreen(navController)
                        }
                        composable("game_batalla_pinceles_screen") {
                            GameBatallaPincelesScreen(navController)
                        }
                        composable("game_ponle_titulo_screen") {
                            GamePonleTituloScreen(navController)
                        }
                        composable("game_erase_una_vez_screen") {
                            GameEraseUnaVezScreen(navController)
                        }
                        composable("game_libre_screen") {
                            GameLibreScreen(navController)
                        }
                        composable("game_ojo_de_aguila_screen") {
                            GameOjoDeAguilaScreen(navController)
                        }
                        composable("game_colaborativo_screen") {
                            GameColaborativoScreen(navController)
                        }
                        composable("game_desafio_screen") {
                            GameDesafioScreen(navController)
                        }
                    },
                    enterTransition = {
                        EnterTransition.None
                    },
                    exitTransition = {
                        ExitTransition.None
                    }
                )
            }
        }
    }
}