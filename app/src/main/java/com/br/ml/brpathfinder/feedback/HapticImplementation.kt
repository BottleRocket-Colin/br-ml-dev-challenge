package com.br.ml.brpathfinder.feedback

import android.app.Activity
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.br.ml.brpathfinder.models.Direction
import com.br.ml.brpathfinder.settings.SettingsFragment
import com.br.ml.brpathfinder.utils.preferences.PreferencesImplementation

class HapticImplementation(val activity: Activity) : FeedbackInterface {
    private val FEEDBACK_LENGTH_A = 90L
    private val FEEDBACK_LENGTH_B = 190L
    private val FEEDBACK_LENGTH_OFF = 45L
    private val TAG = "HapticImplementation"

    // These can be changed easily
    // TODO - Adjust this to be easier for user to recognize, stay below 333 millis total cycle time.
    private val leftVibratePattern =
        longArrayOf(FEEDBACK_LENGTH_B, FEEDBACK_LENGTH_OFF, FEEDBACK_LENGTH_A)
    private val rightVibratePattern =
        longArrayOf(FEEDBACK_LENGTH_A, FEEDBACK_LENGTH_OFF, FEEDBACK_LENGTH_B)
    private val bothVibratePattern =
        longArrayOf(FEEDBACK_LENGTH_A, FEEDBACK_LENGTH_OFF, FEEDBACK_LENGTH_A)

    val preferences = PreferencesImplementation(activity.applicationContext)

    override fun signalUser(direction: Direction, severity: Float, position: Float) {
        // check if user has enabled vibration in the settings before proceeding
        if (preferences.currentFeedbackMode == SettingsFragment.FeedbackOption.BOTH.saveKey ||
            preferences.currentFeedbackMode == SettingsFragment.FeedbackOption.VIBRATE.saveKey)
        {
            if (severity > .25)
                performVibrate(activity.applicationContext, direction, severity)
        }
    }

    private fun performVibrate(context: Context, direction: Direction, severity: Float) {
        // Vibration effects takes an Int
        val amplitude = (severity * 255).toInt().let { intArrayOf(it, 0, it) }
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