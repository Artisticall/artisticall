package com.dadm.artisticall.drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        strokeWidth = 10f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val path = Path()
    private val paths = mutableListOf<Pair<Path, Paint>>()
    private var currentColor = Color.BLACK
    private var currentLineSize = 10f
    private var isEraserActive = false

    init {
        setBackgroundColor(Color.WHITE)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(x, y)
            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
            MotionEvent.ACTION_UP -> {
                paths.add(Pair(Path(path), Paint(paint)))
                path.reset()
            }
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        for ((drawnPath, drawnPaint) in paths) {
            canvas.drawPath(drawnPath, drawnPaint)
        }
        canvas.drawPath(path, paint)
    }

    fun setColor(color: Int) {
        currentColor = color
        isEraserActive = false
        paint.color = currentColor
    }

    fun setLineSize(size: Float) {
        currentLineSize = size
        paint.strokeWidth = currentLineSize
    }

    fun activateEraser() {
        isEraserActive = true
        paint.color = Color.WHITE
    }
}
