package com.dadm.artisticall.gamemodes.ojoDeAguila

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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

@Composable
fun EvaluateOtherScreen(
    navController: NavController,
    lobbyCode: String,
    username: String,
    drawingId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = Firebase.firestore

    var rating by remember { mutableStateOf(0) }
    var originalImageUrl by remember { mutableStateOf<String?>(null) }
    var drawingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var authorName by remember { mutableStateOf<String?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Función para verificar si hay más dibujos para evaluar
    // Definida aquí arriba para evitar problemas de referencia
    val checkForMoreDrawings: () -> Unit = {
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
                    // Hay otro dibujo para evaluar
                    val nextDrawingId = evaluationsSnapshot.documents[0].id
                    navController.navigate("evaluate_other_screen/$lobbyCode/$username/$nextDrawingId")
                } else {
                    // No hay más dibujos para evaluar, ir a resultados o esperar
                    navController.navigate("waiting_results_screen/$lobbyCode/$username")
                }
            } catch (e: Exception) {
                Log.e("EvaluateOtherScreen", "Error al buscar más dibujos: ${e.message}")
                isSubmitting = false
                errorMessage = "Error al buscar más dibujos: ${e.message}"
            }
        }
    }

    // Cargar datos del dibujo
    LaunchedEffect(drawingId) {
        try {
            val drawingDoc = firestore.collection("lobbies")
                .document(lobbyCode)
                .collection("evaluations")
                .document(drawingId)
                .get()
                .await()

            if (drawingDoc.exists()) {
                originalImageUrl = drawingDoc.getString("originalImageUrl")
                authorName = drawingDoc.getString("authorId")

                // Convertir la lista de enteros a un bitmap
                val drawingData = drawingDoc.get("drawingData") as? List<*>
                if (drawingData != null) {
                    val byteArray = drawingData.map { (it as Number).toByte() }.toByteArray()
                    drawingBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                }
            } else {
                errorMessage = "No se encontró el dibujo"
            }

            isLoading = false
        } catch (e: Exception) {
            Log.e("EvaluateOtherScreen", "Error al cargar el dibujo: ${e.message}")
            errorMessage = "Error al cargar el dibujo: ${e.message}"
            isLoading = false
        }
    }

    // Función para guardar la calificación en Firestore
    fun saveRating() {
        if (rating <= 0) return

        isSubmitting = true

        // Actualizaciones para Firestore
        val drawingRef = firestore.collection("lobbies")
            .document(lobbyCode)
            .collection("evaluations")
            .document(drawingId)

        // Transacción para actualizar la calificación y recalcular el promedio
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(drawingRef)

            // Obtener las calificaciones actuales
            val ratings = snapshot.get("ratings") as? Map<*, *> ?: mapOf<String, Int>()
            val evaluatedBy = snapshot.get("evaluatedBy") as? List<*> ?: listOf<String>()

            // Calcular la nueva calificación promedio
            val newRatings = ratings.toMutableMap()
            newRatings[username] = rating

            val averageRating = newRatings.values.map {
                when (it) {
                    is Number -> it.toDouble()
                    else -> 0.0
                }
            }.average()

            // Actualizar el documento
            transaction.update(drawingRef, "ratings.$username", rating)
            transaction.update(drawingRef, "averageRating", averageRating)
            transaction.update(drawingRef, "evaluatedBy", evaluatedBy + username)
        }.addOnSuccessListener {
            Log.d("EvaluateOtherScreen", "Calificación guardada exitosamente")

            // Verificar si hay más dibujos para evaluar o ir a resultados
            checkForMoreDrawings()
        }.addOnFailureListener { e ->
            Log.e("EvaluateOtherScreen", "Error al guardar la calificación: ${e.message}")
            errorMessage = "Error al guardar la calificación: ${e.message}"
            isSubmitting = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2D36))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Evalúa el dibujo de ${authorName ?: "otro jugador"}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            color = Color(0xFFFFA040)
        )

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFFFFA040))
        } else if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
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

                // Dibujo del otro usuario
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Dibujo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    drawingBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Dibujo del usuario",
                            modifier = Modifier.size(150.dp)
                        )
                    } ?: Text("No se pudo cargar el dibujo", color = Color.Red)
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

            Button(
                onClick = { saveRating() },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFFFA040)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040)),
                enabled = rating > 0 && !isSubmitting
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
}