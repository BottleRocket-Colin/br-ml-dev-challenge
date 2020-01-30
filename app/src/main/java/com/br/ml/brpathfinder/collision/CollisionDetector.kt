package com.br.ml.brpathfinder.collision

import android.graphics.Rect
import androidx.annotation.FloatRange

object CollisionDetector {
    // TODO  - Drive box boundaries and center line from results from ML Kit

    private val history: MutableList<Frame> = mutableListOf()
    private const val memory = 1000 * 15

    fun createNewFrame(objects: List<DetectedObject>,
                       timestamp: Long = System.currentTimeMillis(),
                       trim: Boolean = true) {
        history.add(Frame(timestamp, objects))
        if (trim) trimHistory()
    }

    private fun trimHistory() {
        val currentTimeMillis = System.currentTimeMillis()
        history.dropWhile {
            it.timestamp < currentTimeMillis - memory
        }
    }

    fun runDetection(callback: (List<Risk>) -> Unit)  {
        val risks: MutableList<Risk> = mutableListOf()
        val latest = history.maxBy { it.timestamp }
        val currentIds = latest?.objects?.map { it.id }

        // FIXME remove this debug code!
        risks.add(
            Risk(if (currentIds?.size?.rem(2) == 0) Direction.LEFT else Direction.RIGHT,
                Math.min((currentIds?.size?.toFloat() ?: 0f / 5f), 1f)
        ))

        callback(risks)
    }

}

data class Frame(val timestamp: Long,
                 val objects: List<DetectedObject>)

data class DetectedObject(val id: Int,
                          val box: Rect)

enum class Direction { LEFT, RIGHT }

data class Risk(val direction: Direction,
                @FloatRange(from=0.0,to=1.0)
                val severity: Float)