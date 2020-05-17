package com.br.ml.brpathfinder.feedback

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.models.Direction
import com.br.ml.brpathfinder.settings.convertToAlertTone
import com.br.ml.brpathfinder.utils.preferences.PreferencesImplementation

class SoundImplementation(val activity: Activity) : FeedbackInterface {
    private val TAG = "SoundImplementation"

    /*
    If the position is below .5 the left ear will be 100% volume and the right ear will be 2x the position
    basically making it a 10% volume reduction for ever .05 lower from .5 the position is
    If the position is above .5 the left ear will be 1-position x2 and right ear will be 100p
    (Ex. - .15 = L 100, R 30
         - .40 = L 100, R 80
         - .65 = L 70, R 100
         - .85 = L 30, R 100
    * */
    override fun signalUser(direction: Direction, severity: Float, position: Float) {
        Log.d("ere signalUser", "Direction: $direction, Severity: $severity, Position $position")
        val leftSide: Float
        val rightSide: Float
        val noHeadphoneModeAlertTone = 0

        @Suppress("DEPRECATION")
        if (position in 0..1) {
            when {
                position > .5 -> {
                    leftSide = (((1 - position) * 2) * severity)
                    rightSide = severity
                }
                position < .5 -> {
                    leftSide = severity
                    rightSide = ((position * 2) * severity)
                }
                else -> {
                    leftSide = severity
                    rightSide = severity
                }
            }
        } else {
            leftSide = 0f
            rightSide = 0f
        }

        performSound(activity.applicationContext, leftSide, rightSide, noHeadphoneModeAlertTone)
    }

    private fun performSound(
        context: Context,
        leftSide: Float,
        rightSide: Float,
        noHeadphoneModeAlertTone: Int
    ) {
        val preferences = PreferencesImplementation(context)
        Log.d(
            "ere performSound",
            "leftSide: $leftSide, rightSide: $rightSide, noHeadphoneModeAlertTone $noHeadphoneModeAlertTone"
        )
        if (preferences.noHeadphoneModeActive) {
            val mediaPlayer: MediaPlayer
            when {
                leftSide > rightSide -> {
                    mediaPlayer = MediaPlayer.create(context, R.raw.piano_left)
                    mediaPlayer.setVolume(leftSide, rightSide)
                }
                leftSide < rightSide -> {
                    mediaPlayer = MediaPlayer.create(context, R.raw.piano_right)
                    mediaPlayer.setVolume(leftSide, rightSide)
                }
                else -> {
                    mediaPlayer = MediaPlayer.create(context, R.raw.piano_center)
                }
            }
            mediaPlayer.start()
        } else {
            val mediaPlayer = MediaPlayer.create(
                context,
                preferences.currentAlertToneSaveKey.convertToAlertTone().soundFile
            )
            mediaPlayer.setVolume(leftSide, rightSide)
            mediaPlayer.start()
        }
    }
}