package com.br.ml.brpathfinder.models

import android.graphics.Rect
import java.util.*

data class Frame(val objects: List<DetectedObject>,
                 val timestamp: Long = System.currentTimeMillis(),
                 val gyroSensor: Vector<Double>? = null,
                 val compassSensor: Vector<Double>? = null,
                 val accelerometerSensor: Vector<Double>? = null
)

data class DetectedObject(val id: Int,
                          val box: Rect,
                          var distance: Double? = null)
