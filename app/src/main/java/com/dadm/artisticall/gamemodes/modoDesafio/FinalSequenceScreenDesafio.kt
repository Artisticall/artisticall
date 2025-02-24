package com.dadm.artisticall.gamemodes.modoDesafio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import com.dadm.artisticall.gamemodes.Phrase

@Composable
fun FinalSequenceScreenDesafio(
    navController: NavController,
    lobbyCode: String,
    username: String
) {
    var sequence by remember { mutableStateOf<List<Any>>(emptyList()) }

    // Obtener la secuencia al iniciar la pantalla
    LaunchedEffect(Unit) {
        val (phrases, drawings) = getAllPhrasesAndDrawings(lobbyCode)
        sequence = buildSequence(phrases, drawings)
    }

    // Mostrar la secuencia con scroll
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2D36))
            .padding(top = 32.dp) // Margen superior para evitar solapamiento
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()) // Hacer el contenido desplazable
        ) {
            var isFirstItem = true
            sequence.forEach { item ->
                if (!isFirstItem) {
                    // Línea de separación entre secuencias
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color(0xFFFFA040))
                            .padding(vertical = 8.dp)
                    )
                }
                isFirstItem = false

                when (item) {
                    is Phrase -> {
                        Text(
                            text = "Frase: ${item.text}",
                            color = Color(0xFFFFA040),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    is Drawing -> {
                        val bitmap = convertImageListToBitmap(item.image)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Dibujo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(bottom = 16.dp)
                            )
                        } else {
                            Text(
                                text = "Error al cargar el dibujo",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Botón para volver al lobby
        Button(
            onClick = {
                // Navegar de regreso al lobby
                navController.navigate("lobby_screen/$lobbyCode/$username")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFFFFA040)
            ),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
        ) {
            Text(text = "Volver al Lobby")
        }
    }
}

// Obtener todas las frases y dibujos
suspend fun getAllPhrasesAndDrawings(lobbyCode: String): Pair<List<Phrase>, List<Drawing>> {
    val firestore = Firebase.firestore

    // Obtener todas las frases
    val phrases = firestore.collection("lobbies")
        .document(lobbyCode)
        .collection("phrases")
        .get()
        .await()
        .documents
        .mapNotNull { it.toObject(Phrase::class.java)?.copy(id = it.id) }

    // Obtener todos los dibujos
    val drawings = firestore.collection("lobbies")
        .document(lobbyCode)
        .collection("drawings")
        .get()
        .await()
        .documents
        .mapNotNull { it.toObject(Drawing::class.java)?.copy(id = it.id) }

    return Pair(phrases, drawings)
}

// Reconstruir la secuencia de frases y dibujos
fun buildSequence(phrases: List<Phrase>, drawings: List<Drawing>): List<Any> {
    val sequence = mutableListOf<Any>()
    val usedPhrases = mutableSetOf<String>()
    val usedDrawings = mutableSetOf<String>()

    // Empezar con frases que tienen un solo autor
    val initialPhrases = phrases.filter { it.authorIds.size == 1 && !usedPhrases.contains(it.id) }

    for (initialPhrase in initialPhrases) {
        sequence.add(initialPhrase)
        usedPhrases.add(initialPhrase.id)

        var currentAuthors = initialPhrase.authorIds
        var nextType = "drawing" // Alternar entre frase y dibujo

        while (true) {
            when (nextType) {
                "drawing" -> {
                    // Buscar un dibujo que comience con los autores actuales y tenga un autor adicional
                    val nextDrawing = drawings.find { drawing ->
                        drawing.authorIds.take(currentAuthors.size) == currentAuthors &&
                                drawing.authorIds.size == currentAuthors.size + 1 &&
                                !usedDrawings.contains(drawing.id)
                    }

                    if (nextDrawing != null) {
                        sequence.add(nextDrawing)
                        usedDrawings.add(nextDrawing.id)
                        currentAuthors = nextDrawing.authorIds
                        nextType = "phrase"
                    } else {
                        break // No hay más dibujos que coincidan
                    }
                }
                "phrase" -> {
                    // Buscar una frase que comience con los autores actuales y tenga un autor adicional
                    val nextPhrase = phrases.find { phrase ->
                        phrase.authorIds.take(currentAuthors.size) == currentAuthors &&
                                phrase.authorIds.size == currentAuthors.size + 1 &&
                                !usedPhrases.contains(phrase.id)
                    }

                    if (nextPhrase != null) {
                        sequence.add(nextPhrase)
                        usedPhrases.add(nextPhrase.id)
                        currentAuthors = nextPhrase.authorIds
                        nextType = "drawing"
                    } else {
                        // Si no hay una frase que coincida, buscar una nueva frase de longitud 1
                        val newInitialPhrase = phrases.find { phrase ->
                            phrase.authorIds.size == 1 &&
                                    !usedPhrases.contains(phrase.id)
                        }

                        if (newInitialPhrase != null) {
                            sequence.add(newInitialPhrase)
                            usedPhrases.add(newInitialPhrase.id)
                            currentAuthors = newInitialPhrase.authorIds
                            nextType = "drawing"
                        } else {
                            break // No hay más frases de longitud 1
                        }
                    }
                }
            }
        }
    }

    return sequence
}

