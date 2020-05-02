package com.br.ml.brpathfinder.ui.views.boundingboxoverlayview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.databinding.BindingAdapter
import android.util.Log
import com.br.ml.brpathfinder.models.DetectedObject
import com.br.ml.brpathfinder.models.Frame
import com.br.ml.brpathfinder.models.Risk
import java.math.RoundingMode
import java.text.DecimalFormat


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

    private val decimalFormat = DecimalFormat("###.###").apply {
        roundingMode = RoundingMode.CEILING
    }

    // Dynamic Elements
    var history: List<Frame> = emptyList()
    private val boundingBoxes
        get() = history.filter {
            it.timestamp >  System.currentTimeMillis() - tail
        }.flatMap { frame ->
            frame.objects.map { Pair(frame.timestamp, it) }
        }.let { flatList ->
            flatList.partition { it.second.id == 0 }.let { (unknown, identified) ->
                identified.sortedByDescending { it.first }.distinctBy { it.second.id } + unknown
            }.apply {
                forEach { (_, detected) ->
                    Log.d("CCS", "BBB - Made it to forEach")
                    if (detected.id != 0) {
                        // TODO - Do same thing of risk....
                        detected.bubbledUpDistance = flatList.filter {
                            it.second.id == detected.id && it.second.distance != null
                        }.maxBy {
                            Log.d("CCS", "BBB - Made it to maxBy")
                            it.first
                        }?.second?.distance
                    }
//                    if (detected.bubbledUpDistance == null) {
                        // TODO - Implement a positional based search as backup in case ID changes
                        //   ..... So a frame with new ID that overlapped a preivous frame less than a bit ago....
                        //    if underlying ID doesn't exist in current frame then likely ID number just changed, also
                        //  but it won't make sense until our depth map is closer to 100 milisecond round trip
//                    }
                }
            }
        }

    private val tail = 1500

    // TODO - Remove after we don't need this to trigger update of UI
    var risks: List<Risk> = emptyList()

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
    private val textFontSize = 60f
    private val textLineHeight = 90f
    var stroke = 6f
    var radius = 60f


    ///////////////////////////////////////////////////////////////////////////
    // Paints
    ///////////////////////////////////////////////////////////////////////////
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

    private fun colorPicker(obj: DetectedObject) =
        when (obj.risk?.severity ?: 0f) {
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

        // Static elements
        canvas?.drawARGB(0,0,0, 0)
        canvas?.drawCircle( center.first.toFloat(), center.second.toFloat(), radius , white)

        // redeclare to avoid costly getter for boundingBoxes.... or move that into function, so iit's implied.... ???
        val boxes = boundingBoxes

        Log.d("CCS", "history size : ${history.size}")
        Log.d("CCS", "boxes to draw : ${boxes.size}")
        val sysTime = System.currentTimeMillis()
        boxes.forEach { (timestamp, detected) ->
            val paint = colorPicker(detected)
            val scaled = detected.box.scaleBy(widthScale, heightScale)


            paint.alpha = 255 - (((sysTime - timestamp).toFloat() / tail.toFloat()) * 255).toInt().coerceAtMost(255)
            canvas?.drawRect(scaled, paint)

            // Add debug info to box.
            var line1 = ""
            if (detected.id > 0) line1 += "ID: ${detected.id}  "
            detected.risk?.severity?.let { line1 += "Risk: ${detected.risk?.severity}" }
            if (line1.isNotEmpty()) {
                canvas?.drawText(line1, scaled.left.toFloat(), scaled.top.toFloat(), paint)
            }

            detected.bubbledUpDistance?.let {
                canvas?.drawText("Dist: ${decimalFormat.format(it)}", scaled.left.toFloat(), scaled.top.toFloat() + textLineHeight, paint)
            }
            paint.alpha = 255
        }
    }
}

@BindingAdapter("history")
fun BoundingBoxOverlayView.setBoxes(history: List<Frame>) {
    this.history = history
    invalidate()
}

// FIXME - USe another mehtod than this unsued list to trigger UI updates....
@BindingAdapter("risks")
fun BoundingBoxOverlayView.setRiskList(riskList: List<Risk>) {
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

