package com.br.ml.brpathfinder.ui.main


import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

        var bitmap =  BitmapFactory.decodeResource(resources, R.drawable.test3)
        val fireBaseLocalModelSource = FirebaseCustomLocalModel.Builder().setAssetFilePath("depth.tflite").build()
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
                val probabilities = output[0][0][0]

                val poop = output[0][0][0]

                //val out = Bitmap.createBitmap(240, 320, Bitmap.Config.ARGB_8888)
                //out.setPixels(output[0][0][0], 0, 240, 0,0, 240, 320)
                val byteBuf = ByteBuffer.allocate(4 * output[0][0][0].size)
                val floatBuf = byteBuf.asFloatBuffer()
                floatBuf.put(poop)
                val byte_array = byteBuf.array()

                val optionsB = BitmapFactory.Options()
                optionsB.inMutable = true
                val bmp = BitmapFactory.decodeByteArray(byte_array, 0, byte_array.size, optionsB)

                img.setImageBitmap(bmp)
                Log.d("output", probabilities.toString())
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
