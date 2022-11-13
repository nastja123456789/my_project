package ru.ytken.a464_project_watermarks.ui.fragments
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.ContourDetector
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.view.edit.CroppingActivity
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
import kotlinx.android.synthetic.main.fragment_photo_crop.*
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.ui.MainViewModel

internal class PhotoCropFragment : Fragment(R.layout.fragment_photo_crop) {

    private val vm: MainViewModel by activityViewModels()

    private lateinit var imageProcessor: ImageProcessor
    private lateinit var contourDetector: ContourDetector

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
        resultImageView.visibility = View.VISIBLE
        resultImageView.setImageBitmap(selectedImage)
        cropButton.setOnClickListener {
            val pageId = pageFileStorage.add(selectedImage!!)
            var page = Page(pageId, emptyList(), DetectionStatus.OK, ImageFilterType.NONE)
            cropping = CroppingConfiguration(page)
            val intent = CroppingActivity.newIntent(context!!, cropping)
            startActivityForResult(intent, CROP_UI_REQUEST_CODE_CONSTANT)
        }
        saveButton.setOnClickListener {
            findNavController().navigate(R.id.action_photoCropFragment_to_imageResultFragment)
        }

        imageButtonCloseCrop.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_UI_REQUEST_CODE_CONSTANT) {
            val result = CroppingActivity.extractResult(resultCode, data)!!
            if (result.resultOk) {
                val page: String? = result.result!!.pageId
                val image: Bitmap? = pageFileStorage.getImage(page!!, PageFileStorage.PageFileType.DOCUMENT, BitmapFactory.Options())
                vm.setInitImage(image!!)
            }
            findNavController().navigate(R.id.action_photoCropFragment_to_imageResultFragment)
        }
    }

    companion object {
        val CROP_UI_REQUEST_CODE_CONSTANT = 100
    }
}