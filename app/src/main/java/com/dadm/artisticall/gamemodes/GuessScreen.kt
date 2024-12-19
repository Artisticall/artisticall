package com.dadm.artisticall.gamemodes

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuessScreen(filePath: String) {
    // Decodificar la ruta del archivo
    val decodedFilePath = Uri.decode(filePath)
    val bitmap = BitmapFactory.decodeFile(decodedFilePath)

    var guessText by remember { mutableStateOf(TextFieldValue()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2D36)) // Fondo oscuro
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mostrar el dibujo
        if (bitmap != null) {
            Image(
                painter = BitmapPainter(bitmap.asImageBitmap()),
                contentDescription = "Imagen del dibujo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 16.dp)
            )
        }

        // Campo de texto para adivinar lo que se dibujó
        TextField(
            value = guessText,
            onValueChange = { guessText = it },
            label = { Text("¿Qué es este dibujo?", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color(0xFF2A3B47), // Fondo del TextField
                focusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedIndicatorColor = Color(0xFFFFA040), // Indicador en naranja
                unfocusedIndicatorColor = Color(0xFF6A6A6A) // Indicador inactivo
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para verificar la adivinanza
        Button(
            onClick = {
                // Acción del botón
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, // Fondo transparente
                contentColor = Color(0xFFFFA040)
            ),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040)) // Borde naranja
        ) {
            Text("Adivinar")
        }
    }
}
