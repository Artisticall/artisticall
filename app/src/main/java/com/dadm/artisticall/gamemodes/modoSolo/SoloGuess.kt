package com.dadm.artisticall.gamemodes.modoSolo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.material3.*
import androidx.compose.ui.Alignment


@Composable
fun SoloGuess(navController: NavController, imagePath: String) {
    val context = LocalContext.current
    val bitmap = remember(imagePath) { loadBitmapFromPath(context, imagePath) }
    var prediction by remember { mutableStateOf("Analizando dibujo...") }


    // Cargar el modelo y hacer la predicción cuando la pantalla se inicia
    LaunchedEffect(bitmap) {
        bitmap?.let {
            val model = ImageClassifier(context)
            prediction = model.predict(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        bitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Tu Dibujo", modifier = Modifier.size(200.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Predicción: $prediction", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
        Button(
            onClick = {
                navController.navigate("lobby_screen")
            },
        ) {
            Text(text = "Lobby")
        }
    }
}
fun loadBitmapFromPath(context: Context, path: String): Bitmap? {
    return try {
        BitmapFactory.decodeFile(Uri.decode(path))
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
