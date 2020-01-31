package com.br.ml.brpathfinder

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.br.ml.brpathfinder.ui.settings.SettingsFragment

interface FeedbackInterface {

    enum class Direction { LEFT, RIGHT }

    private val FEEDBACK_LENGTH_A: Long
        get() = 100
    private val FEEDBACK_LENGTH_B: Long
        get() = 300

    private val TAG: String
        get() = "FeedbackInterface"

    fun signalUser(context: Context, direction: Direction, severity: Float) {
        if (severity > 1.0 || severity < 0.0) throw IllegalArgumentException()

        when (getFeedbackType(context)) {
            SettingsFragment.FeedbackType.VIBRATE -> {
                // Vibrate
                performVibrate(context, direction, severity)
            }
            SettingsFragment.FeedbackType.SOUND -> {
                // Sound
                performSound(context, direction, severity)
            }
            SettingsFragment.FeedbackType.BOTHVIBRATEANDSOUND -> {
                // Both vibrate and sound
                performBothVibrateAndSound(context, direction, severity)
            }
            else -> { // NoFeedback selected
                // Do nothing
                performNoFeedback(direction, severity)
            }
        }
    }

    /*
        Get the selected feedback type from shared prefs
    * */
    fun getFeedbackType(context: Context): SettingsFragment.FeedbackType {
        val sharedPrefs = context.getSharedPreferences(
            context.getString(R.string.shared_prefs_tag),
            Context.MODE_PRIVATE
        )
        val feebackType = sharedPrefs.getInt("FEEDBACK_TYPE", 1)
        Log.d("feedback_type", feebackType.toString())

        return when (feebackType) {
            1 -> {
                SettingsFragment.FeedbackType.VIBRATE
            }
            2 -> SettingsFragment.FeedbackType.SOUND
            3 -> SettingsFragment.FeedbackType.BOTHVIBRATEANDSOUND
            else -> SettingsFragment.FeedbackType.NOFEEDBACK
        }
    }

    /*
        Perform the vibration based on the given direction and severity
    * */
    private fun performVibrate(context: Context, direction: Direction, severity: Float) {
        // Vibration effects takes an Int
        val amplitude = intArrayOf(severity.toInt())
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // These can be changed easily
        val leftVibratePattern =
            longArrayOf(FEEDBACK_LENGTH_B, FEEDBACK_LENGTH_A, FEEDBACK_LENGTH_B)
        val rightVibratePattern =
            longArrayOf(FEEDBACK_LENGTH_A, FEEDBACK_LENGTH_B, FEEDBACK_LENGTH_A)

        val leftVibrate = VibrationEffect.createWaveform(leftVibratePattern, amplitude, -1)
        val rightVibrate = VibrationEffect.createWaveform(rightVibratePattern, amplitude, -1)

        if (direction == Direction.LEFT) {
            vibrator.vibrate(leftVibrate)
            Log.d(TAG, "Vibrate on $direction at $amplitude severity")
        } else if (direction == Direction.RIGHT) {
            vibrator.vibrate(rightVibrate)
            Log.d(TAG, "Vibrate on $direction at $amplitude severity")
        }
    }

    /*
        Perform the sound based on the given direction and severity
    * */
    private fun performSound(context: Context, direction: Direction, severity: Float) {
        // Right now this is just a notification alert, but will change eventually to something we
        // want
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        val audioFile = R.raw.scream
        val soundId: Int = soundPool.load(context, audioFile, 1)
        when (direction) {
            Direction.LEFT -> {
                soundPool.play(soundId, severity, 0F, 1, 1, 1f)
                Log.d(TAG, "Sound on $direction side at $severity severity")
            }
            Direction.RIGHT -> {
                soundPool.play(soundId, 0F, severity, 1, 1, 1f)
                Log.d(TAG, "Sound on $direction side at $severity severity")
            }
        }
    }

    /*
        Perform both the vibration and the sound
    * */
    fun performBothVibrateAndSound(context: Context, direction: Direction, severity: Float) {
        performVibrate(context, direction, severity)
        performSound(context, direction, severity)
    }

    /*
        This is just  a logging function
    * */
    fun performNoFeedback(direction: Direction, severity: Float) {
        Log.d(TAG, "Object moving $direction with a $severity severity")
    }
}