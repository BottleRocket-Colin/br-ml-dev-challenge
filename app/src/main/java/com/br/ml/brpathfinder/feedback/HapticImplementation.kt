package com.br.ml.brpathfinder.feedback

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.br.ml.brpathfinder.models.Direction

class HapticImplementation(val context: Context) : FeedbackInterface {
    private val FEEDBACK_LENGTH_A = 100L
    private val FEEDBACK_LENGTH_B = 300L
    private val TAG = "HapticImplementation"

    // These can be changed easily
    private val leftVibratePattern =
        longArrayOf(FEEDBACK_LENGTH_B, FEEDBACK_LENGTH_A, FEEDBACK_LENGTH_B)
    private val rightVibratePattern =
        longArrayOf(FEEDBACK_LENGTH_A, FEEDBACK_LENGTH_B, FEEDBACK_LENGTH_A)
    private val bothVibratePattern =
        longArrayOf(FEEDBACK_LENGTH_A, FEEDBACK_LENGTH_A, FEEDBACK_LENGTH_A)

    // TODO - parse risks here????
    override fun signalUser(direction: Direction, severity: Float) {
        if (severity > .25)
            performVibrate(context, direction, severity)
    }

    private fun performVibrate(context: Context, direction: Direction, severity: Float) {
        // Vibration effects takes an Int
        val amplitude = (severity * 255).toInt().let { intArrayOf(it, it, it) }
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val leftVibrate = VibrationEffect.createWaveform(leftVibratePattern, amplitude, -1)
        val rightVibrate = VibrationEffect.createWaveform(rightVibratePattern, amplitude, -1)
        val bothVibrate = VibrationEffect.createWaveform(bothVibratePattern, amplitude, -1)

        when (direction) {
            Direction.LEFT -> {
                vibrator.vibrate(leftVibrate)
                Log.d(TAG, "Vibrate on $direction at $amplitude severity")
            }
            Direction.RIGHT -> {
                vibrator.vibrate(rightVibrate)
                Log.d(TAG, "Vibrate on $direction at $amplitude severity")
            }
            Direction.BOTH -> {
                vibrator.vibrate(bothVibrate)
                Log.d(TAG, "Vibrate on $direction at $amplitude severity")
            }
        }
    }

}