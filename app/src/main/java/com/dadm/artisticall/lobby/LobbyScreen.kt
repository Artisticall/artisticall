import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.dadm.artisticall.R

@Composable
fun LobbyScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2D36)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo de fondo (paleta de colores)
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Lobby Logo",
                modifier = Modifier
                    .size(320.dp)
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo "Lobby"
            Text(
                text = "Lobby",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lista de usuarios
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                PlayerIcon(name = "Ian")
                Spacer(modifier = Modifier.width(32.dp))
                PlayerIcon(name = "Diana")
                Spacer(modifier = Modifier.width(32.dp))
                PlayerIcon(name = "Alex")
            }

            Spacer(modifier = Modifier.height(16.dp))
            PlayerIcon(name = "Roger")

            Spacer(modifier = Modifier.height(40.dp))

            // Botones
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                LobbyButton(text = "Invitar") {
                    // Acción para invitar
                }
                LobbyButton(text = "Iniciar") {
                    navController.navigate("drawing_screen")
                }
            }
        }
    }
}

@Composable
fun PlayerIcon(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White, shape = RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Icono de usuario
            Image(
                painter = painterResource(id = R.drawable.ic_user), // Reemplaza con tu recurso de ícono
                contentDescription = "Player Icon",
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LobbyButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color(0xFFFFA040) // Naranja
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040)),
        modifier = Modifier
            .width(120.dp)
            .height(48.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

