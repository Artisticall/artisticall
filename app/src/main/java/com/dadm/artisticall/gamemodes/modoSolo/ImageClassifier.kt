package com.dadm.artisticall.gamemodes.modoSolo
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ImageClassifier(context: Context) {
    private var interpreter: Interpreter

    init {
        interpreter = Interpreter(loadModelFile(context))
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("quickdraw_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }


    fun predict(bitmap: Bitmap): String {
        val input = preprocessBitmap(bitmap)
        val output = Array(1) { FloatArray(8) }

        interpreter.run(input, output)

        
        val bestPredictionIndex = output[0].indices.maxByOrNull { output[0][it] } ?: 0
        val labels = listOf("butterfly", "cat","fish", "house",
            "tree", "triangle", "square", "circle"  )


        return labels.getOrElse(bestPredictionIndex) { "Desconocido" }
    }

    private fun preprocessBitmap(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val resizedBitmap = resizeBitmap(bitmap, 28, 28)  // Redimensionar
        val input = Array(1) { Array(28) { Array(28) { FloatArray(1) } } }

        // Recorrer cada píxel del Bitmap
        for (x in 0 until 28) {
            for (y in 0 until 28) {

                val pixel = resizedBitmap.getPixel(x, y)

                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                val grayValue = (red + green + blue) / 3.0f

                val normalizedValue = grayValue / 255.0f

                input[0][x][y][0] = normalizedValue
            }
        }

        return input
    }


    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        // Calcular el factor de escala
        val scaleFactor =
            (options.outWidth / targetWidth).coerceAtMost(options.outHeight / targetHeight)

        // Decodificar la imagen con el factor de escala
        options.inJustDecodeBounds = false
        options.inSampleSize = scaleFactor

        // Redimensionar la imagen al tamaño deseado (28x28)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

}

