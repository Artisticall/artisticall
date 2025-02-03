import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dadm.artisticall.ui.theme.surfaceDark
import com.dadm.artisticall.ui.theme.AppTypography
import com.dadm.artisticall.ui.theme.backgroundDark
import com.dadm.artisticall.ui.theme.onPrimaryDark
import com.dadm.artisticall.ui.theme.primaryDark
import com.dadm.artisticall.ui.theme.secondaryDark

@Composable
fun LobbyScreen(
    navController: NavController
) {
    val clipboardManager = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val users = listOf("Paula", "Juan Manuel", "Camargod", "Julián", "Felipe", "Jorge")
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

    var selectedGameMode by remember { mutableStateOf<GameMode?>(null) } // Estado para el juego seleccionado
    var showPopup by remember { mutableStateOf(false) } // Estado para mostrar el popup
    var roomCode by remember { mutableStateOf("") } // Estado para el código de la sala

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
            .padding(top = 32.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            UserList(users)
            DescriptionBox(selectedGameMode)
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.4f)
            ) {
                items(gameModes) { mode ->
                    GameModeCard(mode, selectedGameMode) {
                        selectedGameMode = mode
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showPopup = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryDark
                    )
                ) {
                    Text("¿Cómo Unirme?", style = AppTypography.bodyMedium.copy(color = Color.White))
                }
                Button(
                    onClick = {
                        selectedGameMode?.let {
                            navController.navigate(it.route)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = secondaryDark
                    ),
                    enabled = selectedGameMode != null
                ) {
                    Text("Iniciar Juego", style = AppTypography.bodyMedium.copy(color = Color.White))
                }
            }
        }

        if (showPopup) {
            CodeInputPopup(
                onDismiss = { showPopup = false },
                onCopy = {
                    copyToClipboard(roomCode, clipboardManager)
                    showPopup = false
                },
                onJoin = {
                    //TODO: Handle Join Lobbies
                    showPopup = false
                },
                roomCode = roomCode,
                onRoomCodeChange = { roomCode = it }
            )
        }
    }
}

@Composable
fun DescriptionBox(selectedGameMode: GameMode?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(surfaceDark, RoundedCornerShape(8.dp))
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .height(150.dp)
    ) {
        selectedGameMode?.let {
            Column {
                Text(it.title, style = AppTypography.bodyMedium.copy(fontSize = 18.sp).copy(color = Color.White))
                Spacer(modifier = Modifier.height(8.dp))
                Text(it.description, style = AppTypography.bodySmall.copy(color = Color.White))
            }
        } ?: run {
            Text("Selecciona un juego para ver su descripción", style = AppTypography.bodySmall.copy(color = Color.White))
        }
    }
}


@Composable
fun UserList(users: List<String>) {
    LazyRow(modifier = Modifier.padding(16.dp)) {
        items(users) { user ->
            Column(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .width(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(primaryDark, CircleShape)
                        .border(2.dp, onPrimaryDark, CircleShape)
                ) {
                    Text(
                        text = user.take(1),
                        modifier = Modifier.align(Alignment.Center),
                        style = AppTypography.bodySmall.copy(color = Color.White)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(user, style = AppTypography.bodyLarge.copy(color = Color.White), maxLines = 1)
            }
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

@Composable
fun CodeInputPopup(
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onJoin: () -> Unit,
    roomCode: String,
    onRoomCodeChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Cómo Unirme?") },
        text = {
            Column {
                Text("Ingresa un código de 6 dígitos")
                TextField(
                    value = roomCode,
                    onValueChange = { onRoomCodeChange(it) },
                    label = { Text("Código de Sala") },
                    maxLines = 1,
                    isError = roomCode.length != 6
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onCopy,
                enabled = roomCode.length == 6
            ) {
                Text("Copiar Código")
            }
        },
        dismissButton = {
            Button(onClick = onJoin) {
                Text("Unirme a una sala")
            }
        }
    )
}

fun copyToClipboard(roomCode: String, clipboardManager: ClipboardManager) {
    val clip = android.content.ClipData.newPlainText("Room Code", roomCode)
    clipboardManager.setPrimaryClip(clip)
}
