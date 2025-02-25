package com.dadm.artisticall.gamemodes.ojoDeAguila

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun WaitingResultsScreen(
    navController: NavController,
    lobbyCode: String,
    username: String
) {
    var isCheckingComplete by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Verificar periódicamente si todas las evaluaciones están completas
    LaunchedEffect(Unit) {
        while (isCheckingComplete) {
            try {
                val isComplete = checkAllEvaluationsComplete(lobbyCode)
                if (isComplete) {
                    // Todas las evaluaciones están completas, navegar a la pantalla de resultados
                    navController.navigate("results_screen/$lobbyCode/$username")
                    break
                }
            } catch (e: Exception) {
                Log.e("WaitingResultsScreen", "Error al verificar evaluaciones: ${e.message}")
                errorMessage = "Error al verificar el estado: ${e.message}"
            }

            // Esperar antes de volver a verificar
            delay(5000) // 5 segundos
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2D36))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Esperando a que todos terminen de evaluar...",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFFFFA040),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        CircularProgressIndicator(
            color = Color(0xFFFFA040),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Función para verificar si todas las evaluaciones están completas
suspend fun checkAllEvaluationsComplete(lobbyCode: String): Boolean {
    val firestore = Firebase.firestore

    // Obtener la lista de jugadores
    val gameSnapshot = firestore.collection("games")
        .document(lobbyCode)
        .get()
        .await()

    val players = gameSnapshot.get("usernames") as? List<String> ?: emptyList()
    val totalPlayers = players.size

    if (totalPlayers == 0) return false

    // Obtener todas las evaluaciones
    val evaluationsSnapshot = firestore.collection("lobbies")
        .document(lobbyCode)
        .collection("evaluations")
        .get()
        .await()

    val evaluations = evaluationsSnapshot.documents

    // Verificar que todos los dibujos tienen evaluaciones de todos los demás jugadores
    for (evaluation in evaluations) {
        val authorId = evaluation.getString("authorId") ?: continue
        val evaluatedBy = evaluation.get("evaluatedBy") as? List<*> ?: emptyList<String>()

        // Cada dibujo debe ser evaluado por todos los jugadores excepto su autor
        val requiredEvaluators = players.filter { it != authorId }

        // Si algún jugador aún no ha evaluado, no está completo
        if (!requiredEvaluators.all { evaluatedBy.contains(it) }) {
            return false
        }
    }

    // Si llegamos aquí, todas las evaluaciones están completas
    return true
}