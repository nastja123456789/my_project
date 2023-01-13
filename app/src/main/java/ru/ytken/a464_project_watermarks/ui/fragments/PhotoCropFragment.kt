package ru.ytken.a464_project_watermarks.ui.fragments
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.ContourDetector
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.process.ImageProcessor
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui.view.edit.CroppingActivity
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration
import kotlinx.android.synthetic.main.fragment_photo_crop.*
import ru.ytken.a464_project_watermarks.R
import java.io.ByteArrayOutputStream
import java.util.*

internal class PhotoCropFragment : Fragment(R.layout.fragment_photo_crop) {

    private lateinit var imageProcessor: ImageProcessor
    private lateinit var contourDetector: ContourDetector

    private var selectedImage: Bitmap? = null
    private lateinit var cropping: CroppingConfiguration
    private lateinit var pageFileStorage: PageFileStorage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener("fromButtonToCrop") {
                _ , bun ->
            val str = bun.getString("uri")
            val uri = Uri.parse(
                str
            )
            selectedImage = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            resultImageView.setImageBitmap(selectedImage)
        }
        imageButtonCloseCrop.visibility = View.INVISIBLE
        if (selectedImage == null) {
            Toast.makeText(context, "Загрузите файл!", Toast.LENGTH_SHORT).show()
            cropButton.visibility = View.INVISIBLE
            saveButton.visibility = View.INVISIBLE
            imageButtonCloseCrop.visibility = View.VISIBLE
            resultImageView.setImageBitmap(null)
        }
        else {
            createSDK()
            resultImageView.visibility = View.VISIBLE
            resultImageView.setImageBitmap(selectedImage)
        }
        cropButton.setOnClickListener {
                crop()
        }
        saveButton.setOnClickListener {
            if (selectedImage!=null) {
                findNavController().navigate(R.id.action_photoCropFragment_to_imageResultFragment)
            } else {
                findNavController().popBackStack()
            }
        }

        imageButtonCloseCrop.setOnClickListener {
            //findNavController().popBackStack()
            finishAffinity(requireActivity())
        }
    }

    private fun createSDK() {
        val scanbotSDK = ScanbotSDK(context!!)
        pageFileStorage = scanbotSDK.createPageFileStorage()
        contourDetector = scanbotSDK.createContourDetector()
        imageProcessor = scanbotSDK.imageProcessor()
    }

    private fun crop() {
            if (selectedImage!=null) {
                val pageId = pageFileStorage.add(selectedImage!!)

                val page = Page(pageId, emptyList(), DetectionStatus.OK, ImageFilterType.NONE)

                try {
                    cropping = CroppingConfiguration(page)
                    resultLauncher.launch(cropping)
                } catch (e: RuntimeException) {
                    findNavController().popBackStack()
                    Toast.makeText(context, "sorry, session has been done", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(context, "Загрузите другой файл!", Toast.LENGTH_SHORT).show()
                cropButton.visibility = View.INVISIBLE
                saveButton.visibility = View.INVISIBLE
            }
    }

    private var resultLauncher = registerForActivityResultOk(CroppingActivity.ResultContract()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.resultOk) {
                val page: String = result.result!!.pageId
                val image: Bitmap? = pageFileStorage.getImage(page, PageFileStorage.PageFileType.DOCUMENT, BitmapFactory.Options())
                //vm.setInitImage(image!!)
                val bytes = ByteArrayOutputStream()
                image!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                val path: String = MediaStore.Images.Media.insertImage(
                    requireActivity().contentResolver,
                    image,
                    "IMG_" + Calendar.getInstance().time,
                    null
                )
                val uri = Uri.parse(path)
                setFragmentResult("fromCropToImage", bundleOf("uri" to uri.toString()))
            }
            findNavController().navigate(R.id.action_photoCropFragment_to_imageResultFragment)
        }
    }
}