package com.dadm.artisticall.gamemodes.modoAdivina

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dadm.artisticall.ui.theme.AppTypography
import com.dadm.artisticall.ui.theme.backgroundDark
import com.dadm.artisticall.ui.theme.primaryContainerDark
import com.dadm.artisticall.ui.theme.primaryDark
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun GameAdivinaScreen(
    navController: NavController,
    lobbyCode: String,
    username: String,
) {
    val context = LocalContext.current
    var gameState by remember { mutableStateOf<AdivinaPalabraGame?>(null) }
    var isDrawing by remember { mutableStateOf(false) }
    var currentWord by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(60L) }
    var showCorrectWordMessage by remember { mutableStateOf(false) }
    var correctWord by remember { mutableStateOf("") }

    LaunchedEffect(lobbyCode) {
        val gameRef = FirebaseFirestore.getInstance()
            .collection("games")
            .document(lobbyCode)

        val words = loadWordsFromAssets(context)

        val game = AdivinaPalabraGame(
            gameId = lobbyCode,
            players = listOf(PlayerGame(username = username)),
            currentRound = 1,
            currentSubRound = 1,
            totalRounds = 3,
            currentDrawer = username,
            currentWord = getRandomWord(words),
            gameState = GameState.WAITING,
            timeLeft = 60L,
            scores = mapOf(username to 0),
            playersDrew = listOf()
        )

        gameRef.set(game)
            .addOnSuccessListener {
                println("El documento fue creado correctamente.")
            }
            .addOnFailureListener { e ->
                println("Error al crear el documento: $e")
            }

        gameRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            snapshot?.toObject(AdivinaPalabraGame::class.java)?.let { game ->
                gameState = game
                isDrawing = game.currentDrawer == username
                if (isDrawing) {
                    currentWord = game.currentWord
                }
                timeLeft = game.timeLeft
            }
        }
    }

    LaunchedEffect(gameState) {
        when (gameState?.gameState) {
            GameState.WAITING -> {
                delay(5000)
                changeGameState(lobbyCode, GameState.DRAWING)
            }
            GameState.DRAWING -> {
                if (timeLeft > 0L) {
                    delay(1000)
                    timeLeft -= 1
                    updateTimeLeft(lobbyCode, timeLeft)
                } else {
                    showCorrectWordMessage = true
                    correctWord = gameState?.currentWord ?: ""

                    changeGameState(lobbyCode, GameState.WAITING)

                    delay(5000)

                    if (gameState?.playersDrew?.size == gameState?.players?.size) {
                        changeGameState(lobbyCode, GameState.ROUND_END)
                    } else {
                        changeDrawer(lobbyCode, context)
                    }

                    showCorrectWordMessage = false
                }
            }
            GameState.ROUND_END -> {
                if (gameState?.currentRound ?: 0 < gameState?.totalRounds ?: 3) {
                    changeGameState(lobbyCode, GameState.DRAWING)
                } else {
                    changeGameState(lobbyCode, GameState.GAME_END)
                }
            }
            GameState.GAME_END -> {
                // El juego terminó, manejamos el fin del juego
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
            .padding(16.dp)
    ) {
        GameTopBar(
            round = gameState?.currentRound ?: 1,
            timeLeft = timeLeft,
            currentWord = if (isDrawing) currentWord else "_ ".repeat(currentWord.length)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
        ) {
            if (showCorrectWordMessage) {
                Text(
                    text = "¡Palabra Correcta!\n$correctWord",
                    style = AppTypography.titleLarge.copy(fontSize = 32.sp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = primaryDark
                )
            } else {
                DrawingCanvas(
                    isDrawingEnabled = isDrawing,
                    lobbyCode = lobbyCode
                ) { }
            }
        }

        GameBottomPanel(
            players = gameState?.players ?: emptyList(),
            isDrawing = isDrawing,
            onGuessSubmitted = { guess ->
                submitGuess(guess, username, lobbyCode)
            }
        )
    }
}

fun updateTimeLeft(lobbyCode: String, timeLeft: Long) {
    val gameRef = FirebaseFirestore.getInstance()
        .collection("games")
        .document(lobbyCode)

    gameRef.update("timeLeft", timeLeft)
        .addOnSuccessListener {
            println("Tiempo actualizado: $timeLeft")
        }
        .addOnFailureListener { e ->
            println("Error al actualizar el tiempo restante: $e")
        }
}


fun changeGameState(lobbyCode: String, newState: GameState, currentDrawer: String? = null) {
    val gameRef = FirebaseFirestore.getInstance()
        .collection("games")
        .document(lobbyCode)

    gameRef.get().addOnSuccessListener { document ->
        val game = document.toObject(AdivinaPalabraGame::class.java)
        if (game != null) {
            if (newState == GameState.ROUND_END && currentDrawer != null && !game.playersDrew.contains(currentDrawer)) {
                val updatedPlayersDrew = game.playersDrew.toMutableList()
                updatedPlayersDrew.add(currentDrawer)
                gameRef.update("playersDrew", updatedPlayersDrew)
            }

            gameRef.update("gameState", newState)
                .addOnSuccessListener {
                    println("Estado del juego actualizado a: $newState")
                }
                .addOnFailureListener { e ->
                    println("Error al actualizar el estado del juego: $e")
                }
        }
    }
}


fun changeDrawer(lobbyCode: String, context: Context) {
    val gameRef = FirebaseFirestore.getInstance()
        .collection("games")
        .document(lobbyCode)

    gameRef.get().addOnSuccessListener { document ->
        val game = document.toObject(AdivinaPalabraGame::class.java)
        if (game != null) {
            val nextDrawer = getNextDrawer(game)
            gameRef.update("currentDrawer", nextDrawer)

            val updatedPlayersDrew = game.playersDrew.toMutableSet()
            updatedPlayersDrew.add(nextDrawer)
            gameRef.update("playersDrew", updatedPlayersDrew)

            val words = loadWordsFromAssets(context)
            gameRef.update("currentWord", getRandomWord(words))
        }
    }
}

fun getNextDrawer(game: AdivinaPalabraGame): String {
    val currentDrawerIndex = game.players.indexOfFirst { it.username == game.currentDrawer }
    val nextDrawerIndex = (currentDrawerIndex + 1) % game.players.size
    return game.players[nextDrawerIndex].username
}

fun submitGuess(guess: String, playerName: String, lobbyCode: String) {
    val gameRef = FirebaseFirestore.getInstance()
        .collection("games")
        .document(lobbyCode)

    gameRef.get().addOnSuccessListener { document ->
        val game = document.toObject(AdivinaPalabraGame::class.java)
        if (game != null) {
            if (guess.equals(game.currentWord, ignoreCase = true)) {
                val updatedScores = game.scores.toMutableMap()
                val currentScore = updatedScores[playerName] ?: 0
                updatedScores[playerName] = currentScore + 10

                gameRef.update("scores", updatedScores)
                val updatedPlayers = game.players.map {
                    if (it.username == playerName) {
                        it.copy(hasGuessed = true)
                    } else {
                        it
                    }
                }

                gameRef.update("players", updatedPlayers)

                val allGuessed = updatedPlayers.filter { !it.isDrawing }.all { it.hasGuessed }
                if (allGuessed) {
                    changeGameState(lobbyCode, GameState.ROUND_END)
                }
            }
        }
    }
}


@Composable
fun GameTopBar(
    round: Int,
    timeLeft: Long,
    currentWord: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ronda $round/3",
            style = AppTypography.titleMedium,
            color = primaryDark
        )

        Text(
            text = currentWord,
            style = AppTypography.titleLarge,
            color = primaryDark
        )

        Text(
            text = "$timeLeft s",
            style = AppTypography.titleMedium,
            color = primaryDark
        )
    }
}

@Composable
fun GameBottomPanel(
    players: List<PlayerGame>,
    isDrawing: Boolean,
    onGuessSubmitted: (String) -> Unit
) {
    var guessInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryContainerDark, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            players.forEach { player ->
                PlayerScore(player)
            }
        }

        if (!isDrawing) {
            OutlinedTextField(
                value = guessInput,
                onValueChange = { guessInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Escribe tu respuesta...") },
                trailingIcon = {
                    IconButton(onClick = {
                        if (guessInput.isNotBlank()) {
                            onGuessSubmitted(guessInput)
                            guessInput = ""
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar respuesta"
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun PlayerScore(player: PlayerGame) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = player.username,
            style = AppTypography.bodyMedium,
            color = primaryDark
        )
        Text(
            text = "${player.score} pts",
            style = AppTypography.bodySmall,
            color = primaryDark.copy(alpha = 0.7f)
        )
    }
}
