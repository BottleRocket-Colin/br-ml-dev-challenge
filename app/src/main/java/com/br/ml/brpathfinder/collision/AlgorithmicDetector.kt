package com.br.ml.brpathfinder.collision

import android.graphics.Rect
import android.util.Log
import com.br.ml.brpathfinder.models.DetectedObject
import com.br.ml.brpathfinder.models.Direction
import com.br.ml.brpathfinder.models.Direction.*
import com.br.ml.brpathfinder.models.Frame
import com.br.ml.brpathfinder.models.Risk
import kotlin.math.absoluteValue
import kotlin.math.min

class AlgorithmicDetector : CollisionDetector() {
    private val minHistorySize = 3
    private val timeThreshold = 1000  // One second
    private val tail = 1500
    private val velocityThreshold = .1  // pixel per millisecond
    private val scale = .5
    private val velocityScale = .5
    private val distanceScale = 5.0
    private val centeredRisk = .25
    private val approachRisk = .25
    private val offsetRisk = .25
    private val distanceRisk = .25
    private val finalScale = 1.3

    override fun runDetection(callback: (List<Risk>) -> Unit)  {
        val risks: MutableList<Risk> = mutableListOf()
        val latest = history.maxBy { it.timestamp }
        latest?.objects?.forEach { risks.add(calculateRisk(it) ?: return@forEach) }
        callback(risks)
    }


    private fun calculateRisk(obj: DetectedObject): Risk? {
        Log.d("CCS", "AG - Calculating Risk for ID: ")
        var severity = 0.0

        val idHistory: MutableList<Frame>  = mutableListOf()

        // TODO - Can we use map and filter here to shorten this
        history.forEach { frame ->
            val newFrame = Frame(timestamp = frame.timestamp,
                objects = frame.objects.filter { it.id == obj.id })
            if (newFrame.objects.isNotEmpty()) {
                idHistory.add(newFrame)
            }
        }
        // TODO - also want to process 0 IDs with no velocity computed, just from last frame.
        // TODO - Float up distances ????, if so , store age of distance that gets bubbled up

        Log.d("CCS", "AG - ID: ${obj.id}\t    History size: ${idHistory.size}")
        if (idHistory.size < minHistorySize) return null

        idHistory.sortBy { it.timestamp }
        val finalThree = idHistory.takeLast(3)

        val first = finalThree.first()
        val last  = finalThree.last()
        val timeDelta = last.timestamp - first.timestamp
        Log.d("CCS", "AG - ID: ${obj.id}\t    Timespan: $timeDelta")
        if (timeDelta == 0L) return null

        val firstBox = first.objects.first().box
        val lastBox = last.objects.first().box

        val firstWidth =  with (firstBox) { right - left }
        val lastWidth =  with (lastBox) { right - left }
        val widthDelta = lastWidth - firstWidth

        val approachVelocity = widthDelta.toDouble() / timeDelta.toDouble()
        Log.d("CCS", "AG - ID: ${obj.id}\t    Velocity: $approachVelocity")

        val approaching: Boolean = approachVelocity > velocityThreshold
        if (approaching) Log.d("CCS", "AG - ID: ${obj.id}\t    approaching!")

        // determine centered
        val direction: Direction = with (lastBox) {
            when {
                right < width / 2 -> LEFT
                left > width / 2 -> RIGHT
                else -> BOTH
            }
        }
        Log.d("CCS", "AG - ID: ${obj.id}\t    Direction: $direction")

        //  detect offset from center.
        val centerOffset = when (direction) {
            LEFT -> getLeftOffset(lastBox)
            RIGHT -> getRightOffset(lastBox)
            BOTH ->  min(getLeftOffset(lastBox).absoluteValue, getRightOffset(lastBox).absoluteValue)
        }
        Log.d("CCS", "AG - ID: ${obj.id}\t    Center Offset: $centerOffset")

        // Scale offset to percentage of half of the screen.
        val scaledOffset = if (direction == BOTH) {
            centerOffset.toDouble() / center.toDouble()
        } else {
            (center - centerOffset).toDouble() / center.toDouble()
        }
        Log.d("CCS", "AG - ID: ${obj.id}\t    Scaled Offset: $scaledOffset")

        // Bubble up Distance from most recent ID match
        if (obj.id != 0) {
            obj.bubbledUpDistance = history.filter {
                it.timestamp > System.currentTimeMillis() - tail
            }.flatMap { frame ->
                frame.objects.map { Pair(frame.timestamp, it) }
            }.filter {
                it.second.id == obj.id && it.second.distance != null
            }.maxBy {
                it.first
            }?.second?.distance
        }

        // Use best distance
        (obj.distance ?: obj.bubbledUpDistance)?.let { workingDistance ->
            //  distance is backwards on scale from 10->0 , 10 = near, 0 = far
            Log.d("CCS", "AG - ID: ${obj.id}\t    Working Distance: $workingDistance")
            val scaledDistance = (workingDistance - distanceScale) / distanceScale
            Log.d("CCS", "AG - ID: ${obj.id}\t    Scaled Distance: $scaledDistance")

            Log.d("CCS", "AG - ID: ${obj.id}\t    Distance Risk: ${scaledDistance * distanceRisk} ")
            severity =+ scaledDistance * distanceRisk
        }

        // Apply computed risks to severity
        Log.d("CCS", "AG - ID: ${obj.id}\t    Offset Risk: ${scaledOffset * offsetRisk}")
        severity += scaledOffset * offsetRisk
        if (direction == BOTH) {
            Log.d("CCS", "AG - ID: ${obj.id}\t    Centered Risk: $centeredRisk")
            severity += centeredRisk
        }

        Log.d("CCS", "AG - ID: ${obj.id}\t    Approach Risk: ${(approachVelocity / velocityScale) * approachRisk}")
        severity += (approachVelocity / velocityScale) * approachRisk

        severity *= finalScale
        severity = severity.coerceAtMost(1.0)
        Log.d("CCS", "AG - ID: ${obj.id}\t    Final Risk: $severity")
        return Risk(direction, severity.toFloat()).also { obj.risk = it }
    }

    private fun getRightOffset(lastBox: Rect) = lastBox.left - (width / 2)

    private fun getLeftOffset(lastBox: Rect) = (width / 2) - lastBox.right
}



//      TODO - Archive velocity based code for now.  We're going to try just realtime status updates.
//          If they're faster enough we won't need preditive logic
//        val leftVelocity = (lastBox.left - firstBox.left).toDouble() / timeDelta.toDouble()
//        val rightVelocity = (lastBox.right - firstBox.right).toDouble() / timeDelta.toDouble()
//
//        val centeringVelocity = when (direction) {
//            LEFT -> rightVelocity
//            RIGHT -> 0.0 - leftVelocity
//            BOTH -> rightVelocity - leftVelocity
//        }
//        // compute time to collision = T = D / V
//        val centeringTime = max(centerOffset / centeringVelocity, 0.0)
//
//        if (centeringTime > 0 && centeringTime < timeThreshold) {
//            severity += scale * (timeThreshold - centeringTime) / timeThreshold
//        }
