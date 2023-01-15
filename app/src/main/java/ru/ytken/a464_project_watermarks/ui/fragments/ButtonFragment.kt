package ru.ytken.a464_project_watermarks.ui.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import kotlinx.android.synthetic.main.fragment_button.*
import ru.ytken.a464_project_watermarks.ImportImageContract
import ru.ytken.a464_project_watermarks.R
import java.io.ByteArrayOutputStream
import java.util.*


class ButtonFragment : Fragment(
    R.layout.fragment_button,
) {
    private var scanbot: ScanbotSDK ?= null
    private var pageFileStorage: PageFileStorage ?= null
    private var pageProcess: PageProcessor ?= null
    private var pageId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createSDK()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stopSDK()

        galleryImageLauncher = registerForActivityResult(ImportImageContract(requireContext())){ resultEntity ->
            if (resultEntity!=null) {
                setFragmentResult(
                    "fromButtonToCrop",
                    bundleOf("uri" to resultEntity.toString())
                )
                findNavController().navigate(R.id.action_buttonFragment_to_photoCropFragment)
            } else {
                reLoadFragment()
            }
        }
        buttonTakePhoto.setOnClickListener {
            takePhoto()
        }

        buttonChoosePhotoFromStorage.setOnClickListener {
            createSDK()
            galleryImageLauncher.launch(Unit)
            buttonChoosePhotoFromStorage.visibility = View.INVISIBLE
            buttonTakePhoto.visibility = View.INVISIBLE
            progressBarWaitForResult.visibility = View.VISIBLE
        }
    }

    private fun createSDK() {
        scanbot = ScanbotSDK(requireActivity())
        pageFileStorage = scanbot!!.createPageFileStorage()
        pageProcess = scanbot!!.createPageProcessor()
    }

    private fun takePhoto() {
        createSDK()
        val cameraConfiguration = DocumentScannerConfiguration()
        resultLauncher.launch(cameraConfiguration)
    }

    private var resultLauncher = registerForActivityResultOk(DocumentScannerActivity.ResultContract()) { result ->
        if (result.resultOk) {
                val snappedPages: List<Page>? = result.result
                pageId = snappedPages?.get(0)?.pageId
                val image = pageFileStorage!!.getImage(pageId!!, PageFileStorage.PageFileType.DOCUMENT, BitmapFactory.Options())
                val bytes = ByteArrayOutputStream()
                image!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                val path: String = MediaStore.Images.Media.insertImage(
                    requireActivity().contentResolver,
                    image,
                    "IMG_" + Calendar.getInstance().time,
                    null
            )
                val uri = Uri.parse(path)
                setFragmentResult(
                    "fromButtonToImage",
                    bundleOf("uri" to uri.toString())
                )
                image.recycle()
            }
            findNavController().navigate(R.id.action_mainFragment_to_imageResultFragment)
    }

    private fun  stopSDK(){
        scanbot = null
        pageFileStorage = null
        pageProcess = null
    }

    private fun reLoadFragment(){
        val id = findNavController().currentDestination?.id
        findNavController().popBackStack(id!!,true)
        findNavController().navigate(id)
    }
    companion object {
        lateinit var galleryImageLauncher: ActivityResultLauncher<Unit>
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopSDK()
    }
}