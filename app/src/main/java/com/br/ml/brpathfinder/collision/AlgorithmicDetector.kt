package com.br.ml.brpathfinder.collision

import android.util.Log
import com.br.ml.brpathfinder.models.Direction
import com.br.ml.brpathfinder.models.Direction.*
import com.br.ml.brpathfinder.models.Frame
import com.br.ml.brpathfinder.models.Risk
import kotlin.math.max

class AlgorithmicDetector : CollisionDetector() {
    private val timeThreshold = 1000  // One second
    private val velocityThreshold = .1  // pixel per millisecond
    private val scale = .5
    private val centeredRisk = .25
    private val approachRisk = .25
    private val minHistorySize = 3

    override fun runDetection(callback: (List<Risk>) -> Unit)  {
        val risks: MutableList<Risk> = mutableListOf()
        val latest = history.maxBy { it.timestamp }
        val currentIds = latest?.objects?.map { it.id }


        currentIds?.forEach { id ->
            val risk = calculateRisk(id) ?: return@forEach
            risks.add(risk)
        }
        // TODO - Move debug code to new DebugDetector for testing feedback engine.
        // FIXME remove this debug code!
//        risks.add(
//            Risk(if (currentIds?.size?.rem(2) == 0) Direction.LEFT else Direction.RIGHT,
//                Math.min((currentIds?.size?.toFloat() ?: 0f / 5f), 1f)
//        ))

        callback(risks)
    }


    private fun calculateRisk(id: Int): Risk? {
        var severity = 0.0

        val idHistory: MutableList<Frame>  = mutableListOf()

        history.forEach { frame ->
            val newFrame = Frame(timestamp = frame.timestamp,
                objects = frame.objects.filter { it.id == id })
            if (newFrame.objects.isNotEmpty()) {
                idHistory.add(newFrame)
            }
        }

        if (idHistory.size < minHistorySize) return null

        idHistory.sortBy { it.timestamp }
        val finalThree = idHistory.takeLast(3)

        val first = finalThree.first()
        val last  = finalThree.last()
        val timeDelta = last.timestamp - first.timestamp
        if (timeDelta == 0L) return null

        val firstBox = first.objects.first().box
        val lastBox = last.objects.first().box

        val firstWidth =  with (firstBox) { right - left }
        val lastWidth =  with (lastBox) { right - left }
        val widthDelta = lastWidth - firstWidth

        val approachVelocity = widthDelta.toDouble() / timeDelta.toDouble()
        if (approachVelocity > 0) {
            Log.d("CCS", "Velocity: $approachVelocity")
        }
        val approaching: Boolean = approachVelocity > velocityThreshold

        if (approaching) Log.d("CCS", "approaching!")

        val direction: Direction = with (lastBox) {
            when {
                right < width / 2 -> LEFT
                left > width / 2 -> RIGHT
                else -> BOTH
            }
        }

        val leftVelocity = (lastBox.left - firstBox.left).toDouble() / timeDelta.toDouble()
        val rightVelocity = (lastBox.right - firstBox.right).toDouble() / timeDelta.toDouble()

        val centeringVelocity = when (direction) {
            LEFT -> rightVelocity
            RIGHT -> 0.0 - leftVelocity
            BOTH -> rightVelocity - leftVelocity
        }

        //  detect offset from center.
        val centerOffset = when (direction) {
            LEFT -> (width / 2) - lastBox.right
            RIGHT -> lastBox.left - (width / 2)
            BOTH -> (lastBox.left - lastBox.right)/2
        }

        // compute time to collision = T = D / V
        val centeringTime = max(centerOffset / centeringVelocity, 0.0)

        if (centeringTime > 0 && centeringTime < timeThreshold) {
            severity += scale * (timeThreshold - centeringTime) / timeThreshold
        }

        if (approaching) severity += approachRisk
        if (direction == BOTH) severity += centeredRisk

        return Risk(direction, severity.toFloat())
    }
}