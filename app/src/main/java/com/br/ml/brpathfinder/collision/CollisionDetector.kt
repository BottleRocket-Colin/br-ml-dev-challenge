package com.br.ml.brpathfinder.collision

import com.br.ml.brpathfinder.models.Frame
import com.br.ml.brpathfinder.models.Risk

abstract class CollisionDetector {
    protected val history: MutableList<Frame> = mutableListOf()
    val frameHistory get() = history.toList()

    private val memory = 1000 * 3

    //  Store H&W for image
    var height = 0
    var width = 0
    val center get() = width / 2

    private fun trimHistory() {
        history.removeAll { it.timestamp < System.currentTimeMillis() - memory }
    }

    fun addFrame(frame: Frame, trim: Boolean= true) {
        history.add(frame)
        if (trim) trimHistory()
    }

    abstract fun runDetection(callback: (List<Risk>) -> Unit)
}



