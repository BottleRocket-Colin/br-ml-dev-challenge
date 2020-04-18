package com.br.ml.brpathfinder.feedback

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.models.Direction

class SoundImplementation(val context: Context) : FeedbackInterface {
    private val TAG = "SoundImplementation"

    override fun signalUser(direction: Direction, severity: Float) {
        if (severity > .25) performSound(context, direction, severity)
    }

    fun performSound(context: Context, direction: Direction, severity: Float) {
        // Create a media player with the beep sound
        val mediaPlayer = MediaPlayer.create(context, R.raw.beep_sound)
        when (direction) {
            Direction.LEFT -> {
                // set the volume to be the severity in the left side and silent in the right side
                mediaPlayer.setVolume(severity, 0f)
                // play the beep sound
                mediaPlayer.start()
                Log.d(TAG, "Sound on $direction side at $severity severity")
            }
            Direction.RIGHT -> {
                // Set the volume as silent in the left side and the level of the severity on the right side
                mediaPlayer.setVolume(0f, severity)
                mediaPlayer.start()
                Log.d(TAG, "Sound on $direction side at $severity severity")
            }
            Direction.BOTH -> {
                // Play both sides at the volume of the severity
                mediaPlayer.setVolume(severity, severity)
                mediaPlayer.start()
                Log.d(TAG, "Sound on $direction side at $severity severity")
            }
        }
    }
}