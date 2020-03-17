package com.br.ml.brpathfinder.ui.views.boundingboxoverlayview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.databinding.BindingAdapter
import android.util.Log
import com.br.ml.brpathfinder.models.DetectedObject
import com.br.ml.brpathfinder.models.Risk
import kotlin.math.roundToInt


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

    // Create this from DPI
    //  Drive these from XML
    private val textFontSize: Float = 60f

    var imageWidth = 1280
    var imageHeight = 960
    private val imageCenter = Pair(imageWidth/2, imageHeight/2)

    // FIXME - Scaling still isn't perfect, need better center-crop, also confirm preview is using center crop
    // FIXME - This involves picking smallest side to determine what to scale to maintain aspect ration

    private val heightScale get () = workHeight.toFloat() / imageHeight
//    private val heightScale get () = 1.0f
//    private val heightScale get () = widthScale
    private val widthScale get () = heightScale
//    private val widthScale get () = workWidth.toFloat() / imageWidth
    private val widthOffset get() = 0
//    private val widthOffset get() = ((center.first - imageCenter.first) * widthScale).roundToInt()
//    private val widthOffset get() = ((imageCenter.first - center.first) * widthScale).roundToInt()
    private val heightOffset get() = 0
//    private val heightOffset get() = ((imageCenter.second - center.second) * heightScale).roundToInt()
    // TODO - Optimize this once it's final so that we don't run computations during draw cycle.

    // TODO -Drive this via binding or attributes
    var stroke = 6f
    var radius = 60f
    var workHeight = 0
    var workWidth = 0

    // Fixed at setup
    var center = Pair(0,0)

    // Dynamic Elements
    var boundingBoxes: List<DetectedObject> = emptyList()
    var risks: List<Risk> = emptyList()

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
        textSize = textFontSize
    }
    private val green = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = Color.GREEN
        textSize = textFontSize
    }
    private val yellow = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = Color.YELLOW
        textSize = textFontSize
    }
    private val orange = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = Color.MAGENTA
        textSize = textFontSize
    }
    private val red = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = Color.RED
        textSize = textFontSize
    }

    private fun colorPicker(id: Int) =
        when (risks.find { it.id == id }?.severity ?: 0f) {
            in .8f .. 1.0f -> red
            in .6f .. .8f -> orange
            in .4f .. .6f -> yellow
            in .2f .. .4f -> green
            else -> blue
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
            val paint = colorPicker(it.id)
            val scaled = it.box.scaleBy(widthScale, heightScale)
                .offsetBy(widthOffset, heightOffset)
            canvas?.drawRect(scaled, paint)
            val risk = risks.find { risk -> risk.id == it.id }
            canvas?.drawText("ID: ${it.id}\n Risk: ${risk?.severity}", scaled.left.toFloat(), scaled.top.toFloat(), paint)

//            canvas?.drawRect(it.offsetBy(widthOffset, heightOffset), blue)
//            canvas?.drawRect(it.scaleBy(widthScale, heightScale), green)
        }
        // TODO - Add labels ?? ID number from ML kit?  Perhaps classifcation?
    }
}

@BindingAdapter("boundingBoxes")
fun BoundingBoxOverlayView.setBoxes(boxes: List<DetectedObject>) {
    boundingBoxes = boxes
    invalidate()
}

@BindingAdapter("risks")
fun BoundingBoxOverlayView.setRiskList(riskList: List<Risk>) {
    risks = riskList
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

