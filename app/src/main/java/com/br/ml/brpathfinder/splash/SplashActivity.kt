package com.br.ml.brpathfinder.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.br.ml.brpathfinder.MainActivity
import com.br.ml.brpathfinder.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val videoHolder = VideoView(this)
            setContentView(videoHolder)
            val video: Uri =
                Uri.parse("android.resource://" + packageName + "/" + R.raw.splash)
            videoHolder.apply {
                setVideoURI(video)
                setOnCompletionListener { jump() }
                start()
            }
        } catch (ex: Exception) {
            jump()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        jump()
        return true
    }

    private fun jump() {
        if (isFinishing) return
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}