package com.dadm.artisticall.gamemodes.ojoDeAguila

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream

@Composable
fun EvaluateDrawingScreen(
    navController: NavController,
    originalImageUrl: String,
    drawingImagePath: String,
    lobbyCode: String,
    username: String
) {
    var rating by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = Firebase.firestore

    // Estado para controlar cuando se completa la calificación
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionComplete by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Función para guardar la calificación en Firestore
    fun saveRating() {
        isSubmitting = true

        // Leer el archivo de dibujo como un array de bytes
        val file = File(drawingImagePath)
        val drawingBytes = try {
            val fileInputStream = FileInputStream(file)
            val bytes = fileInputStream.readBytes()
            fileInputStream.close()
            bytes.map { it.toInt() }
        } catch (e: Exception) {
            Log.e("EvaluateDrawingScreen", "Error al leer el archivo: ${e.message}")
            isSubmitting = false
            errorMessage = "Error al leer el archivo del dibujo"
            return
        }

        // Datos del dibujo y su calificación
        val drawingData = hashMapOf(
            "originalImageUrl" to originalImageUrl,
            "drawingData" to drawingBytes,
            "authorId" to username,
            "ratings" to hashMapOf(username to rating),
            "averageRating" to rating.toDouble(),
            "evaluatedBy" to listOf(username)
        )

        // Guardar en Firestore
        firestore.collection("lobbies")
            .document(lobbyCode)
            .collection("evaluations")
            .add(drawingData)
            .addOnSuccessListener { documentReference ->
                Log.d("EvaluateDrawingScreen", "Dibujo guardado con ID: ${documentReference.id}")

                // Verificar si todos los jugadores han evaluado todos los dibujos
                checkAllEvaluationsComplete()
            }
            .addOnFailureListener { e ->
                Log.e("EvaluateDrawingScreen", "Error al guardar el dibujo: ${e.message}")
                isSubmitting = false
                errorMessage = "Error al guardar la calificación: ${e.message}"
            }
    }

    // Función para verificar si hay un dibujo disponible para evaluar
    fun checkForDrawingToEvaluate() {
        scope.launch {
            try {
                // Buscar un dibujo que no haya sido evaluado por el usuario actual
                val evaluationsSnapshot = firestore.collection("lobbies")
                    .document(lobbyCode)
                    .collection("evaluations")
                    .whereNotEqualTo("authorId", username)  // No evaluar el propio dibujo
                    .whereNotIn("evaluatedBy", listOf(username))  // No evaluar dibujos ya evaluados
                    .limit(1)
                    .get()
                    .await()

                if (!evaluationsSnapshot.isEmpty) {
                    // Hay un dibujo disponible para evaluar
                    val drawingDoc = evaluationsSnapshot.documents[0]
                    val drawingId = drawingDoc.id
                    navController.navigate("evaluate_other_screen/$lobbyCode/$username/$drawingId")
                } else {
                    // No hay más dibujos para evaluar, esperar resultados
                    navController.navigate("waiting_results_screen/$lobbyCode/$username")
                }
            } catch (e: Exception) {
                Log.e("EvaluateDrawingScreen", "Error al buscar dibujo para evaluar: ${e.message}")
                isSubmitting = false
                errorMessage = "Error al buscar dibujo para evaluar: ${e.message}"
            }
        }
    }

    // Función para verificar si todas las evaluaciones están completas
    fun checkAllEvaluationsComplete() {
        scope.launch {
            try {
                // Obtener la lista de jugadores
                val gameSnapshot = firestore.collection("games")
                    .document(lobbyCode)
                    .get()
                    .await()

                val players = gameSnapshot.get("usernames") as? List<String> ?: emptyList()
                val totalPlayers = players.size

                // Obtener todas las evaluaciones
                val evaluationsSnapshot = firestore.collection("lobbies")
                    .document(lobbyCode)
                    .collection("evaluations")
                    .get()
                    .await()

                val evaluations = evaluationsSnapshot.documents
                val totalEvaluations = evaluations.size

                // Si hay suficientes evaluaciones (1 por jugador) y el jugador actual ha evaluado
                if (totalEvaluations >= totalPlayers) {
                    // Navegar a la pantalla de resultados
                    submissionComplete = true
                    // Navegar a la pantalla de resultados
                    navController.navigate("results_screen/$lobbyCode/$username")
                } else {
                    // Verificar si hay un dibujo disponible para evaluar
                    checkForDrawingToEvaluate()
                }
            } catch (e: Exception) {
                Log.e("EvaluateDrawingScreen", "Error al verificar evaluaciones: ${e.message}")
                isSubmitting = false
                errorMessage = "Error al verificar evaluaciones: ${e.message}"
            }
        }
    }

    // Función para verificar si hay un dibujo disponible para evaluar
    fun checkForDrawingToEvaluate(lobbyCode: String, username: String) {
        scope.launch {
            try {
                // Buscar un dibujo que no haya sido evaluado por el usuario actual
                val evaluationsSnapshot = firestore.collection("lobbies")
                    .document(lobbyCode)
                    .collection("evaluations")
                    .whereNotEqualTo("authorId", username)  // No evaluar el propio dibujo
                    .whereNotIn("evaluatedBy", listOf(username))  // No evaluar dibujos ya evaluados
                    .limit(1)
                    .get()
                    .await()

                if (!evaluationsSnapshot.isEmpty) {
                    // Hay un dibujo disponible para evaluar
                    val drawingDoc = evaluationsSnapshot.documents[0]
                    val drawingId = drawingDoc.id
                    navController.navigate("evaluate_other_screen/$lobbyCode/$username/$drawingId")
                } else {
                    // No hay más dibujos para evaluar, esperar resultados
                    navController.navigate("waiting_results_screen/$lobbyCode/$username")
                }
            } catch (e: Exception) {
                Log.e("EvaluateDrawingScreen", "Error al buscar dibujo para evaluar: ${e.message}")
                isSubmitting = false
                errorMessage = "Error al buscar dibujo para evaluar: ${e.message}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Evalúa el dibujo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            color = Color(0xFFFFA040)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Imagen original
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Original",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(originalImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen original",
                    modifier = Modifier.size(150.dp)
                )
            }

            // Dibujo del usuario
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tu dibujo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(drawingImagePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Dibujo del usuario",
                    modifier = Modifier.size(150.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "¿Qué tan parecido es el dibujo a la imagen original?",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Estrellas de calificación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            (1..5).forEach { star ->
                IconButton(
                    onClick = { rating = star }
                ) {
                    Icon(
                        imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Estrella $star",
                        tint = if (star <= rating) Color(0xFFFFA040) else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Mensaje de error si hay uno
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = { saveRating() },
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFFFFA040)
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040)),
            enabled = rating > 0 && !isSubmitting && !submissionComplete
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = Color(0xFFFFA040),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = "Enviar calificación")
        }
    }
}