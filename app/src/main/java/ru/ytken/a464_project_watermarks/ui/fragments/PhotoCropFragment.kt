package ru.ytken.a464_project_watermarks.ui.fragments
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.CropOperation
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.view.edit.CroppingActivity
import io.scanbot.sdk.ui.view.edit.configuration.CroppingAccessibilityConfiguration
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
import kotlinx.android.synthetic.main.fragment_image_result.*
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
    private lateinit var cropping: CroppingConfiguration
    private lateinit var pageFileStorage: PageFileStorage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedImage = vm.initImage.value
        val scanbotSDK = ScanbotSDK(context!!)
        pageFileStorage = scanbotSDK.createPageFileStorage()
        contourDetector = scanbotSDK.createContourDetector()
        imageProcessor = scanbotSDK.imageProcessor()
        //val pageId = pageFileStorage.add(selectedImage!!)
        //var page = Page(pageId, emptyList(), DetectionStatus.OK, ImageFilterType.NONE)
        //cropping = CroppingConfiguration(page)

        resultImageView.visibility = View.GONE
        cropButton.setOnClickListener {
            val pageId = pageFileStorage.add(selectedImage!!)
            var page = Page(pageId, emptyList(), DetectionStatus.OK, ImageFilterType.NONE)
            cropping = CroppingConfiguration(page)
            val intent = CroppingActivity.newIntent(context!!, cropping)
            startActivityForResult(intent, CROP_UI_REQUEST_CODE_CONSTANT)
            //crop()
        }
        rotateButton.setOnClickListener {
            rotatePreview()
        }
        backButton.setOnClickListener {
            backButton.visibility = View.GONE
            resultImageView.visibility = View.GONE
            polygonView.visibility = View.VISIBLE
            cropButton.visibility = View.VISIBLE
            rotateButton.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
        }

//        rotateButton.setOnClickListener {
//            rotatePreview()
//        }
//        cropButton.setOnClickListener {
//            crop()
//        }
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

            polygonView.visibility = View.GONE
            cropButton.visibility = View.GONE
            rotateButton.visibility = View.GONE
            resultImageView.setImageBitmap(resizeForPreview(documentImage!!))
            resultImageView.visibility = View.VISIBLE
            backButton.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
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
    }

    private fun rotatePreview() {
        if (System.currentTimeMillis() - lastRotationEventTs < 350) {
            return
        }
        rotationDegrees += 90
        polygonView.rotateClockwise()
        lastRotationEventTs = System.currentTimeMillis()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_UI_REQUEST_CODE_CONSTANT) {
            val result = CroppingActivity.extractResult(resultCode, data)!!
            if (result.resultOk) {
                val page: String? = result.result!!.pageId
                val image: Bitmap? = pageFileStorage.getImage(page!!, PageFileStorage.PageFileType.DOCUMENT, BitmapFactory.Options())
                vm.setInitImage(image!!)
                //imageViewResultImage.setImageBitmap(image)
                //imageViewResultImage.visibility = View.VISIBLE
            }
            findNavController().navigate(R.id.action_photoCropFragment_to_imageResultFragment)
        }
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

    companion object {
        val CROP_UI_REQUEST_CODE_CONSTANT = 100
    }
}