package com.br.ml.brpathfinder.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.br.ml.brpathfinder.MainActivity
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.common.FragmentName
import kotlinx.android.synthetic.main.activity_landing.*

class LandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        arfragLoadButton.setOnClickListener {
            startActivity(MainActivity.newIntent(this, FragmentName.AR_FRAGMENT))
        }

        mlfragLoadButton.setOnClickListener {
            startActivity(MainActivity.newIntent(this, FragmentName.ML_FRAGMENT))
        }

        depthButton.setOnClickListener {
            startActivity(MainActivity.newIntent(this, FragmentName.DEPTH_FRAGMENT))
        }

        settingsLoadButton.setOnClickListener {
            startActivity(MainActivity.newIntent(this, FragmentName.SETTINGS))
        }
    }
}
