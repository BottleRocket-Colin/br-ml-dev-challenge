package com.br.ml.brpathfinder.feedback

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.models.Direction
import com.br.ml.brpathfinder.settings.convertToAlertTone
import com.br.ml.brpathfinder.utils.preferences.PreferencesImplementation

class SoundImplementation(private val activity: Activity) : FeedbackInterface {
    private val TAG = "SoundImplementation"
    val preferences = PreferencesImplementation(activity.applicationContext)

    /*
    If the position is below .5 the left ear will be 100% volume and the right ear will be 2x the position
    basically making it a 10% volume reduction for ever .05 lower from .5 the position is
    If the position is above .5 the left ear will be 1-position x2 and right ear will be 100p
    (Ex. - .15 = L 100, R 30
         - .40 = L 100, R 80
         - .65 = L 70, R 100
         - .85 = L 30, R 100 )
    Then we take these and multiply by severity to give the user a sense of sonar
    Finally in the performSound() function we add a pitch based of how far the position is away from .5F
    * */
    override fun signalUser(direction: Direction, severity: Float, position: Float) {
        val leftSide: Float
        val rightSide: Float
        val pitch: Float?

        @Suppress("DEPRECATION")
        if (position in 0..1) {
            when {
                position > .5 -> {
                    leftSide = (((1 - position) * 2) * severity)
                    rightSide = severity
                    pitch = if (preferences.pitchAdjustModeActive && !preferences.noHeadphoneModeActive) position - .49F else null
                }
                position < .5 -> {
                    leftSide = severity
                    rightSide = ((position * 2) * severity)
                    pitch = if (preferences.pitchAdjustModeActive && !preferences.noHeadphoneModeActive) .51F - position else null
                }
                else -> {
                    leftSide = severity
                    rightSide = severity
                    pitch = null
                }
            }
        } else {
            leftSide = 0f
            rightSide = 0f
            pitch = null
        }

        performSound(
            activity.applicationContext,
            leftSide,
            rightSide,
            pitch
        )
    }

    private fun performSound(
        context: Context,
        leftSide: Float,
        rightSide: Float,
        pitch: Float?
    ) {
        val preferences = PreferencesImplementation(context)
        if (preferences.noHeadphoneModeActive) {
            val mediaPlayer: MediaPlayer
            val params = PlaybackParams()
            when {
                leftSide > rightSide -> {
                    mediaPlayer = MediaPlayer.create(context, R.raw.alert_beep2)
                    mediaPlayer.setVolume(leftSide, rightSide)
                }
                leftSide < rightSide -> {
                    mediaPlayer = MediaPlayer.create(context, R.raw.alert_beep3)
                    mediaPlayer.setVolume(leftSide, rightSide)
                }
                else -> {
                    mediaPlayer = MediaPlayer.create(context, R.raw.alert_beep)
                }
            }
            if (pitch != null && pitch > 0.0F && pitch < 1.0F ) {
                params.pitch = pitch
                mediaPlayer.playbackParams = params
            }
            mediaPlayer.start()
        } else {
            val mediaPlayer = MediaPlayer.create(
                context,
                preferences.currentAlertToneSaveKey.convertToAlertTone().soundFile
            )
            val params = PlaybackParams()
            if (pitch != null && pitch > 0.0F && pitch < 1.0F) {
                params.pitch = pitch
                mediaPlayer.playbackParams = params
            }
            mediaPlayer.setVolume(leftSide, rightSide)
            mediaPlayer.start()
        }
    }
}