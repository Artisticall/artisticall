package com.dadm.artisticall.gamemodes.modoSolo
import android.content.Context
import android.graphics.Bitmap
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
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 28, 28, true)
        val input = preprocessBitmap(resizedBitmap)
        val output = Array(1) { FloatArray(5) } // Cambia 5 por el número de clases en tu modelo

        interpreter.run(input, output)

        val labels = listOf("Gato", "Perro", "Carro", "Casa", "Árbol") // Ajusta según tu modelo
        val bestPredictionIndex = output[0].indices.maxByOrNull { output[0][it] } ?: 0

        return labels[bestPredictionIndex]
    }

    private fun preprocessBitmap(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val input = Array(1) { Array(28) { Array(28) { FloatArray(1) } } }
        for (x in 0 until 28) {
            for (y in 0 until 28) {
                val pixel = bitmap.getPixel(x, y)
                val grayscale = (pixel and 0xFF) / 255.0f
                input[0][x][y][0] = grayscale
            }
        }
        return input
    }
}
