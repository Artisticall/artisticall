package com.dadm.artisticall.gamemodes

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.absoluteValue


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
