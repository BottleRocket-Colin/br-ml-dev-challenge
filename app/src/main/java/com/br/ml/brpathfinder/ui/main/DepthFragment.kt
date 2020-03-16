package com.br.ml.brpathfinder.ui.main


import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.br.ml.brpathfinder.R
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.*
import kotlinx.android.synthetic.main.fragment_depth.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DepthFragment : Fragment() {

    private val intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
    private var imgData: ByteBuffer = ByteBuffer.allocateDirect(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * 3 * 4)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_depth, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val optionsB = BitmapFactory.Options()
        optionsB.inMutable = true
        var bitmap =  BitmapFactory.decodeResource(resources, R.drawable.test5, optionsB)
        val fireBaseLocalModelSource = FirebaseCustomLocalModel.Builder().setAssetFilePath("depth_trained15.tflite").build()
        //Registering the model loaded above with the ModelManager Singleton

        val options = FirebaseModelInterpreterOptions.Builder(fireBaseLocalModelSource).build()
        val interpreter = FirebaseModelInterpreter.getInstance(options)

        val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
            .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 480, 640, 3))
            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 240, 320, 1))
            .build()

        val inputs = FirebaseModelInputs.Builder()
            .add(convertBitmapToByteBuffer(bitmap)) // add() as many input arrays as your model requires
            .build()

        interpreter?.run(inputs, inputOutputOptions)
            ?.addOnSuccessListener {
                //img.setImageDrawable()
                val output = it.getOutput<Array<Array<Array<FloatArray>>>>(0)

                val rectPaint = Paint()

                val canvas = Canvas(bitmap)

                output[0].forEachIndexed { x, row ->
                    row.forEachIndexed { y, pixel ->
                        rectPaint.color = Color.rgb(pixel[0] * 255, 255f, pixel[0] * 255)
                        rectPaint.strokeWidth = 5.0f
                        canvas.drawPoint(y.toFloat() * 2, x.toFloat() * 2, rectPaint)
                        canvas.drawPoint(y.toFloat() * 2, x.toFloat() * 2 + 1, rectPaint)
                        canvas.drawPoint(y.toFloat()* 2 + 1, x.toFloat() * 2, rectPaint)
                        canvas.drawPoint(y.toFloat()* 2 + 1, x.toFloat() * 2 + 1, rectPaint)
                    }
                }

                img.setImageDrawable(BitmapDrawable(resources, bitmap))
            }
            ?.addOnFailureListener {
                //The interpreter failed to identify a Pokemon
            }
    }



    companion object {
        /** Dimensions of inputs.  */
        const val DIM_IMG_SIZE_X = 480
        const val DIM_IMG_SIZE_Y = 640
        const val DIM_BATCH_SIZE = 1
        const val DIM_PIXEL_SIZE = 3
        const val IMAGE_MEAN = 128
        private const val IMAGE_STD = 128.0f

        fun newInstance() = DepthFragment()
    }

    //...
    private fun convertBitmapToByteBuffer(bitmap: Bitmap?): ByteBuffer {
        //Clear the Bytebuffer for a new image
        imgData.rewind()
        imgData.order(ByteOrder.nativeOrder())
        bitmap?.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        // Convert the image to floating point.
        var pixel = 0
        for (i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val currPixel = intValues[pixel++]
                imgData.putFloat(((currPixel shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((currPixel shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((currPixel and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        return imgData
    }
}
