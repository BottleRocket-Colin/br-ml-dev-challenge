package com.br.ml.brpathfinder


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import kotlinx.android.synthetic.main.main_activity.*


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
        setSupportActionBar(my_toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onSupportNavigateUp()
            = findNavController(R.id.nav_host_fragment).popBackStack()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

}
