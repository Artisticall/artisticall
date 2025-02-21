package com.dadm.artisticall.gamemodes

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dadm.artisticall.ui.theme.AppTypography
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay

@Composable
fun WritePhraseScreen(
    navController: NavController,
    lobbyCode: String,
    username: String
) {
    var phrase by remember { mutableStateOf("") }
    var remainingTime by remember { mutableStateOf(15) } // Contador de 15 segundos
    val context = LocalContext.current

    // Efecto para manejar el contador
    LaunchedEffect(remainingTime) {
        if (remainingTime > 0) {
            delay(1000) // Espera 1 segundo
            remainingTime-- // Disminuye el tiempo restante
        } else {
            // Guardar la frase automáticamente cuando el tiempo llegue a 0
            if (phrase.isNotBlank()) {
                savePhrase(lobbyCode, username, phrase, { phraseId ->
                    Toast.makeText(context, "Frase guardada automáticamente", Toast.LENGTH_SHORT).show()
                    // Redirige a la pantalla game_normal_screen después de guardar la frase
                    navController.navigate("game_normal_screen/$lobbyCode/$username")
                }, { error ->
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                    // Redirige incluso si hay un error (opcional)
                    navController.navigate("game_normal_screen/$lobbyCode/$username")
                })
            } else {
                // Si no hay frase, redirige de todos modos
                navController.navigate("game_normal_screen/$lobbyCode/$username")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mostrar el tiempo restante
        Text(
            text = "Tiempo restante: $remainingTime segundos",
            style = AppTypography.bodyMedium.copy(fontSize = 20.sp),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Campo de texto para escribir la frase
        TextField(
            value = phrase,
            onValueChange = { phrase = it },
            label = { Text("Escribe una frase para dibujar") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun savePhrase(
    lobbyCode: String,
    username: String,
    phrase: String,
    onSuccess: (String) -> Unit, // Ahora recibe el ID de la frase
    onFailure: (String) -> Unit
) {
    // Crear el objeto con los datos de la frase
    val phraseData = hashMapOf(
        "text" to phrase,
        "authorIds" to listOf(username), // Guardar el username en lugar del userId
        "drawed" to false,
        "assigned" to false, // Nuevo campo para marcar si la frase está asignada
        "lobbyCode" to lobbyCode
    )

    // Guardar la frase en la subcolección "phrases" del documento del lobby
    Firebase.firestore.collection("lobbies")
        .document(lobbyCode) // Acceder al documento del lobby
        .collection("phrases") // Acceder a la subcolección "phrases"
        .add(phraseData) // Añadir la frase
        .addOnSuccessListener { documentReference ->
            // Obtener el ID de la frase recién creada
            val phraseId = documentReference.id
            onSuccess(phraseId) // Pasar el ID de la frase al callback
        }
        .addOnFailureListener { e ->
            onFailure(e.message ?: "Error desconocido")
        }
}