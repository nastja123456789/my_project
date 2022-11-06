package ru.ytken.a464_project_watermarks.ui.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_photo_crop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ytken.a464_project_watermarks.opencv.OpenCvNativeBridge
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.ui.MainViewModel
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

internal class PhotoCropFragment : Fragment(R.layout.fragment_photo_crop) {

    private val vm: MainViewModel by activityViewModels()

    companion object {
        private val TAG = PhotoCropFragment::class.simpleName

        fun newInstance(): PhotoCropFragment {
            return PhotoCropFragment()
        }
    }

    private val nativeClass = OpenCvNativeBridge()

    private var selectedImage: Bitmap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*val pathToImage: String = arguments?.getString(getString(R.string.NAME_BITMAP)).toString()
        val fileToDecode = File(pathToImage)
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888
        bitmapOptions.inSampleSize = 4
        selectedImage = BitmapFactory.decodeStream(FileInputStream(fileToDecode as File), null, bitmapOptions)//long operation, time depends on bitmapOptions.inSampleSize*/
        selectedImage = vm.initImage.value

        if (selectedImage == null) {
            Handler(Looper.getMainLooper()).post{
                closeFragment()
            }
        }
        holderImageView.post {
            if (selectedImage != null) {
                initializeCropping()
            }
        }

        initListeners()
    }

    private fun initListeners() {
        closeButton.setOnClickListener {
            closeFragment()
        }
        confirmButton.setOnClickListener {
            val croppedBitmap = getCroppedImage()
            if (croppedBitmap != null) {
                vm.setScanImage(croppedBitmap)
                findNavController().navigate(R.id.action_imageCropFragment_to_seeScanFragment)
            }
        }
    }

    private fun closeFragment() {
        findNavController().popBackStack()
    }

    private fun initializeCropping() = lifecycleScope.launch(Dispatchers.Main) {
        if(selectedImage != null && selectedImage!!.width > 0 && selectedImage!!.height > 0) {
            val bitmapOptions = BitmapFactory.Options()
            bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmapOptions.inSampleSize = 4

            val outStream = ByteArrayOutputStream()
            selectedImage?.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            val byteArray = outStream.toByteArray()
            val inStream = ByteArrayInputStream(byteArray)
            selectedImage = BitmapFactory.decodeStream(inStream, Rect(), bitmapOptions)

            progressBarScanning.visibility = View.VISIBLE
            imagePreview.setImageBitmap(selectedImage)
            val tempBitmap = (imagePreview.drawable as BitmapDrawable).bitmap
            val pointFs = vm.getAsyncEdgePoints(nativeClass, polygonView, selectedImage!!).await()
            polygonView.setPoints(pointFs)
            polygonView.visibility = View.VISIBLE
            val padding = resources.getDimension(R.dimen.zdc_polygon_dimens).toInt()
            val layoutParams = FrameLayout.LayoutParams(tempBitmap.width + padding, tempBitmap.height + padding)
            layoutParams.gravity = Gravity.CENTER
            polygonView.layoutParams = layoutParams
            progressBarScanning.visibility = View.GONE
        }
    }

    private fun getCroppedImage(): Bitmap? {
        if(selectedImage != null) {
            try {
                val points: Map<Int, PointF> = polygonView.getPoints()
                val xRatio: Float = selectedImage!!.width.toFloat() / imagePreview.width
                val yRatio: Float = selectedImage!!.height.toFloat() / imagePreview.height
                val pointPadding = requireContext().resources.getDimension(R.dimen.zdc_point_padding).toInt()
                val x1: Float = (points.getValue(0).x + pointPadding) * xRatio
                val x2: Float = (points.getValue(1).x + pointPadding) * xRatio
                val x3: Float = (points.getValue(2).x + pointPadding) * xRatio
                val x4: Float = (points.getValue(3).x + pointPadding) * xRatio
                val y1: Float = (points.getValue(0).y + pointPadding) * yRatio
                val y2: Float = (points.getValue(1).y + pointPadding) * yRatio
                val y3: Float = (points.getValue(2).y + pointPadding) * yRatio
                val y4: Float = (points.getValue(3).y + pointPadding) * yRatio

                return nativeClass.getScannedBitmap(selectedImage!!, x1, y1, x2, y2, x3, y3, x4, y4)
            } catch (e: java.lang.Exception) {
            }
        }
        return null
    }
}