package com.dadm.artisticall.gamemodes

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dadm.artisticall.drawing.DrawingView

@Composable
fun DrawingScreen() {
    val context = LocalContext.current
    val drawingView = remember { DrawingView(context) }
    val colors = listOf(
        Color(AndroidColor.RED),
        Color(AndroidColor.GREEN),
        Color(AndroidColor.BLUE),
        Color(AndroidColor.YELLOW),
        Color(AndroidColor.CYAN),
        Color(AndroidColor.MAGENTA),
        Color(AndroidColor.BLACK),
        Color(AndroidColor.WHITE)
    )
    val defaultLineSize = 10f
    var currentColor by remember { mutableStateOf(Color.Black) }
    var currentLineSize by remember { mutableStateOf(defaultLineSize) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Vista de dibujo
        AndroidView(
            factory = { drawingView },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )

        // Controles
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp)
        ) {
            // Selector de colores
            Text(
                text = "Selecciona un color",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color = color, shape = RoundedCornerShape(8.dp))  // Color y forma definidos
                            .clickable {
                                currentColor = color
                                drawingView.setColor(color.toArgb()) // Convertir el color Compose a un Int usando toArgb()
                            }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Selector de tamaño de línea
            Text(
                text = "Grosor de la línea",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    currentLineSize = 5f
                    drawingView.setLineSize(5f)
                }) {
                    Text("Fina")
                }
                Button(onClick = {
                    currentLineSize = 20f
                    drawingView.setLineSize(20f)
                }) {
                    Text("Gruesa")
                }
                Button(onClick = {
                    currentLineSize = defaultLineSize
                    drawingView.setLineSize(defaultLineSize)
                }) {
                    Text("Restaurar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activar borrador
            Button(
                onClick = {
                    drawingView.activateEraser()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Borrador")
            }
        }
    }
}

