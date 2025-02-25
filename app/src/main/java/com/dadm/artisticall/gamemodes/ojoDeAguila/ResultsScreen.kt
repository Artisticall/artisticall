package com.dadm.artisticall.gamemodes.ojoDeAguila

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

data class EvaluationResult(
    val id: String,
    val authorId: String,
    val originalImageUrl: String,
    val drawingBitmap: Bitmap?,
    val averageRating: Double,
    val totalEvaluations: Int
)

@Composable
fun ResultsScreen(
    navController: NavController,
    lobbyCode: String,
    username: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = Firebase.firestore

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var evaluationResults by remember { mutableStateOf<List<EvaluationResult>>(emptyList()) }
    var userResults by remember { mutableStateOf<EvaluationResult?>(null) }

    // Cargar los resultados
    LaunchedEffect(Unit) {
        try {
            val evaluationsSnapshot = firestore.collection("lobbies")
                .document(lobbyCode)
                .collection("evaluations")
                .get()
                .await()

            val results = evaluationsSnapshot.documents.mapNotNull { doc ->
                try {
                    val authorId = doc.getString("authorId") ?: return@mapNotNull null
                    val originalImageUrl = doc.getString("originalImageUrl") ?: return@mapNotNull null
                    val averageRating = doc.getDouble("averageRating") ?: 0.0
                    val evaluatedBy = doc.get("evaluatedBy") as? List<*> ?: emptyList<String>()

                    // Convertir la lista de enteros a un bitmap
                    val drawingData = doc.get("drawingData") as? List<*> ?: return@mapNotNull null
                    val byteArray = drawingData.map { (it as Number).toByte() }.toByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                    EvaluationResult(
                        id = doc.id,
                        authorId = authorId,
                        originalImageUrl = originalImageUrl,
                        drawingBitmap = bitmap,
                        averageRating = averageRating,
                        totalEvaluations = evaluatedBy.size
                    )
                } catch (e: Exception) {
                    Log.e("ResultsScreen", "Error procesando documento: ${e.message}")
                    null
                }
            }

            // Ordenar por calificación promedio (de mayor a menor)
            evaluationResults = results.sortedByDescending { it.averageRating }

            // Obtener el resultado del usuario actual
            userResults = evaluationResults.find { it.authorId == username }

            isLoading = false
        } catch (e: Exception) {
            Log.e("ResultsScreen", "Error al cargar resultados: ${e.message}")
            errorMessage = "Error al cargar los resultados: ${e.message}"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2D36))
            .padding(16.dp)
    ) {
        Text(
            text = "Resultados",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFFFA040),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFA040))
            }
        } else if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            // Resultado del usuario actual
            userResults?.let { result ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A3F4D)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Tu puntuación",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFFA040)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Imagen original
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Original", color = Color.White)
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(result.originalImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Imagen original",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .padding(8.dp)
                                )
                            }

                            // Dibujo del usuario
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Tu dibujo", color = Color.White)
                                result.drawingBitmap?.let { bitmap ->
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Tu dibujo",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .padding(8.dp)
                                    )
                                }
                            }

                            // Puntuación
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "Puntuación",
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Text(
                                    text = String.format("%.1f", result.averageRating),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color(0xFFFFA040),
                                    fontWeight = FontWeight.Bold
                                )

                                Row {
                                    repeat(5) { index ->
                                        val filled = index < result.averageRating
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = if (filled) Color(0xFFFFA040) else Color.Gray.copy(alpha = 0.5f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "${result.totalEvaluations} evaluaciones",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Título de clasificación
            Text(
                text = "Clasificación",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Lista de todos los resultados
            LazyColumn {
                items(evaluationResults) { result ->
                    DrawingResultItem(
                        result = result,
                        isCurrentUser = result.authorId == username,
                        position = evaluationResults.indexOf(result) + 1
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón para volver al lobby
            Button(
                onClick = { navController.navigate("lobby_screen/$lobbyCode/$username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFFFA040)
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
            ) {
                Text("Volver al Lobby")
            }
        }
    }
}

@Composable
fun DrawingResultItem(
    result: EvaluationResult,
    isCurrentUser: Boolean,
    position: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) Color(0xFF2A3F4D) else Color(0xFF1E2D36)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición
            Text(
                text = "$position",
                style = MaterialTheme.typography.titleLarge,
                color = when (position) {
                    1 -> Color(0xFFFFD700) // Oro
                    2 -> Color(0xFFC0C0C0) // Plata
                    3 -> Color(0xFFCD7F32) // Bronce
                    else -> Color.White
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp)
            )

            // Información del dibujo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isCurrentUser) "Tú" else result.authorId,
                    color = if (isCurrentUser) Color(0xFFFFA040) else Color.White,
                    fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
                )

                Row {
                    repeat(5) { index ->
                        val filled = index < result.averageRating
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (filled) Color(0xFFFFA040) else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Puntuación
            Text(
                text = String.format("%.1f", result.averageRating),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFFA040),
                modifier = Modifier.padding(start = 8.dp)
            )

            // Thumbnail del dibujo
            result.drawingBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Dibujo de ${result.authorId}",
                    modifier = Modifier
                        .size(50.dp)
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}