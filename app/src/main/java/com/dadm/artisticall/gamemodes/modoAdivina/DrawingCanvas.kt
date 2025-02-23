package com.dadm.artisticall.gamemodes.modoAdivina

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    isDrawingEnabled: Boolean,
    lobbyCode: String,
    onDrawingChanged: (List<DrawPath>) -> Unit
) {
    var currentPath by remember { mutableStateOf<MutableList<Point>>(mutableListOf()) }
    var paths by remember { mutableStateOf<List<DrawPath>>(listOf()) }
    var currentColor by remember { mutableStateOf(Color.Black) }
    var currentStrokeWidth by remember { mutableStateOf(5f) }

    LaunchedEffect(lobbyCode) {
        FirebaseFirestore.getInstance()
            .collection("games")
            .document(lobbyCode)
            .collection("drawing")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val points = doc.get("points") as? List<Map<String, Any>>
                        val color = (doc.get("color") as? Long)?.toInt() ?: Color.Black.toArgb()
                        val strokeWidthValue = doc.get("strokeWidth")
                        println("StrokeWidth value: $strokeWidthValue")
                        println("StrokeWidth value type: ${strokeWidthValue?.javaClass?.simpleName}")
                        val strokeWidth: Float =
                            (doc.get("strokeWidth") as? Double)?.toFloat() ?: 5f

                        points?.map { point ->
                            val x = (point["x"] as? Double)?.toFloat() ?: 0f
                            val y = (point["y"] as? Double)?.toFloat() ?: 0f
                            Point(x, y)
                        }?.let { pointsList ->
                            DrawPath(pointsList, color, strokeWidth)
                        }
                    } catch (e: Exception) {
                        println("Error parsing document: ${e.message}")
                        null
                    }
                }?.let { newPaths ->
                    paths = newPaths
                }
            }
    }

    Column(modifier = modifier) {
        // Canvas para dibujo
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .pointerInput(isDrawingEnabled) {
                    if (!isDrawingEnabled) return@pointerInput

                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = mutableListOf(Point(offset.x, offset.y))
                        },
                        onDrag = { change, _ ->
                            val newPoint = Point(change.position.x, change.position.y)
                            currentPath.add(newPoint)

                            paths = paths + DrawPath(
                                currentPath.toList(),
                                currentColor.toArgb(),
                                currentStrokeWidth
                            )
                        },
                        onDragEnd = {
                            val newPath = DrawPath(
                                currentPath.toList(),
                                currentColor.toArgb(),
                                currentStrokeWidth
                            )

                            FirebaseFirestore.getInstance()
                                .collection("games")
                                .document(lobbyCode)
                                .collection("drawing")
                                .add(mapOf(
                                    "points" to newPath.points.map {
                                        mapOf("x" to it.x, "y" to it.y)
                                    },
                                    "color" to newPath.color,
                                    "strokeWidth" to newPath.strokeWidth.toDouble()
                                ))

                            onDrawingChanged(paths)
                            currentPath = mutableListOf()
                        }
                    )
                }
        ) {
            // Dibujar todos los paths
            paths.forEach { path ->
                val drawPath = Path()
                path.points.forEachIndexed { index, point ->
                    if (index == 0) {
                        drawPath.moveTo(point.x, point.y)
                    } else {
                        drawPath.lineTo(point.x, point.y)
                    }
                }

                drawPath(
                    path = drawPath,
                    color = Color(path.color),
                    style = Stroke(
                        width = path.strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // Barra de herramientas de dibujo
        if (isDrawingEnabled) {
            DrawingToolbar(
                currentColor = currentColor,
                currentStrokeWidth = currentStrokeWidth,
                onColorSelected = { currentColor = it },
                onStrokeWidthChanged = { currentStrokeWidth = it },
                onClearCanvas = {
                    paths = emptyList()
                    FirebaseFirestore.getInstance()
                        .collection("games")
                        .document(lobbyCode)
                        .collection("drawing")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            snapshot.documents.forEach { it.reference.delete() }
                        }
                }
            )
        }
    }
}

@Composable
fun DrawingToolbar(
    currentColor: Color,
    currentStrokeWidth: Float,
    onColorSelected: (Color) -> Unit,
    onStrokeWidthChanged: (Float) -> Unit,
    onClearCanvas: () -> Unit
) {
    val colors = listOf(
        Color.Black,
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color.Magenta
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selector de colores
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color, shape = MaterialTheme.shapes.small)
                    .padding(2.dp)
                    .clickable { onColorSelected(color) }
            ) {
                if (color == currentColor) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, Color.White, MaterialTheme.shapes.small)
                    )
                }
            }
        }

        // Selector de grosor
        Slider(
            value = currentStrokeWidth,
            onValueChange = onStrokeWidthChanged,
            valueRange = 1f..20f,
            modifier = Modifier.width(150.dp)
        )

        // Botón para limpiar
        Button(
            onClick = onClearCanvas,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Limpiar")
        }
    }
}