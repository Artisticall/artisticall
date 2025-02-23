package com.dadm.artisticall.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.dadm.artisticall.ui.theme.AppTypography
import com.dadm.artisticall.ui.theme.onPrimaryDark
import com.dadm.artisticall.ui.theme.primaryContainerDark
import com.dadm.artisticall.ui.theme.primaryDark
import com.dadm.artisticall.ui.theme.secondaryDark

@Composable
fun GamesList(
    selectedGameMode: GameMode?,
    onGameModeSelected: (GameMode?) -> Unit
) {

    val gameModes = listOf(
        GameMode(
            "Normal",
            "Juego clásico",
            "Alterna entre escribir frases y dibujarlas. Descubre cómo evolucionan las ideas de todos en una secuencia divertida y colaborativa.",
            "write_phrase_screen/{lobbyCode}/{username}",
            "https://cdn-icons-png.freepik.com/512/3964/3964965.png?ga=GA1.1.1882402121.1739594082"),
        GameMode(
            "Solo",
            "Juega en solitario",
            "Disfruta de un espacio tranquilo para dibujar sin competencia. Perfecto para practicar o relajarte creando.",
            "game_solo_screen",
            "https://cdn-icons-png.freepik.com/512/2491/2491519.png?ga=GA1.1.1882402121.1739594082")        ,
        GameMode(
            "Adivina la Palabra",
            "Adivina la palabra oculta",
            "Adivina la palabra que otro jugador dibuja. ¡Gana puntos por velocidad y precisión!",
            "game_adivina_screen/{lobbyCode}/{username}",
            "https://cdn-icons-png.freepik.com/512/17059/17059832.png?ga=GA1.1.1882402121.1739594082"),
        GameMode(
            "¿Qué es esto?",
            "Intenta adivinar el objeto a partir de pistas",
            "Continúa el dibujo de otro jugador con una parte oculta. ¡Sorpréndete con los resultados inesperados!",
            "game_que_es_esto_screen",
            "https://cdn-icons-png.freepik.com/512/5579/5579510.png?ga=GA1.1.1882402121.1739594082"),
        GameMode(
            "Batalla de Pinceles",
            "Compite dibujando",
            "Compite en rondas de dibujo basadas en un tema. Vota por el mejor dibujo y acumula puntos para ganar.",
            "game_batalla_pinceles_screen",
            "https://cdn-icons-png.freepik.com/512/1313/1313485.png?ga=GA1.1.1882402121.1739594082"),
        GameMode(
            "Ponle Título",
            "Ponle título a una imagen",
            "Crea el título más ingenioso para un dibujo. ¡Humor y creatividad son la clave para ganar!",
            "game_ponle_titulo_screen",
            "https://cdn-icons-png.freepik.com/512/16606/16606642.png?ga=GA1.1.1882402121.1739594082"),
        GameMode(
            "Érase una vez",
            "Crea historias de manera colaborativa",
            "Construye una historia visual con otros jugadores. Cada dibujo añade un nuevo capítulo a la narrativa.",
            "game_erase_una_vez_screen",
            "https://cdn-icons-png.freepik.com/512/8013/8013787.png?ga=GA1.1.1882402121.1739594082"),
        GameMode(
            "Modo Libre",
            "Dibuja lo que quieras",
            "Dibuja lo que quieras sin restricciones. Elige un tema o déjalo al azar para una experiencia relajada.",
            "game_libre_screen",
            "https://cdn-icons-png.freepik.com/512/12141/12141689.png?ga=GA1.1.1882402121.1739594082"),
        GameMode(
            "Ojo de Águila",
            "Adivina el objeto a partir de un pequeño detalle",
            "Reproduce una imagen con la mayor precisión posible. ¡Enfócate en los detalles para ganar!",
            "game_ojo_de_aguila_screen",
            "https://cdn-icons-png.freepik.com/512/983/983890.png?ga=GA1.1.1882402121.1739594082"),
        GameMode(
            "Modo Colaborativo",
            "Trabaja en equipo para lograr un objetivo",
            "Trabaja en equipo para crear una obra de arte colectiva. Cada jugador aporta su estilo único.",
            "game_colaborativo_screen",
            "https://cdn-icons-png.freepik.com/512/11399/11399186.png?ga=GA1.1.1882402121.1739594082"),
        GameMode(
            "Modo Desafío",
            "Desafíos rápidos para ganar puntos",
            "Supera retos como dibujar con tiempo limitado o usando solo ciertos colores. ¡Demuestra tu creatividad bajo presión!",
            "game_desafio_screen",
            "https://cdn-icons-png.freepik.com/512/12476/12476761.png?ga=GA1.1.1882402121.1739594082")
    )

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
    ) {
        items(gameModes) { mode ->
            GameModeCard(mode, selectedGameMode) {
                onGameModeSelected(mode)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun GameModeCard(mode: GameMode, selectedGameMode: GameMode?, onClick: () -> Unit) {
    val isSelected = mode == selectedGameMode

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
            .background(
                color = if (isSelected) primaryDark else secondaryDark,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = if (isSelected) primaryDark else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryContainerDark else primaryDark,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(mode.iconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Game Icon",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(primaryDark, CircleShape)
                    .padding(8.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = mode.title,
                    style = AppTypography.bodyLarge.copy(
                        fontSize = 20.sp,
                        color = if (isSelected) Color.White else onPrimaryDark
                    ),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mode.description,
                    style = AppTypography.bodySmall.copy(
                        fontSize = 14.sp,
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else onPrimaryDark.copy(alpha = 0.8f)
                    ),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

data class GameMode(
    val title: String,
    val description: String,
    val descriptionLong: String,
    val route: String,
    val iconUrl: String
)