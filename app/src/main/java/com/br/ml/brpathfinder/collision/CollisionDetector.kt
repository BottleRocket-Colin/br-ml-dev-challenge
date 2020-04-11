package com.br.ml.brpathfinder.collision

import com.br.ml.brpathfinder.models.Frame
import com.br.ml.brpathfinder.models.Risk

abstract class CollisionDetector {
    protected val history: MutableList<Frame> = mutableListOf()
    private val memory = 1000 * 15

    //  Store H&W for image
    var height = 0
    var width = 0

    private fun trimHistory() {
        val currentTimeMillis = System.currentTimeMillis()
        history.removeAll {
            it.timestamp < currentTimeMillis - memory
        }
    }

    fun addFrame(frame: Frame, trim: Boolean= true) {
        history.add(frame)
        if (trim) trimHistory()
    }

    abstract fun runDetection(callback: (List<Risk>) -> Unit)
}



