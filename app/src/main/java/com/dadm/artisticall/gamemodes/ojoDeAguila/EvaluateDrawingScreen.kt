package com.dadm.artisticall.gamemodes

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

@Composable
fun EvaluateDrawingScreen(
    navController: NavController,
    originalImageUrl: String,
    drawingImagePath: String,

) {
    var rating by remember { mutableStateOf(0) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Evalúa el dibujo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Imagen original
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(originalImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagen original",
                modifier = Modifier.size(150.dp)
            )

            // Dibujo del usuario
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(drawingImagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Dibujo del usuario",
                modifier = Modifier.size(150.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "¿Qué tan parecido es el dibujo a la imagen original?",
            style = MaterialTheme.typography.bodyLarge
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
                        tint = if (star <= rating) Color(0xFFFFA040) else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // Aquí puedes implementar la lógica para guardar la calificación
                navController.navigate("results_screen")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFA040)
            )
        ) {
            Text("Enviar calificación")
        }
    }
}