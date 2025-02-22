package com.dadm.artisticall.gamemodes.modoNormal

import android.graphics.Bitmap
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
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.absoluteValue

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
// Función para guardar la imagen en Firestore
    fun saveImageToFirestore(bitmap: Bitmap, onSuccess: (String) -> Unit) {
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
                .addOnSuccessListener { documentReference ->
                    // Obtener el ID del documento recién creado
                    val drawingId = documentReference.id
                    Log.d("GameNormalScreen", "Dibujo guardado con ID: $drawingId")

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
                            // Llamar a onSuccess con el ID del dibujo
                            onSuccess(drawingId)
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

        // Guardar la imagen y obtener el ID del dibujo recién guardado
        saveImageToFirestore(bitmap) { drawingId ->
            // Obtener la lista de usernames del documento del juego
            val gameRef = Firebase.firestore.collection("games")
                .document(lobbyCode)

            gameRef.get().addOnSuccessListener { gameSnapshot ->
                val usernames = gameSnapshot.get("usernames") as? List<String> ?: emptyList()
                Log.d("GameNormalScreen", "Usernames: $usernames")

                // Obtener los authorIds del dibujo recién guardado
                val drawingRef = Firebase.firestore.collection("lobbies")
                    .document(lobbyCode)
                    .collection("drawings")
                    .document(drawingId)

                drawingRef.get().addOnSuccessListener { drawingSnapshot ->
                    val authorIds = drawingSnapshot.get("authorIds") as? List<String> ?: emptyList()
                    Log.d("GameNormalScreen", "AuthorIds del dibujo: $authorIds")

                    // Comparar las dos listas
                    if (usernames.toSet() == authorIds.toSet()) {
                        Log.d("GameNormalScreen", "Redirigiendo a pointscreen")
                        navController.navigate("final_sequence_screen/$lobbyCode/$username")
                    } else {
                        Log.d("GameNormalScreen", "Redirigiendo a guessscreen")
                        navController.navigate("guess_screen/$lobbyCode/$username")
                    }
                }.addOnFailureListener { e ->
                    Log.e("GameNormalScreen", "Error al obtener authorIds del dibujo: ${e.message}")
                    navController.navigate("guess_screen/$lobbyCode/$username")
                }
            }.addOnFailureListener { e ->
                Log.e("GameNormalScreen", "Error al obtener usernames: ${e.message}")
                navController.navigate("guess_screen/$lobbyCode/$username")
            }
        }
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
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp) // Margen superior para evitar solapamiento
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
    val firestore = Firebase.firestore
    val phrasesRef = firestore.collection("lobbies")
        .document(lobbyCode)
        .collection("phrases")

    var retries = 5
    while (retries > 0) {
        try {
            val phrasesSnapshot = withTimeoutOrNull(5000) {
                phrasesRef
                    .whereEqualTo("drawed", false)
                    .whereEqualTo("assigned", false)
                    .get()
                    .await()
            } ?: throw IllegalStateException("La consulta tardó demasiado.")

            val phrases = phrasesSnapshot.documents
                .mapNotNull { document ->
                    val phrase = document.toObject(Phrase::class.java)
                    phrase?.copy(id = document.id)
                }
                .filter { !it.authorIds.contains(username) }
                .sortedBy { it.id }

            if (phrases.isEmpty()) throw IllegalStateException("No hay frases disponibles para asignar.")

            // Calcular índice basado en el hash del nombre de usuario
            val index = username.hashCode().absoluteValue % phrases.size
            val selectedPhrase = phrases[index]

            return firestore.runTransaction { transaction ->
                val phraseDoc = phrasesRef.document(selectedPhrase.id)
                val phraseSnapshot = transaction.get(phraseDoc)

                if (phraseSnapshot.getBoolean("assigned") == true) {
                    throw IllegalStateException("La frase ya fue asignada por otro dispositivo.")
                }

                transaction.update(phraseDoc, "assigned", true)
                selectedPhrase
            }.await()

        } catch (e: Exception) {
            Log.e("getDeterministicPhrase", "Error al obtener frase: ${e.message}")
            retries--
            if (retries > 0) delay((5 - retries) * 1000L)
        }
    }

    throw IllegalStateException("No hay frases disponibles después de varios intentos.")
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




