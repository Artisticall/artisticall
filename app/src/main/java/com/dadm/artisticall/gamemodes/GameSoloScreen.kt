package com.dadm.artisticall.gamemodes

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
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
import com.dadm.artisticall.drawing.DrawingView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.delay

@Composable
fun GameSoloScreen(navController: NavController) {
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
        Color(0xFF6A4E23)
    )
    val defaultLineSize = 10f
    var currentColor by remember { mutableStateOf(Color.Black) }
    var currentLineSize by remember { mutableStateOf(defaultLineSize) }

    // Estado para el temporizador
    var timeLeft by remember { mutableStateOf(50) }
    var isTimerRunning by remember { mutableStateOf(true) }

    // Función para guardar la imagen
    fun saveImage() {
        val bitmap = drawingView.getBitmap()
        val file = File(context.cacheDir, "drawing_image.png")
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            val encodedFilePath = Uri.encode(file.absolutePath)
            navController.navigate("guess_screen/$encodedFilePath")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Iniciar el temporizador
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
        }
        if (timeLeft == 0) {
            saveImage()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { drawingView },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E2D36))
                .padding(8.dp)
        ) {
            Text(
                text = "Selecciona un color",
                color = Color.White,
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
                            .background(color = color, shape = RoundedCornerShape(8.dp))
                            .clickable {
                                currentColor = color
                                drawingView.setColor(color.toArgb())
                            }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Grosor de la línea",
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        currentLineSize = 5f
                        drawingView.setLineSize(5f)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFA040)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
                ) {
                    Text("Fina")
                }
                Button(
                    onClick = {
                        currentLineSize = 20f
                        drawingView.setLineSize(20f)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFA040)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
                ) {
                    Text("Gruesa")
                }
                Button(
                    onClick = {
                        currentLineSize = defaultLineSize
                        drawingView.setLineSize(defaultLineSize)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFA040)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
                ) {
                    Text("Restaurar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tamaño del borrador",
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        drawingView.setEraserSize(15f)
                        drawingView.activateEraser()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFA040)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
                ) {
                    Text("Pequeño")
                }
                Button(
                    onClick = {
                        drawingView.setEraserSize(30f)
                        drawingView.activateEraser()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFA040)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
                ) {
                    Text("Grande")
                }
                Button(
                    onClick = {
                        drawingView.clearCanvas()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFA040)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFA040))
                ) {
                    Text("Borrar Pizarra")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tiempo restante: $timeLeft segundos",
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}





