package com.dadm.artisticall.gamemodes.modoDesafio

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.absoluteValue

@Composable
fun GuessScreenDesafio(
    navController: NavController,
    lobbyCode: String,
    username: String
) {
    // Estados
    var selectedImage by remember { mutableStateOf<Drawing?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var guess by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(30) } // Cambiado a 30 segundos
    var isTimerRunning by remember { mutableStateOf(true) }

    // Obtener imagen aleatoria y convertirla
    LaunchedEffect(Unit) {
        var retries = 3 // Número de reintentos
        while (retries > 0) {
            try {
                selectedImage = getRandomDrawing(lobbyCode, username)
                selectedImage?.let {
                    imageBitmap = convertImageListToBitmap(it.image)
                }
                break // Salir del bucle si se obtiene la imagen
            } catch (e: Exception) {
                Log.e("GuessScreen", "Error al obtener la imagen: ${e.message}")
                retries--
                if (retries > 0) {
                    delay(1000) // Esperar 1 segundo antes de reintentar
                } else {
                    selectedImage = null
                }
            }
        }
    }

    // Guardar adivinanza y actualizar dibujo
    suspend fun saveGuess(): String? { // Retorna el ID de la frase guardada
        selectedImage?.let { drawing ->
            val guessData = hashMapOf(
                "text" to guess,
                "authorIds" to drawing.authorIds + username,
                "drawed" to false,
                "assigned" to false,
                "lobbyCode" to lobbyCode
            )

            return try {
                // Guardar la adivinanza en phrases y obtener el ID del documento
                val phraseRef = Firebase.firestore.collection("lobbies")
                    .document(lobbyCode)
                    .collection("phrases")
                    .add(guessData)
                    .await()

                // Actualizar el dibujo a guessed = true
                Firebase.firestore.collection("lobbies")
                    .document(lobbyCode)
                    .collection("drawings")
                    .document(drawing.id)
                    .update("guessed", true)
                    .await()

                Log.d("GuessScreen", "Adivinanza guardada y dibujo actualizado.")
                phraseRef.id // Retornar el ID de la frase guardada
            } catch (e: Exception) {
                Log.e("GuessScreen", "Error al guardar la adivinanza: ${e.message}")
                null
            }
        }
        return null
    }

    // Redirigir a la pantalla de puntos o a la pantalla normal del juego
    suspend fun navigateToPointsScreen() {
        val phraseId = saveGuess() // Guardar la adivinanza y obtener el ID de la frase

        if (phraseId == null) {
            Log.e("GuessScreen", "No se pudo obtener el ID de la frase")
            navController.navigate("game_desafio_screen/$lobbyCode/$username")
            return
        }

        // Obtener la lista de usernames del documento del juego
        val gameRef = Firebase.firestore.collection("games")
            .document(lobbyCode)

        try {
            val gameSnapshot = gameRef.get().await()
            val usernames = gameSnapshot.get("usernames") as? List<String> ?: emptyList()
            Log.d("GuessScreen", "Usernames: $usernames")

            // Obtener los authorIds de la frase recién guardada usando el ID
            val phraseRef = Firebase.firestore.collection("lobbies")
                .document(lobbyCode)
                .collection("phrases")
                .document(phraseId)

            val phraseSnapshot = phraseRef.get().await()
            val authorIds = phraseSnapshot.get("authorIds") as? List<String> ?: emptyList()
            Log.d("GuessScreen", "AuthorIds de la frase: $authorIds")

            // Comparar las dos listas
            if (usernames.toSet() == authorIds.toSet()) {
                Log.d("GuessScreen", "Redirigiendo a points_screen")
                navController.navigate("final_sequence_screen/$lobbyCode/$username")
            } else {
                Log.d("GuessScreen", "Redirigiendo a game_normal_screen")
                navController.navigate("game_desafio_screen/$lobbyCode/$username")
            }
        } catch (e: Exception) {
            Log.e("GuessScreen", "Error al obtener datos: ${e.message}")
            navController.navigate("game_desafio_screen/$lobbyCode/$username")
        }
    }

    // Temporizador
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
        }
        if (timeLeft == 0) {
            navigateToPointsScreen()
        }
    }

    // Diseño de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2D36))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mostrar la imagen seleccionada
        imageBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Dibujo a adivinar",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        } ?: Text(
            text = "No hay imágenes disponibles",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto para la adivinanza
        BasicTextField(
            value = guess,
            onValueChange = { guess = it },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E2D36))
                .padding(8.dp),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (guess.isEmpty()) {
                    Text(text = "Escribe tu adivinanza...", color = Color.White)
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar el tiempo restante
        Text(
            text = "Tiempo restante: $timeLeft segundos",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Obtener una imagen aleatoria desde Firestore
suspend fun getRandomDrawing(lobbyCode: String, username: String): Drawing {
    val firestore = Firebase.firestore
    val drawingsRef = firestore.collection("lobbies")
        .document(lobbyCode)
        .collection("drawings")

    var retries = 5
    while (retries > 0) {
        try {
            val drawingsSnapshot = withTimeoutOrNull(5000) {
                drawingsRef
                    .whereEqualTo("assigned", false)
                    .whereEqualTo("guessed", false)
                    .get()
                    .await()
            } ?: throw IllegalStateException("La consulta tardó demasiado.")

            val drawings = drawingsSnapshot.documents
                .mapNotNull { document ->
                    val drawing = document.toObject(Drawing::class.java)
                    drawing?.copy(id = document.id)
                }
                .filter { !it.authorIds.contains(username) }
                .sortedBy { it.id }

            if (drawings.isEmpty()) throw IllegalStateException("No hay dibujos disponibles para adivinar.")

            // Calcular índice basado en el hash del nombre de usuario
            val index = username.hashCode().absoluteValue % drawings.size
            val selectedDrawing = drawings[index]

            return firestore.runTransaction { transaction ->
                val drawingDoc = drawingsRef.document(selectedDrawing.id)
                val drawingSnapshot = transaction.get(drawingDoc)

                if (drawingSnapshot.getBoolean("assigned") == true) {
                    throw IllegalStateException("El dibujo ya fue asignado por otro dispositivo.")
                }

                transaction.update(drawingDoc, "assigned", true)
                selectedDrawing
            }.await()

        } catch (e: Exception) {
            Log.e("getDeterministicDrawing", "Error al obtener dibujo: ${e.message}")
            retries--
            if (retries > 0) delay((3 - retries) * 1000L)
        }
    }

    throw IllegalStateException("No hay imágenes disponibles para adivinar después de varios intentos.")
}

// Convertir lista de enteros a Bitmap
fun convertImageListToBitmap(imageList: List<Int>): Bitmap? {
    return try {
        val byteArray = imageList.map { it.toByte() }.toByteArray()
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    } catch (e: Exception) {
        Log.e("GuessScreen", "Error al convertir la imagen: ${e.message}")
        null
    }
}

// Modelo Drawing
data class Drawing(
    val id: String = "",
    val image: List<Int> = emptyList(),
    val authorIds: List<String> = emptyList(),
    val guessed: Boolean = false,
    val assigned: Boolean = false
)
