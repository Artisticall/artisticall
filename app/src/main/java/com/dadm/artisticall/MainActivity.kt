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
import com.dadm.artisticall.gamemodes.SoloScreen
import com.dadm.artisticall.gamemodes.DrawingScreen
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
                        composable("solo_screen") {
                            SoloScreen()
                        }
                        composable("drawing_screen") {
                            DrawingScreen(navController)
                        }
                        composable("guess_screen/{filePath}") { backStackEntry ->
                            val filePath = backStackEntry.arguments?.getString("filePath") ?: ""
                            GuessScreen(filePath, navController) // Pasar la ruta del archivo a GuessScreen
                        }
                        composable("points_screen") {
                            PointsScreen(navController)
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