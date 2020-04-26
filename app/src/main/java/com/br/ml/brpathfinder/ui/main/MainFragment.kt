package com.br.ml.brpathfinder.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.databinding.FragmentMainBinding
import com.br.ml.brpathfinder.settings.SettingsFragment
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    // CameraX
    private val cameraPermissionCode = 12
    private val previewConfig = PreviewConfig.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
        .build()
    private val preview = Preview(previewConfig)

    private lateinit var viewModel: MainViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).apply {
            supportActionBar?.apply {
                this.title = resources.getString(R.string.app_name)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        DataBindingUtil.inflate<FragmentMainBinding>(inflater, R.layout.fragment_main, container, false).apply {
            viewModel = ViewModelProviders.of(this@MainFragment).get(MainViewModel::class.java)
//            viewModel?.modelFile = context?.assets?.open("depth_trained30_quant.tflite")?.readBytes()
            viewModel?.modelFile = context?.assets?.open("depth_trained25.tflite")?.readBytes()
        }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
//        viewModel.feedback = context?.let { HapticImplementation(it) }

        viewModel.analyzedDimens.observe(this, Observer { dimens ->
            overlay.imageWidth = dimens.first
            overlay.imageHeight = dimens.second
        })

        viewModel.mlDrawable.observe(viewLifecycleOwner, Observer {
            mlkit_image.setImageDrawable(it)
        })

        viewModel.tfDrawable.observe(viewLifecycleOwner, Observer {
            tflite_image.setImageDrawable(it)
        })

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                // TODO - Show rationale
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), cameraPermissionCode)
            }
        } else {
            startCamera()
        }
    }


    private fun startCamera() {
        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            cameraTextureView.surfaceTexture = previewOutput.surfaceTexture
        }

        CameraX.bindToLifecycle(this as LifecycleOwner, viewModel.imageAnalysis, preview)
        // TODO - SG - Look at connecting preview to toggle.
        //  it can be unbound using following:
        //        CameraX.unbind(preview)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            cameraPermissionCode -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startCamera()
                } else {
                    // TODO - permission denied, boo!
                }
                return
            }
            else -> { }
        }
    }
}
