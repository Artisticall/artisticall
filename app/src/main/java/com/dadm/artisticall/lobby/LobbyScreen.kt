import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.dadm.artisticall.lobby.GameMode
import com.dadm.artisticall.lobby.GamesList
import com.dadm.artisticall.ui.theme.surfaceDark
import com.dadm.artisticall.ui.theme.AppTypography
import com.dadm.artisticall.ui.theme.backgroundDark
import com.dadm.artisticall.ui.theme.onPrimaryDark
import com.dadm.artisticall.ui.theme.primaryDark
import com.dadm.artisticall.ui.theme.secondaryDark
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

    val clipboardManager = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val users = remember { mutableStateOf(listOf<UserData>()) }
    val maxPlayers = 8

    var showCodeDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            errorMessage = null
        }
    }

    BackHandler {
        showExitDialog = true
    }

    var lobbyCreated by remember { mutableStateOf(false) }
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showCodeDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryDark
                    )
                ) {
                    Text(
                        "¿Cómo unirme?",
                        style = AppTypography.bodyMedium.copy(color = Color.White)
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
                        containerColor = secondaryDark
                    ),
                    enabled = selectedGameMode != null
                ) {
                    Text(
                        "Iniciar Juego",
                        style = AppTypography.bodyMedium.copy(color = Color.White)
                    )
                }
            }
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
                    Text("Escribe un código para unirte a otra sala", style = AppTypography.bodySmall)
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
                                    navController.navigate("lobby_screen/${joinCode}/${username}")
                                }, { message ->
                                    errorMessage = message
                                },
                                    users = users
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

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("¿Salir de la aplicación?") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión y salir?") },
            confirmButton = {
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login_screen")
                        showExitDialog = false
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
                onUsersFetched(players)
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
fun UserList(users: List<UserData>) {
    val hasUsers = users.isNotEmpty()

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
                        user.imageUrl?.let {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(it)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "User Profile Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } ?: run {
                            Text(
                                text = user.username.take(1),
                                modifier = Modifier.align(Alignment.Center),
                                style = AppTypography.bodySmall.copy(color = Color.White)
                            )
                        }
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
    users: MutableState<List<UserData>>
) {
    Firebase.firestore.collection("lobbies")
        .document(lobbyCode)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val lobby = document.toObject(Lobby::class.java)

                if (lobby?.players?.size ?: 0 >= 8) {
                    onFailure("El lobby está lleno. No puedes unirte.")
                } else {
                    val newUser = UserData(username = username)
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
