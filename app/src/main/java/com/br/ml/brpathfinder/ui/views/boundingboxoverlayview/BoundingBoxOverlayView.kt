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
                Log.v("CCS", "BB - Surface changed = h: $height  |  w: $width")
                center = Pair(width/2, height/2)
                workHeight = height
                workWidth = width
                drawWrapper(holder)
            }
            override fun surfaceDestroyed(holder: SurfaceHolder?) {}
        })
        setWillNotDraw(false)
    }

    // Canvas Info
    var workHeight = 0
    var workWidth = 0
    var center = Pair(0,0)

    // Image info
    var imageWidth = 960
        set(value) {
            if (value != field) {
                Log.d("CCS", "BB - imageWidth: $value")
            }
            field = value
        }
    var imageHeight = 1280
        set(value) {
            if (value != field) {
                Log.d("CCS", "BB - imageHeight: $value")
            }
            field = value
        }
    private val imageCenter get() = Pair(imageWidth/2, imageHeight/2)

    // UI - Is fixed at portrait 4:3, as long as we get portrait 4:3 from Ml Kit then a single scaling factor is good.
    // May need lock to portrait until we can handle all rotations??
    private val heightScale get() = workHeight.toFloat() / imageHeight
    private val widthScale get() = heightScale
    private val widthOffset get() = 0
    private val heightOffset get() = 0

    // TODO - Drive this via binding or attributes
    //  Create this from DPI
    //  Drive these from XML
    private val textFontSize: Float = 60f
    var stroke = 6f
    var radius = 60f

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
        Log.d("CCS", "boxes to draw : ${boundingBoxes.size}")
        canvas?.drawARGB(0,0,0, 0)
        canvas?.drawCircle( center.first.toFloat(), center.second.toFloat(), radius , white)
        boundingBoxes.forEach {
            val paint = colorPicker(it.id)
            val scaled = it.box.scaleBy(widthScale, heightScale)
//  We know offset is hardcoded to 0 currently.
//                .offsetBy(widthOffset, heightOffset)
            canvas?.drawRect(scaled, paint)
            val risk = risks.find { risk -> risk.id == it.id }
            canvas?.drawText("ID: ${it.id}\n Risk: ${risk?.severity}", scaled.left.toFloat(), scaled.top.toFloat(), paint)
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

