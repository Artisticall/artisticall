package com.dadm.artisticall.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadm.artisticall.ui.theme.AppTypography
import com.dadm.artisticall.ui.theme.onPrimaryDark
import com.dadm.artisticall.ui.theme.primaryDark
import com.dadm.artisticall.ui.theme.secondaryDark

@Composable
fun GamesList(
    selectedGameMode: GameMode?,
    onGameModeSelected: (GameMode?) -> Unit
) {

    val gameModes = listOf(
        GameMode("Normal", "Juego clásico", "game_normal_screen"),
        GameMode("Solo", "Juega en solitario", "game_solo_screen"),
        GameMode("Adivina la Palabra", "Adivina la palabra oculta", "game_adivina_screen"),
        GameMode("¿Qué es esto?", "Intenta adivinar el objeto a partir de pistas", "game_que_es_esto_screen"),
        GameMode("Batalla de Pinceles", "Compite dibujando", "game_batalla_pinceles_screen"),
        GameMode("Ponle Título", "Ponle título a una imagen", "game_ponle_titulo_screen"),
        GameMode("Érase una vez", "Crea historias de manera colaborativa", "game_erase_una_vez_screen"),
        GameMode("Modo Libre", "Dibuja lo que quieras", "game_libre_screen"),
        GameMode("Ojo de Águila", "Adivina el objeto a partir de un pequeño detalle", "game_ojo_de_aguila_screen"),
        GameMode("Modo Colaborativo", "Trabaja en equipo para lograr un objetivo", "game_colaborativo_screen"),
        GameMode("Modo Desafío", "Desafíos rápidos para ganar puntos", "game_desafio_screen")
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
            .background(if (isSelected) primaryDark else secondaryDark)
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(primaryDark, CircleShape)
                    .border(2.dp, onPrimaryDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.title.take(1),
                    style = AppTypography.bodySmall.copy(color = Color.White)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = mode.title,
                    style = AppTypography.bodyLarge.copy(fontSize = 18.sp),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mode.description,
                    style = AppTypography.bodySmall.copy(fontSize = 14.sp),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

data class GameMode(
    val title: String,
    val description: String,
    val route: String
)