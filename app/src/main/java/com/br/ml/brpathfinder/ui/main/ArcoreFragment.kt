package com.br.ml.brpathfinder.ui.main

import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.br.ml.brpathfinder.R
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.fragment_arcore.*


class ArcoreFragment : Fragment() {

    private lateinit var arFragment: ArFragment
    private var currentAnchorNode: AnchorNode? = null
    private val tvDistance: TextView? = null
    var cubeRenderable: ModelRenderable? = null
    private var currentAnchor: Anchor? = null
    private var isTracking = false
    private var isHitting = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_arcore, container, false)
    }

    /*private fun initModel() {
        MaterialFactory.makeTransparentWithColor(this, Color(Color.RED))
            .thenAccept { material: Material? ->
                val vector3 = Vector3(0.05f, 0.01f, 0.01f)
                cubeRenderable = ShapeFactory.makeCube(vector3, Vector3.zero(), material)
                cubeRenderable.setShadowCaster(false)
                cubeRenderable.setShadowReceiver(false)
            }
    }*/

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        arFragment = childFragmentManager.findFragmentById(R.id.sceneformFragment) as ArFragment

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            distance.text = hitResult.distance.toString()

        }

        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            arFragment.onUpdate(frameTime)
            onUpdate()
        }

        val frame: Frame? = arFragment.arSceneView.session?.update()
        val pointCloud: PointCloud? = frame?.acquirePointCloud()

        Log.d("API123", pointCloud?.points?.get(1).toString())
        button.setOnClickListener { addObject(Uri.parse("andy.sfb")) }
        showFab(false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun addObject(model: Uri) {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        if (frame != null) {
            val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    placeObject(arFragment, hit.createAnchor(), model)
                    break
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri) {
        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenAccept {
                addNodeToScene(fragment, anchor, it)
            }
            .exceptionally {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: ModelRenderable) {
        val anchorNode = AnchorNode(anchor)
// TransformableNode means the user to move, scale and rotate the model
        val transformableNode = TransformableNode(fragment.transformationSystem)
        transformableNode.renderable = renderable
        transformableNode.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }

    private fun clearAnchor() {
        currentAnchor = null
        if (currentAnchorNode != null) {
            arFragment.arSceneView.scene.removeChild(currentAnchorNode)
            currentAnchorNode!!.anchor!!.detach()
            currentAnchorNode!!.setParent(null)
            currentAnchorNode = null
        }
    }

    fun onUpdate() {

        updateTracking()
        if (isTracking) {
            val hitTestChanged = updateHitTest()
            if (hitTestChanged) {
                showFab(isHitting)
            }

        }
/*Log.d("API123", "onUpdateframe... current anchor node " + (currentAnchorNode == null))
if (currentAnchorNode != null) {
    val objectPose = currentAnchor!!.pose
    val cameraPose: Pose? = frame?.getCamera()?.getPose()
    val dx = objectPose.tx() - cameraPose?.tx()!!
    val dy = objectPose.ty() - cameraPose.ty()
    val dz = objectPose.tz() - cameraPose.tz()
    ///Compute the straight-line distance.
    val distanceMeters =
        Math.sqrt(dx * dx + dy * dy + (dz * dz).toDouble()).toFloat()
    distance.text = "Distance from camera: $distanceMeters metres"
    *//*float[] distance_vector = currentAnchor.getPose().inverse()
            .compose(cameraPose).getTranslation();
    float totalDistanceSquared = 0;
    for (int i = 0; i < 3; ++i)
        totalDistanceSquared += distance_vector[i] * distance_vector[i];*//*
}*/
    }

    private fun showFab(enabled: Boolean) {
        if (enabled) {
            button.isEnabled = true
            button.visibility = View.VISIBLE
        } else {
            button.isEnabled = false
            button.visibility = View.GONE
        }
    }

    private fun updateHitTest(): Boolean {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        val hits: List<HitResult>
        val wasHitting = isHitting
        isHitting = false
        if (frame != null) {
            hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    isHitting = true
                    break
                }
            }
        }
        return wasHitting != isHitting
    }

    private fun getScreenCenter(): Point {
        val view = content
        return Point(view.width / 2, view.height / 2)
    }

    private fun updateTracking(): Boolean {
        val frame = arFragment.arSceneView.arFrame
        val wasTracking = isTracking
        isTracking = frame?.camera?.trackingState == TrackingState.TRACKING
        return isTracking != wasTracking
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ArcoreFragment()
    }
}