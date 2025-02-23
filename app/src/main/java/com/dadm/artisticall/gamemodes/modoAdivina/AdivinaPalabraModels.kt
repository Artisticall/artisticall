package com.dadm.artisticall.gamemodes.modoAdivina

data class AdivinaPalabraGame(
    val gameId: String = "",
    val players: List<PlayerGame> = listOf(),
    val currentRound: Int = 1,
    val currentSubRound: Int = 1,
    val totalRounds: Int = 3,
    val currentDrawer: String = "",
    val currentWord: String = "",
    val gameState: GameState = GameState.WAITING,
    val timeLeft: Long = 60,
    val scores: Map<String, Int> = mapOf(),
    val playersDrew: List<String> = listOf()
)

data class PlayerGame(
    val username: String = "",
    val isDrawing: Boolean = false,
    val hasGuessed: Boolean = false,
    val score: Int = 0
)

enum class GameState {
    WAITING,      // Esperando que todos los jugadores estén listos
    DRAWING,      // Jugador dibujando
    ROUND_END,    // Fin de la subronda
    GAME_END      // Fin del juego
}

data class WordGuess(
    val player: String,
    val guess: String,
    val timestamp: Long
)

sealed class GameEvent {
    object StartGame : GameEvent()
    data class WordSelected(val word: String) : GameEvent()
    data class PlayerGuess(val player: String, val guess: String) : GameEvent()
    data class DrawingUpdated(val paths: List<DrawPath>) : GameEvent()
    object TimeUp : GameEvent()
    object NextRound : GameEvent()
}

data class DrawPath(
    val points: List<Point>,
    val color: Int,
    val strokeWidth: Float
)

data class Point(
    val x: Float,
    val y: Float
)