package com.br.ml.brpathfinder.feedback

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.models.Direction
import com.br.ml.brpathfinder.settings.SettingsFragment.Companion.pullAlertToneFromSharedPreferences
import com.br.ml.brpathfinder.settings.SettingsFragment.Companion.pullNoHeadphonesModeFromSharedPreferences

class SoundImplementation(val activity: Activity) : FeedbackInterface {
    private val TAG = "SoundImplementation"

    override fun signalUser(direction: Direction, severity: Float) {
        if (severity > .25) performSound(activity.applicationContext, direction, severity)
    }

    fun performSound(context: Context, direction: Direction, severity: Float) {
        if (!pullNoHeadphonesModeFromSharedPreferences(activity)) {
            // No headphones mode is turned OFF, play the sounds only on the side of incoming collision
            // Create a media player with the users chosen sound
            val mediaPlayer = MediaPlayer.create(context, pullAlertToneFromSharedPreferences(activity).soundFile)
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
        } else {
            // No Headphone Mode is turned on
            val leftMediaPlayer = MediaPlayer.create(context, R.raw.alert_beep)
            val centerMediaPlayer = MediaPlayer.create(context, R.raw.alert_jazz)
            val rightMediaPlayer = MediaPlayer.create(context, R.raw.alert_snippy)
            when (direction) {
                Direction.LEFT -> {
                    leftMediaPlayer.setVolume(severity, severity)
                    leftMediaPlayer.start()
                    Log.d(TAG, "No Headphone Mode Sound on $direction side at $severity severity")
                }
                Direction.BOTH -> {
                    centerMediaPlayer.setVolume(severity, severity)
                    centerMediaPlayer.start()
                    Log.d(TAG, "No Headphone Mode Sound on $direction side at $severity severity")
                }
                Direction.RIGHT -> {
                    rightMediaPlayer.setVolume(severity, severity)
                    rightMediaPlayer.start()
                    Log.d(TAG, "No Headphone Mode Sound on $direction side at $severity severity")
                }
            }
        }
    }
}