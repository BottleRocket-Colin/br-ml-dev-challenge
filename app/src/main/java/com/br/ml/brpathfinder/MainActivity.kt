package com.br.ml.brpathfinder

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.br.ml.brpathfinder.common.FragmentName
import com.br.ml.brpathfinder.settings.SettingsFragment
import com.br.ml.brpathfinder.ui.main.ArcoreFragment
import com.br.ml.brpathfinder.ui.main.DepthFragment
import com.br.ml.brpathfinder.ui.main.MainFragment

/*
    Notes:

   Landing page:
    - simple act w/ buttons to launch other activities
    - future, nav components and bottom nav

   Techniques for gathering frame data:
    - Ar Core - depth library
      -- point cloud
    - ML Kit - no depth
    - Dual Camera solution - bokeh example from io (need link)
    - Face detection - human faces are roughly equal in size
    - Accelerometer - Use accel to determine device movement and trig to compute distance
    - Camera TOF sensor - depends on vendor APIs
    - dual images, single sensor - needs research

   Priority:
    - ML Kit - no depth
    - Face Detection
    - AR core
    - Accelerometer w/ trig
    - Dual camera, TOF, dual images - single sensor

   Frame data:
     - Sensors: accel and gyro, compass
     - Timestamp
     - Bounding boxes
        --- Hopefully have depth per box.

   Techniques for evaluating frame data
     - Algorithmic
     - ML model custom trained (we have hammer, is there a nail ??? )
        - Prepare training data, model, prediction
     - ????

   Priority:
     - Model/Interface
     - Algo - Primary
     - ML model - BACKUP only
      - Concerns with collecting data
      - QR codes as known object and capturing a lot of video.

   Risk data:
     - box id
     - direction
     - severity
     - ??

   Techniques for feedback
     - Model/Interface
     - Haptic
     - Audio
     - ??

   Priority:
     - Haptic
     - Audio

   Settings:
     - Feedback config
     - selecting techniques to use
     - ???


   Timeline:
     - Early march: ML Kit (no depth) -> algo -> haptic
     - Mid March: Styled settings
     - Late March/Early April - on boarding and fully accessible UI
     - Start of April checkpoint: moar!, ML model, new depth techniques


   Open action items:
     - Email to Christopher about AR Core depth library


   Mid-term goals:
     - on-boarding
     - accessible UI
     - ???

    Tasks:
     - Colin: Arch, Algo
     - Sam: AR Core (hold for depth), Face detection
     - Eric: Feedback, settings
     - Jing: on boarding ??
     - Luke: Gary!

 */

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val fragmentName = intent.extras
        val frag = fragmentName?.getSerializable(INTENT_FRAGMENT_NAME) as FragmentName
        when (frag) {
            FragmentName.AR_FRAGMENT -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ArcoreFragment.newInstance())
                    .commitNow()
            }
            FragmentName.ML_FRAGMENT -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
            }

            FragmentName.DEPTH_FRAGMENT -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, DepthFragment.newInstance())
                    .commitNow()
            }
            FragmentName.SETTINGS -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, SettingsFragment.newInstance())
                    .commitNow()
            }
        }
    }

    companion object {

        private val INTENT_FRAGMENT_NAME = "frag_name"

        fun newIntent(context: Context, fragName: FragmentName?): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(INTENT_FRAGMENT_NAME, fragName)
            return intent
        }
    }

}
