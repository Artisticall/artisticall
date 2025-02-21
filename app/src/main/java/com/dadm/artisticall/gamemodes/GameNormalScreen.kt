package com.dadm.artisticall.gamemodes

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.dadm.artisticall.drawing.DrawingView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun GameNormalScreen(
    navController: NavController,
    lobbyCode: String,
    username: String
) {
    val context = LocalContext.current
    val drawingView = remember { DrawingView(context) }
    val colors = listOf(
        Color(AndroidColor.RED),
        Color(AndroidColor.GREEN),
        Color(AndroidColor.BLUE),
        Color(AndroidColor.YELLOW),
        Color(AndroidColor.CYAN),
        Color(AndroidColor.MAGENTA),
        Color(AndroidColor.BLACK),
        Color(0xFF6A4E23)
    )
    val defaultLineSize = 10f
    var currentColor by remember { mutableStateOf(Color.Black) }
    var currentLineSize by remember { mutableStateOf(defaultLineSize) }

    // Estado para el temporizador
    var timeLeft by remember { mutableStateOf(50) }
    var isTimerRunning by remember { mutableStateOf(true) }

    // Estado para la frase seleccionada
    var selectedPhrase by remember { mutableStateOf<Phrase?>(null) }

    // Obtener una frase aleatoria cuando se inicia la pantalla
    LaunchedEffect(Unit) {
        selectedPhrase = try {
            getRandomPhrase(lobbyCode, username)
        } catch (e: Exception) {
            Log.e("GameNormalScreen", "Error al obtener la frase: ${e.message}")
            null // Si no hay frases disponibles, selectedPhrase será null
        }
    }

    // Función para guardar la imagen en Firestore
    fun saveImageToFirestore(bitmap: Bitmap) {
        if (selectedPhrase == null) return // Si no hay frase seleccionada, no hacer nada

        val file = File(context.cacheDir, "drawing_image.png")
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Convertir la imagen a bytes
            val imageBytes = file.readBytes().map { it.toInt() }

            // Guardar la imagen en Firestore
            val imageData = hashMapOf(
                "authorIds" to (selectedPhrase!!.authorIds + username), // Copia de authorIds + dibujante
                "guessed" to false, // Inicialmente no adivinada
                "image" to imageBytes, // Imagen en formato binario
                "assigned" to false // Inicialmente no asignada
            )

            Firebase.firestore.collection("lobbies")
                .document(lobbyCode)
                .collection("drawings")
                .add(imageData)
                .addOnSuccessListener {
                    // Actualizar la frase a "drawed: true" y "assigned: true"
                    Firebase.firestore.collection("lobbies")
                        .document(lobbyCode)
                        .collection("phrases")
                        .document(selectedPhrase!!.id) // Usar el ID de la frase
                        .update(
                            "drawed", true,
                            "assigned", true
                        )
                        .addOnSuccessListener {
                            Log.d("GameNormalScreen", "Frase actualizada a drawed: true y assigned: true")
                        }
                        .addOnFailureListener { e ->
                            Log.e("GameNormalScreen", "Error al actualizar la frase: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("GameNormalScreen", "Error al guardar la imagen: ${e.message}")
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Función para guardar la imagen y redirigir
    fun saveImage() {
        val bitmap = drawingView.getBitmap()
        saveImageToFirestore(bitmap)
        navController.navigate("points_screen")
    }

    // Iniciar el temporizador
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
        }
        if (timeLeft == 0) {
            saveImage()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Mostrar la frase seleccionada
        Text(
            text = "Frase: ${selectedPhrase?.text ?: "No hay frases disponibles"}",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E2D36))
                .padding(8.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        AndroidView(
            factory = { drawingView },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )

        // Resto del código (colores, grosor de línea, borrador, etc.)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E2D36))
                .padding(8.dp)
        ) {
            Text(
                text = "Selecciona un color",
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color = color, shape = RoundedCornerShape(8.dp))
                            .clickable {
                                currentColor = color
                                drawingView.setColor(color.toArgb())
                            }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Grosor de la línea",
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        currentLineSize = 5f
                        drawingView.setLineSize(5f)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFA040)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
                ) {
                    Text("Fina")
                }
                Button(
                    onClick = {
                        currentLineSize = 20f
                        drawingView.setLineSize(20f)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFA040)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
                ) {
                    Text("Gruesa")
                }
                Button(
                    onClick = {
                        currentLineSize = defaultLineSize
                        drawingView.setLineSize(defaultLineSize)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFA040)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
                ) {
                    Text("Restaurar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tamaño del borrador",
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        drawingView.setEraserSize(15f)
                        drawingView.activateEraser()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFA040)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
                ) {
                    Text("Pequeño")
                }
                Button(
                    onClick = {
                        drawingView.setEraserSize(30f)
                        drawingView.activateEraser()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFA040)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
                ) {
                    Text("Grande")
                }
                Button(
                    onClick = {
                        drawingView.clearCanvas()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFA040)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
                ) {
                    Text("Borrar Pizarra")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tiempo restante: $timeLeft segundos",
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Función para obtener una frase aleatoria
suspend fun getRandomPhrase(lobbyCode: String, username: String): Phrase {
    var retries = 3 // Número de reintentos
    while (retries > 0) {
        try {
            val phrases = Firebase.firestore.collection("lobbies")
                .document(lobbyCode)
                .collection("phrases")
                .whereEqualTo("drawed", false)
                .whereEqualTo("assigned", false) // Solo frases no asignadas
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    val phrase = document.toObject(Phrase::class.java)
                    phrase?.copy(id = document.id) // Asignar el ID del documento
                }
                .filter { !it.authorIds.contains(username) } // Filtrar frases no escritas por el jugador

            if (phrases.isNotEmpty()) {
                // Seleccionar una frase aleatoria
                val randomPhrase = phrases.random()

                // Marcar la frase como asignada
                Firebase.firestore.collection("lobbies")
                    .document(lobbyCode)
                    .collection("phrases")
                    .document(randomPhrase.id) // Usar el ID de la frase
                    .update("assigned", true)
                    .await()

                return randomPhrase
            }
        } catch (e: Exception) {
            Log.e("getRandomPhrase", "Error al obtener frases: ${e.message}")
        }

        // Esperar un momento antes de reintentar
        delay(1000)
        retries--
    }

    throw IllegalStateException("No hay frases disponibles para asignar después de varios intentos.")
}

// Modelo de datos para la frase
data class Phrase(
    val id: String = "",
    val text: String = "",
    val authorIds: List<String> = emptyList(),
    val drawed: Boolean = false,
    val assigned: Boolean = false, // Nuevo campo para marcar si la frase está asignada
    val lobbyCode: String = ""
)




