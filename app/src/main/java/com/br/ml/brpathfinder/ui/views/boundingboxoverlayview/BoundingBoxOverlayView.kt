package com.br.ml.brpathfinder.ui.views.boundingboxoverlayview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.databinding.BindingAdapter
import android.util.Log


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
                Log.v("CCS", "Surface changed = h: $height  |  w: $width")
                center = Pair(width/2, height/2)
                workHeight = height
                workWidth = width
                drawWrapper(holder)
            }
            override fun surfaceDestroyed(holder: SurfaceHolder?) {}
        })
        setWillNotDraw(false)
    }

    //  Drive these from XML
    // TODO - Drive this from real values....
    private val imageHeight = 960
    private val imageWidth = 1280

    // FIXME - Scaling still isn't perfect, need better center-crop, also confirm preview is using center crop
    private val imageCenter = Pair(imageWidth/2, imageHeight/2)
    private val heightScale get () = workHeight.toFloat() / imageHeight
    private val widthScale get () = workWidth.toFloat() / imageWidth
    private val widthOffset get() = (imageCenter.first - center.first)
    private val heightOffset get() = (imageCenter.second - center.second)

    var stroke = 6f
    var radius = 60f
    var workHeight = 0
    var workWidth = 0

    // Fixed at setup
    var center = Pair(0,0)

    // Dynamic Elements
    var boundingBoxes: List<Rect> = emptyList()

    // Working holders
    private val white = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = Color.WHITE
    }
    private val blue = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = Color.BLUE
    }
    private val green = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = Color.GREEN
    }


    fun drawWrapper(holder: SurfaceHolder?) {
        val canvas = holder?.lockCanvas() ?: return
        draw(canvas)
        holder.unlockCanvasAndPost(canvas)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawARGB(0,0,0, 0)
        canvas?.drawCircle( center.first.toFloat(), center.second.toFloat(), radius , white)
        boundingBoxes.forEach {
            canvas?.drawRect(it.scaleBy(widthScale, heightScale)
                .offsetBy(widthOffset,heightOffset)
                , white)
//            canvas?.drawRect(it.offsetBy(widthOffset, heightOffset), blue)
//            canvas?.drawRect(it.scaleBy(widthScale, heightScale), green)
        }
        // TODO - Add labels
    }
}

@BindingAdapter("boundingBoxes")
fun BoundingBoxOverlayView.setBoxes(boxes: List<Rect>) {
    boundingBoxes = boxes
    invalidate()
}

fun Rect.scaleBy(xScale: Float, yScale: Float) = Rect(
    (left * xScale).toInt(),
    (top * yScale).toInt(),
    (right * xScale).toInt(),
    (bottom * yScale).toInt())

fun Rect.offsetBy(xOffset: Int, yOffset: Int) = Rect(
    left + xOffset,
    top + yOffset,
    right + xOffset,
    bottom + yOffset
)

