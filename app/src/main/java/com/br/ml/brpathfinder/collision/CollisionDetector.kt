package com.br.ml.brpathfinder.collision

import android.graphics.Rect
import android.util.Log
import com.br.ml.brpathfinder.models.DetectedObject
import com.br.ml.brpathfinder.models.Direction
import com.br.ml.brpathfinder.models.Frame
import com.br.ml.brpathfinder.models.Risk

abstract class CollisionDetector {
    // TODO  - Drive box boundaries and center line from results from ML Kit

    protected val velocityThreshold = .1
    protected val history: MutableList<Frame> = mutableListOf()
    private val memory = 1000 * 15

    fun createNewFrame(objects: List<DetectedObject>,
                       timestamp: Long = System.currentTimeMillis(),
                       trim: Boolean = true) {
        history.add(Frame(timestamp, objects))
        if (trim) trimHistory()
    }

    private fun trimHistory() {
        val currentTimeMillis = System.currentTimeMillis()
        history.removeAll {
            it.timestamp < currentTimeMillis - memory
        }
    }


    abstract fun runDetection(callback: (List<Risk>) -> Unit)

}



