package com.br.ml.brpathfinder.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.databinding.FragmentMainBinding
import com.br.ml.brpathfinder.feedback.HapticImplementation
import com.br.ml.brpathfinder.feedback.SoundImplementation


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
            try {
                viewModel?.modelFile = context?.assets?.open("depth_trained30_quant_f16.tflite")?.readBytes()
            } catch (e: OutOfMemoryError) {
                activity?.let {
                    AlertDialog.Builder(it)
                }?.setMessage(R.string.oom_message)
                    ?.setTitle(R.string.oom_title)
                    ?.setPositiveButton(android.R.string.ok) { _, _ -> activity?.finish() }
                    ?.create()?.show()
            }
        }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        activity?.let { viewModel.feedbacks.add(HapticImplementation(it)) }
        activity?.let { viewModel.feedbacks.add(SoundImplementation(it)) }

        viewModel.analyzedDimens.observe(viewLifecycleOwner, Observer { dimens ->
//            overlay.imageWidth = dimens.first
//            overlay.imageHeight = dimens.second
        })

        viewModel.mlDrawable.observe(viewLifecycleOwner, Observer {
//            mlkit_image.setImageDrawable(it)
        })

        viewModel.tfDrawable.observe(viewLifecycleOwner, Observer {
//            tflite_image.setImageDrawable(it)
        })

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                // TODO - Show rationale
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraPermissionCode)
            }
        } else {
            startCamera()
        }
    }

    override fun onResume() {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onResume()
    }

    override fun onPause() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    private fun startCamera() {
        /*preview.setOnPreviewOutputUpdateListener { previewOutput ->
            cameraTextureView.surfaceTexture = previewOutput.surfaceTexture
        }*/

        CameraX.bindToLifecycle(viewLifecycleOwner, viewModel.imageAnalysis)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            cameraPermissionCode -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d("MainFrag", "Starting Camera")
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
