package ru.ytken.a464_project_watermarks.ui.fragments
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.ContourDetector
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.core.contourdetector.Line2D
import io.scanbot.sdk.process.CropOperation
import io.scanbot.sdk.process.ImageProcessor
import kotlinx.android.synthetic.main.fragment_photo_crop.*
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.ui.MainViewModel
import java.util.concurrent.Executors
import kotlin.math.roundToInt

internal class PhotoCropFragment : Fragment(R.layout.fragment_photo_crop) {

    private val vm: MainViewModel by activityViewModels()

    private lateinit var originalBitmap: Bitmap

    private lateinit var imageProcessor: ImageProcessor
    private lateinit var contourDetector: ContourDetector
    private var lastRotationEventTs = 0L
    private var rotationDegrees = 0

    private var selectedImage: Bitmap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedImage = vm.initImage.value
        val scanbotSDK = ScanbotSDK(context!!)
        contourDetector = scanbotSDK.createContourDetector()
        imageProcessor = scanbotSDK.imageProcessor()
        resultImageView.visibility = View.VISIBLE
        polygonView.visibility = View.VISIBLE
        rotateButton.visibility = View.VISIBLE

        rotateButton.setOnClickListener {
            rotatePreview()
        }
        cropButton.setOnClickListener {
            crop()
        }
        saveButton.setOnClickListener {
            findNavController().navigate(R.id.action_photoCropFragment_to_imageResultFragment)
        }

        imageButtonCloseCrop.setOnClickListener {
            findNavController().popBackStack()
        }

        InitImageViewTask().executeOnExecutor(Executors.newSingleThreadExecutor())
    }

    private fun crop() {

        var documentImage = imageProcessor.processBitmap(originalBitmap, CropOperation(polygonView.polygon), false)
        documentImage?.let {
            if (rotationDegrees > 0) {
                // rotate the final cropped image result based on current rotation value:
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                documentImage = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
            }

            polygonView.polygon = contourDetector.detect(documentImage!!)!!.polygonF
            polygonView.setLines(contourDetector.detect(documentImage!!)!!.horizontalLines, contourDetector.detect(documentImage!!)!!.verticalLines)
            resultImageView.setImageBitmap(resizeForPreview(documentImage!!))
        }
    }

    private fun resizeForPreview(bitmap: Bitmap): Bitmap {
        val maxW = 1000f
        val maxH = 1000f
        val oldWidth = bitmap.width.toFloat()
        val oldHeight = bitmap.height.toFloat()
        val scaleFactor = if (oldWidth > oldHeight) maxW / oldWidth else maxH / oldHeight
        val scaledWidth = (oldWidth * scaleFactor).roundToInt()
        val scaledHeight = (oldHeight * scaleFactor).roundToInt()
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false)
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false)
    }

    private fun rotatePreview() {
        if (System.currentTimeMillis() - lastRotationEventTs < 350) {
            return
        }
        rotationDegrees += 90
        polygonView.rotateClockwise()
        lastRotationEventTs = System.currentTimeMillis()
    }

    internal inner class InitImageViewTask : AsyncTask<Void?, Void?, InitImageResult>() {
        private var previewBitmap: Bitmap? = null

        override fun doInBackground(vararg params: Void?): InitImageResult {
            originalBitmap = selectedImage!!
            previewBitmap = resizeForPreview(originalBitmap)

            val result = contourDetector.detect(originalBitmap)
            return when (result?.status) {
                DetectionStatus.OK,
                DetectionStatus.OK_BUT_BAD_ANGLES,
                DetectionStatus.OK_BUT_TOO_SMALL,
                DetectionStatus.OK_BUT_BAD_ASPECT_RATIO
                -> {
                    val linesPair = Pair(result.horizontalLines, result.verticalLines)
                    val polygon = result.polygonF

                    InitImageResult(linesPair, polygon)
                }
                else -> InitImageResult(Pair(listOf(), listOf()), listOf())
            }
        }
        override fun onPostExecute(initImageResult: InitImageResult) {
            polygonView.setImageBitmap(previewBitmap)
            magnifier.setupMagnifier(polygonView)

            // set detected polygon and lines into EditPolygonImageView
            polygonView.polygon = initImageResult.polygon
            polygonView.setLines(initImageResult.linesPair.first, initImageResult.linesPair.second)
        }
    }

    internal inner class InitImageResult(val linesPair: Pair<List<Line2D>, List<Line2D>>, val polygon: List<PointF>)
}