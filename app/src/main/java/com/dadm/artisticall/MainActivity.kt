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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dadm.artisticall.gamemodes.EvaluateDrawingScreen
import com.dadm.artisticall.gamemodes.GameBatallaPincelesScreen
import com.dadm.artisticall.gamemodes.GameColaborativoScreen
import com.dadm.artisticall.gamemodes.GameDesafioScreen
import com.dadm.artisticall.gamemodes.GameEraseUnaVezScreen
import com.dadm.artisticall.gamemodes.GameLibreScreen
import com.dadm.artisticall.gamemodes.modoNormal.GameNormalScreen
import com.dadm.artisticall.gamemodes.modoNormal.FinalSequenceScreen
import com.dadm.artisticall.gamemodes.modoNormal.WritePhraseScreen
import com.dadm.artisticall.gamemodes.modoNormal.GuessScreen

import com.dadm.artisticall.gamemodes.modoDesafio.GameDesafioScreen
import com.dadm.artisticall.gamemodes.modoDesafio.FinalSequenceScreenDesafio
import com.dadm.artisticall.gamemodes.modoDesafio.WritePhraseScreenDesafio
import com.dadm.artisticall.gamemodes.modoDesafio.GuessScreenDesafio


import com.dadm.artisticall.gamemodes.ojoDeAguila.GameOjoDeAguilaScreen
import com.dadm.artisticall.gamemodes.GamePonleTituloScreen
import com.dadm.artisticall.gamemodes.GameQueEsEstoScreen
import com.dadm.artisticall.gamemodes.modoAdivina.GameAdivinaScreen
import com.dadm.artisticall.gamemodes.modoSolo.GameSoloScreen
import com.dadm.artisticall.gamemodes.modoSolo.SoloGuess
import com.dadm.artisticall.gamemodes.ojoDeAguila.ResultsScreen
import com.dadm.artisticall.lobby.GameMode
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
                        composable("game_normal_screen/{lobbyCode}/{username}") {backStackEntry ->
                            val lobbyCode = backStackEntry.arguments?.getString("lobbyCode")
                            val username = backStackEntry.arguments?.getString("username")
                            GameNormalScreen(
                                navController = navController,
                                lobbyCode = lobbyCode ?: "",
                                username = username ?: ""
                            )
                        }
                        composable("guess_screen/{lobbyCode}/{username}") { backStackEntry ->
                            val lobbyCode = backStackEntry.arguments?.getString("lobbyCode")
                            val username = backStackEntry.arguments?.getString("username")
                            GuessScreen(
                                navController = navController,
                                lobbyCode = lobbyCode ?: "",
                                username = username ?: ""
                            )
                        }
                        composable("final_sequence_screen/{lobbyCode}/{username}") { backStackEntry ->
                            val lobbyCode = backStackEntry.arguments?.getString("lobbyCode")
                            val username = backStackEntry.arguments?.getString("username")
                            FinalSequenceScreen(
                                navController = navController,
                                lobbyCode = lobbyCode ?: "",
                                username = username ?: ""
                            )
                        }

                       // navigation desafio

                       composable("write_phrase_screen_desafio/{lobbyCode}/{username}") { backStackEntry ->
                           val lobbyCode = backStackEntry.arguments?.getString("lobbyCode")
                           val username = backStackEntry.arguments?.getString("username")
                           WritePhraseScreenDesafio(
                               navController = navController,
                               lobbyCode = lobbyCode ?: "",
                               username = username ?: ""
                           )
                       }
                       composable("game_desafio_screen/{lobbyCode}/{username}") {backStackEntry ->
                           val lobbyCode = backStackEntry.arguments?.getString("lobbyCode")
                           val username = backStackEntry.arguments?.getString("username")
                           GameDesafioScreen(
                               navController = navController,
                               lobbyCode = lobbyCode ?: "",
                               username = username ?: ""
                           )
                       }
                       composable("guess_screen_desafio/{lobbyCode}/{username}") { backStackEntry ->
                           val lobbyCode = backStackEntry.arguments?.getString("lobbyCode")
                           val username = backStackEntry.arguments?.getString("username")
                           GuessScreenDesafio(
                               navController = navController,
                               lobbyCode = lobbyCode ?: "",
                               username = username ?: ""
                           )
                       }
                       composable("final_sequence_screen_desafio/{lobbyCode}/{username}") { backStackEntry ->
                           val lobbyCode = backStackEntry.arguments?.getString("lobbyCode")
                           val username = backStackEntry.arguments?.getString("username")
                           FinalSequenceScreenDesafio(
                               navController = navController,
                               lobbyCode = lobbyCode ?: "",
                               username = username ?: ""
                           )
                       }

                        composable("points_screen") {
                            PointsScreen(navController)
                        }
                        composable("game_solo_screen") {
                            GameSoloScreen(navController)
                        }
                        composable(
                            "guess_solo_screen/{imagePath}",
                            arguments = listOf(navArgument("imagePath") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val imagePath = backStackEntry.arguments?.getString("imagePath") ?: ""
                            SoloGuess(navController, imagePath)
                        }
                        composable("game_adivina_screen/{lobbyCode}/{username}") { backStackEntry ->
                            val lobbyCode = backStackEntry.arguments?.getString("lobbyCode")
                            val username = backStackEntry.arguments?.getString("username")
                            GameAdivinaScreen(
                                navController = navController,
                                lobbyCode = lobbyCode ?: "",
                                username = username ?: ""
                            )
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
                        composable("game_libre_screen/{lobbyCode}/{username}") { backStackEntry ->
                            val lobbyCode = backStackEntry.arguments?.getString("lobbyCode")
                            val username = backStackEntry.arguments?.getString("username")
                            GameLibreScreen(
                                navController = navController,
                                lobbyCode = lobbyCode ?: "",
                                username = username ?: ""
                            )
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
                        composable("results_screen") {
                                backStackEntry ->
                            val lobbyCode = backStackEntry.arguments?.getString("lobbyCode")
                            val username = backStackEntry.arguments?.getString("username")
                            ResultsScreen(
                                navController = navController,
                                lobbyCode = lobbyCode ?: "",
                                username = username ?: ""
                            )
                        }
                        composable(
                            route = "evaluate_screen/{originalImage}/{drawingPath}",
                            arguments = listOf(
                                navArgument("originalImage") { type = NavType.StringType },
                                navArgument("drawingPath") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            EvaluateDrawingScreen(
                                navController = navController,
                                originalImageUrl = backStackEntry.arguments?.getString("originalImage") ?: "",
                                drawingImagePath = backStackEntry.arguments?.getString("drawingPath") ?: ""
                            )
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