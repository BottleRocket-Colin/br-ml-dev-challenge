package com.br.ml.brpathfinder.ui.views.boundingboxoverlayview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.databinding.BindingAdapter


class BoundingBoxOverlayView : SurfaceView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSPARENT)
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) = drawWrapper(holder)
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                center = Pair(width/2, height/2)
                drawWrapper(holder)
            }
            override fun surfaceDestroyed(holder: SurfaceHolder?) {}
        })
        setWillNotDraw(false)
    }

    //  Drive these from XML
    var stroke = 6f
    var radius = 60f

    // Fixed at setup
    var center = Pair(0,0)

    // Dynamic Elements
    var boundingBoxes: List<Rect> = emptyList()

    // Working holders
    private val black = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
//        color =
    }


    fun drawWrapper(holder: SurfaceHolder?) {
        val canvas = holder?.lockCanvas() ?: return
        draw(canvas)
        holder.unlockCanvasAndPost(canvas)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawARGB(0,0,0, 0)
        canvas?.drawCircle( center.first.toFloat(), center.second.toFloat(), radius , black)
        boundingBoxes.forEach { canvas?.drawRect(it, black) }
        // TODO - Add labels and verify boxes are accurate
    }
}

@BindingAdapter("boundingBoxes")
fun BoundingBoxOverlayView.setBoxes(boxes: List<Rect>) {
    boundingBoxes = boxes
    invalidate()
}

