package com.br.ml.brpathfinder.collision

import android.util.Log
import com.br.ml.brpathfinder.models.Direction
import com.br.ml.brpathfinder.models.Frame
import com.br.ml.brpathfinder.models.Risk

class AlgorithmicDetector : CollisionDetector() {
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
        val direction: Direction? = null
        val severity = 0.0

        val idHistory: MutableList<Frame>  = mutableListOf()

        history.forEach { frame ->
            val newFrame = Frame(frame.timestamp,
                objects = frame.objects.filter { it.id == id })
            if (newFrame.objects.isNotEmpty()) {
                idHistory.add(newFrame)
            }
        }

        // todo move magic number to const
        if (idHistory.size < 3) return null

        idHistory.sortBy { it.timestamp }
        val finalThree = idHistory.takeLast(3)

        val first = finalThree.first()
        val last  = finalThree.last()
        val timeDelta = last.timestamp - first.timestamp
        if (timeDelta == 0L) return null

        val firstWidth =  with (first.objects.first().box) { right - left }
        val lastWidth =  with (last.objects.first().box) { right - left }
        val widthDelta = lastWidth - firstWidth

        val velocity = widthDelta.toDouble() / timeDelta.toDouble()
        if (velocity > 0) {
            Log.d("CCS", "Velocity: $velocity")
        }
        val approaching: Boolean = velocity > velocityThreshold

        if (approaching) Log.d("CCS", "approaching!")

        // TODO - Detect if box is "centered" = overlapping centerline

        // TODO - detect if box is "centering" = headed towards center

        // TODO - detect delta from center.


        //  TODO - sum up all factors to get risk
        // .25 = approaching
        // .5 = centered
        // 0.0 to 0.5 = centering varied by distance from center
        // 0.0 to -.25 = "un-centering"
        // FIXME - better rules
        // TODO - Move to LSTM driven judgement not algo.
        //  Concerns... not everything is a nail.

        return null
    }


}