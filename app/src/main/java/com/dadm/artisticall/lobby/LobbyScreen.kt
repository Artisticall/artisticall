import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.dadm.artisticall.lobby.GameMode
import com.dadm.artisticall.lobby.GamesList
import com.dadm.artisticall.ui.theme.AppTypography
import com.dadm.artisticall.ui.theme.backgroundDark
import com.dadm.artisticall.ui.theme.onPrimaryDark
import com.dadm.artisticall.ui.theme.onPrimaryLight
import com.dadm.artisticall.ui.theme.onSecondaryDark
import com.dadm.artisticall.ui.theme.onTertiaryDark
import com.dadm.artisticall.ui.theme.primaryDark
import com.dadm.artisticall.ui.theme.secondaryDark
import com.dadm.artisticall.ui.theme.tertiaryDark
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun LobbyScreen(
    navController: NavController,
    lobbyCode: String?,
    username: String?,
    selectedGameMode: GameMode?,
    onGameModeSelected: (GameMode?) -> Unit
) {

    val clipboardManager =
        LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val users = remember { mutableStateOf(listOf<UserData>()) }
    var showCodeDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    val auth = FirebaseAuth.getInstance()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    lobbyCode?.let { code ->
                        leaveLobby(code, username ?: "Desconocido", {
                            Log.d("LobbyScreen", "Usuario salió del lobby correctamente.")
                        }, { message ->
                            errorMessage = message
                        })
                    }
                    auth.signOut()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            errorMessage = null
        }
    }

    var joinCode by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        if (username != null) {
            if (lobbyCode != null) {
                checkLobbyExists(lobbyCode) { exists ->
                    if (!exists) {
                        createLobby(username, lobbyCode)
                        fetchUsers(lobbyCode) { fetchedPlayers ->
                            users.value = fetchedPlayers
                        }
                    } else {
                        fetchUsers(lobbyCode) { fetchedPlayers ->
                            users.value = fetchedPlayers
                        }
                    }
                }
            }
        }
    }




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
            UserList(users.value)
            DescriptionBox(selectedGameMode)

            Box(
                modifier = Modifier.weight(0.4f)
            ) {
                GamesList(
                    selectedGameMode = selectedGameMode,
                    onGameModeSelected = onGameModeSelected
                )
            }

            LobbyActions(
                showCodeDialog = showCodeDialog,
                onShowCodeDialogChange = { showCodeDialog = it },
                lobbyCode = lobbyCode,
                selectedGameMode = selectedGameMode,
                onGameModeSelected = onGameModeSelected,
                navController = navController,
                clipboardManager = clipboardManager
            )
        }
    }

    if (showCodeDialog) {
        AlertDialog(
            onDismissRequest = { showCodeDialog = false },
            title = { Text("¿Cómo Unirme?") },
            text = {
                Column {
                    Text("Código actual: $lobbyCode", style = AppTypography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Escribe un código para unirte a otra sala",
                        style = AppTypography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = joinCode,
                        onValueChange = { joinCode = it },
                        label = { Text("Ingresa un código") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Row {
                    Button(
                        onClick = {
                            if (joinCode.isNotBlank()) {
                                joinGame(joinCode, username ?: "Desconocido", {
                                    Log.d("LobbyScreen", "Unido con éxito al lobby.")
                                    showCodeDialog = false
                                    navController.navigate("lobby_screen/${joinCode}/${username}") {
                                        popUpTo("lobby_screen/${lobbyCode}/${username}") {
                                            inclusive = true
                                        }
                                    }
                                }, { message ->
                                    errorMessage = message
                                },
                                    users = users,
                                    previousLobbyCode = lobbyCode
                                )
                            }
                            showCodeDialog = false
                        }
                    ) {
                        Text("Unirse")
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            lobbyCode?.let { copyToClipboard(it, clipboardManager) }
                        }
                    ) {
                        Text("Copiar código")
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCodeDialog = false }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("¿Ir al Inicio?") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión y salir?") },
            confirmButton = {
                Button(
                    onClick = {
                        lobbyCode?.let { code ->
                            leaveLobby(code, username ?: "Desconocido", {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("login_screen") {
                                    popUpTo(0)
                                }
                                showExitDialog = false
                            }, { message ->
                                errorMessage = message
                            })
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun LobbyActions(
    showCodeDialog: Boolean,
    onShowCodeDialogChange: (Boolean) -> Unit,
    lobbyCode: String?,
    selectedGameMode: GameMode?,
    onGameModeSelected: (GameMode?) -> Unit,
    navController: NavController,
    clipboardManager: ClipboardManager
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onShowCodeDialogChange(true) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = tertiaryDark
            )
        ) {
            Text(
                "¿Cómo unirme?",
                style = AppTypography.bodyMedium.copy(color = onTertiaryDark)
            )
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
                containerColor = secondaryDark,
                disabledContainerColor = secondaryDark.copy(alpha = 0.3f),
                disabledContentColor = onSecondaryDark.copy(alpha = 0.3f),
            ),
            enabled = selectedGameMode != null
        ) {
            Text(
                "Iniciar Juego",
                style = AppTypography.bodyMedium.copy(color = onSecondaryDark)
            )
        }
    }
}

fun checkLobbyExists(lobbyCode: String, onResult: (Boolean) -> Unit) {
    Firebase.firestore.collection("lobbies")
        .document(lobbyCode)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val lobby = document.toObject(Lobby::class.java)
                val hasPlayers = lobby?.players?.isNotEmpty() == true
                onResult(hasPlayers)
            } else {
                onResult(false)
            }
        }
        .addOnFailureListener { e ->
            Log.e("LobbyScreen", "Error al verificar si el lobby existe: ${e.message}")
            onResult(false)
        }
}


data class UserData(
    val username: String = "",
    val imageUrl: String? = null
)

fun fetchUsers(lobbyCode: String, onUsersFetched: (List<UserData>) -> Unit) {
    Firebase.firestore.collection("lobbies")
        .document(lobbyCode)
        .addSnapshotListener { document, e ->
            if (e != null) {
                Log.e("LobbyScreen", "Error al obtener los jugadores: ${e.message}")
                return@addSnapshotListener
            }

            if (document != null && document.exists()) {
                val lobby = document.toObject(Lobby::class.java)
                val players = lobby?.players.orEmpty()
                Log.d("LobbyScreen", "Jugadores del lobby: $players")

                if (players.isEmpty()) {
                    Firebase.firestore.collection("lobbies")
                        .document(lobbyCode)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("LobbyScreen", "Lobby eliminado porque no hay jugadores.")
                        }
                        .addOnFailureListener { ex ->
                            Log.e("LobbyScreen", "Error al eliminar el lobby: ${ex.message}")
                        }
                } else {
                    onUsersFetched(players)
                }
            } else {
                Log.e("LobbyScreen", "Lobby no encontrado")
            }
        }
}



@Composable
fun DescriptionBox(selectedGameMode: GameMode?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(200.dp)
    ) {
        selectedGameMode?.let { gameMode ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(onPrimaryLight, RoundedCornerShape(16.dp))
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .padding(24.dp)
                    .height(180.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(gameMode.iconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Game Icon",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 12.dp)
                    )

                    Text(
                        text = gameMode.title,
                        style = AppTypography.bodyMedium.copy(
                            fontSize = 20.sp,
                            color = onPrimaryDark,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = gameMode.descriptionLong,
                    style = AppTypography.bodySmall.copy(
                        color = onPrimaryDark,
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        } ?: run {
            Text(
                text = "Selecciona un juego para ver su descripción",
                style = AppTypography.bodySmall.copy(
                    color = Color.White,
                    fontSize = 18.sp
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


@Composable
fun UserList(users: List<UserData>) {
    val hasUsers = users.isNotEmpty()
    val defaultImageUrl = "https://avatar.iran.liara.run/public"

    LazyRow(modifier = Modifier.padding(16.dp)) {
        if (!hasUsers) {
            item {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(16.dp), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = primaryDark,
                        strokeWidth = 4.dp
                    )
                }
            }
        } else {
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
                        val imageUrl = user.imageUrl ?: defaultImageUrl
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "User Profile Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(user.username, style = AppTypography.bodyLarge.copy(color = Color.White), maxLines = 1)
                }
            }
        }
    }
}

data class Lobby(
    val id: String = "",
    val gameMode: String = "",
    val players: List<UserData> = emptyList(),
    val code: String = ""
)

fun createLobby(username: String, lobbyCode: String){
    val user = FirebaseAuth.getInstance().currentUser
    val imageUrl = user?.photoUrl?.toString()
    val userData = UserData(username = username, imageUrl = imageUrl)

    val lobby = Lobby(
        id = lobbyCode,
        gameMode = "",
        players = listOf(userData),
        code = lobbyCode
    )
    Firebase.firestore.collection("lobbies")
        .document(lobbyCode)
        .set(lobby)
        .addOnSuccessListener {
            Log.d("Lobby", "Lobby created successfully!")
        }
        .addOnFailureListener { e ->
            Log.e("Lobby", "Lobby failed creating at: ${e.message}")
        }
}

fun copyToClipboard(lobbyCode: String, clipboardManager: ClipboardManager) {
    val clip = ClipData.newPlainText("Room Code", lobbyCode)
    clipboardManager.setPrimaryClip(clip)
}

fun joinGame(
    lobbyCode: String,
    username: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit,
    users: MutableState<List<UserData>>,
    previousLobbyCode: String?
) {
    val user = FirebaseAuth.getInstance().currentUser
    val imageUrl = user?.photoUrl?.toString()

    previousLobbyCode?.let { code ->
        leaveLobby(code, username, {
            Firebase.firestore.collection("lobbies")
                .document(lobbyCode)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val lobby = document.toObject(Lobby::class.java)

                        if ((lobby?.players?.size ?: 0) >= 8) {
                            onFailure("El lobby está lleno. No puedes unirte.")
                        } else {
                            val newUser = UserData(username = username, imageUrl = imageUrl)
                            val updatedPlayers = lobby?.players.orEmpty() + newUser

                            Firebase.firestore.collection("lobbies")
                                .document(lobbyCode)
                                .update("players", updatedPlayers)
                                .addOnSuccessListener {
                                    Log.d("LobbyScreen", "Jugador agregado con éxito al lobby.")
                                    fetchUsers(lobbyCode) { fetchedPlayers ->
                                        users.value = fetchedPlayers
                                    }
                                    onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("LobbyScreen", "Error al agregar jugador al lobby: ${e.message}")
                                    onFailure("Hubo un error al unirte al lobby.")
                                }
                        }
                    } else {
                        onFailure("No se encontró el lobby con el código: $lobbyCode")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LobbyScreen", "Error obteniendo el lobby: ${e.message}")
                    onFailure("Hubo un error al verificar el código.")
                }
        }, { message ->
            onFailure(message)
        })
    } ?: run {
        Firebase.firestore.collection("lobbies")
            .document(lobbyCode)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val lobby = document.toObject(Lobby::class.java)

                    if ((lobby?.players?.size ?: 0) >= 8) {
                        onFailure("El lobby está lleno. No puedes unirte.")
                    } else {
                        val newUser = UserData(username = username, imageUrl = imageUrl)
                        val updatedPlayers = lobby?.players.orEmpty() + newUser

                        Firebase.firestore.collection("lobbies")
                            .document(lobbyCode)
                            .update("players", updatedPlayers)
                            .addOnSuccessListener {
                                Log.d("LobbyScreen", "Jugador agregado con éxito al lobby.")
                                fetchUsers(lobbyCode) { fetchedPlayers ->
                                    users.value = fetchedPlayers
                                }
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.e("LobbyScreen", "Error al agregar jugador al lobby: ${e.message}")
                                onFailure("Hubo un error al unirte al lobby.")
                            }
                    }
                } else {
                    onFailure("No se encontró el lobby con el código: $lobbyCode")
                }
            }
            .addOnFailureListener { e ->
                Log.e("LobbyScreen", "Error obteniendo el lobby: ${e.message}")
                onFailure("Hubo un error al verificar el código.")
            }
    }
}

fun leaveLobby(lobbyCode: String, username: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
    Firebase.firestore.collection("lobbies")
        .document(lobbyCode)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val lobby = document.toObject(Lobby::class.java)
                val updatedPlayers = lobby?.players?.filter { it.username != username } ?: emptyList()

                if (updatedPlayers.isEmpty()) {
                    Firebase.firestore.collection("lobbies")
                        .document(lobbyCode)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("LobbyScreen", "Lobby eliminado porque no hay jugadores.")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.e("LobbyScreen", "Error al eliminar el lobby: ${e.message}")
                            onFailure("Hubo un error al eliminar el lobby.")
                        }
                } else {
                    Firebase.firestore.collection("lobbies")
                        .document(lobbyCode)
                        .update("players", updatedPlayers)
                        .addOnSuccessListener {
                            Log.d("LobbyScreen", "Jugador eliminado con éxito del lobby.")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.e("LobbyScreen", "Error al eliminar jugador del lobby: ${e.message}")
                            onFailure("Hubo un error al abandonar el lobby.")
                        }
                }
            } else {
                onFailure("No se encontró el lobby con el código: $lobbyCode")
            }
        }
        .addOnFailureListener { e ->
            Log.e("LobbyScreen", "Error obteniendo el lobby: ${e.message}")
            onFailure("Hubo un error al verificar el código.")
        }
}