package com.br.ml.brpathfinder.ui.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.Pose
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


class ArcoreFragment : Fragment(), Scene.OnUpdateListener {

    private lateinit var arFragment: ArFragment
    private var currentAnchorNode: AnchorNode? = null
    private val tvDistance: TextView? = null
    var cubeRenderable: ModelRenderable? = null
    private var currentAnchor: Anchor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_arcore, container, false)
    }

    private fun initModel() {
        MaterialFactory.makeTransparentWithColor(this, Color(Color.RED))
            .thenAccept { material: Material? ->
                val vector3 = Vector3(0.05f, 0.01f, 0.01f)
                cubeRenderable = ShapeFactory.makeCube(vector3, Vector3.zero(), material)
                cubeRenderable.setShadowCaster(false)
                cubeRenderable.setShadowReceiver(false)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        arFragment = childFragmentManager.findFragmentById(R.id.sceneformFragment) as ArFragment

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            distance.text = hitResult.distance.toString()

            // Creating Anchor.
            // Creating Anchor.
            val anchor: Anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            clearAnchor()

            currentAnchor = anchor
            currentAnchorNode = anchorNode


            val node = TransformableNode(arFragment.transformationSystem)
            node.renderable = cubeRenderable
            node.setParent(anchorNode)
            arFragment.arSceneView.scene.addOnUpdateListener(this)
            arFragment.arSceneView.scene.addChild(anchorNode)
            node.select()
        }
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

    override fun onUpdate(frameTime: FrameTime?) {
        val frame: Frame? = arFragment.arSceneView.arFrame
        Log.d("API123", "onUpdateframe... current anchor node " + (currentAnchorNode == null))
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
            /*float[] distance_vector = currentAnchor.getPose().inverse()
                    .compose(cameraPose).getTranslation();
            float totalDistanceSquared = 0;
            for (int i = 0; i < 3; ++i)
                totalDistanceSquared += distance_vector[i] * distance_vector[i];*/
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ArcoreFragment()
    }
}