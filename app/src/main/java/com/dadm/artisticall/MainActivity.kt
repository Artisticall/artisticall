package com.dadm.artisticall

import LobbyScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dadm.artisticall.gamemodes.GameAdivinaScreen
import com.dadm.artisticall.gamemodes.GameBatallaPincelesScreen
import com.dadm.artisticall.gamemodes.GameColaborativoScreen
import com.dadm.artisticall.gamemodes.GameDesafioScreen
import com.dadm.artisticall.gamemodes.GameEraseUnaVezScreen
import com.dadm.artisticall.gamemodes.GameLibreScreen
import com.dadm.artisticall.gamemodes.GameNormalScreen
import com.dadm.artisticall.gamemodes.GameOjoDeAguilaScreen
import com.dadm.artisticall.gamemodes.GamePonleTituloScreen
import com.dadm.artisticall.gamemodes.GameQueEsEstoScreen
import com.dadm.artisticall.gamemodes.GameSoloScreen
import com.dadm.artisticall.gamemodes.GuessScreen
import com.dadm.artisticall.gamemodes.WritePhraseScreen
import com.dadm.artisticall.lobby.GameMode
import com.dadm.artisticall.lobby.PointsScreen
import com.dadm.artisticall.login.LoginScreen
import com.dadm.artisticall.ui.theme.ArtisticallTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtisticallTheme {
                val navController = rememberNavController()
                var selectedGameMode by remember { mutableStateOf<GameMode?>(null) }

                val onGameModeSelected: (GameMode?) -> Unit = { mode ->
                    selectedGameMode = mode
                }
                NavHost(
                    navController = navController,
                    startDestination = "login_screen",
                    builder = {
                        composable("login_screen") {
                            LoginScreen(
                                navController = navController
                            )
                        }
                        composable("lobby_screen/{lobbyCode}/{username}") { backStackEntry ->
                            val lobbyCode = backStackEntry.arguments?.getString("lobbyCode")
                            val username = backStackEntry.arguments?.getString("username")
                            LobbyScreen(
                                navController = navController,
                                lobbyCode = lobbyCode,
                                username = username,
                                selectedGameMode = selectedGameMode,
                                onGameModeSelected = onGameModeSelected
                            )
                        }
                        composable("write_phrase_screen/{lobbyCode}/{username}") { backStackEntry ->
                            val lobbyCode = backStackEntry.arguments?.getString("lobbyCode")
                            val username = backStackEntry.arguments?.getString("username")
                            WritePhraseScreen(
                                navController = navController,
                                lobbyCode = lobbyCode ?: "",
                                username = username ?: ""
                            )
                        }
                        composable("game_normal_screen") {
                            GameNormalScreen(navController)
                        }
                        composable("guess_screen/{filePath}") { backStackEntry ->
                            val filePath = backStackEntry.arguments?.getString("filePath") ?: ""
                            GuessScreen(filePath, navController)
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