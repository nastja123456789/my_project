package ru.ytken.a464_project_watermarks.ui.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.ui.registerForActivityResultOk
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import kotlinx.android.synthetic.main.fragment_button.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ytken.a464_project_watermarks.ImportImageContract
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.repository.SavedImageRepositoryImpl
import ru.ytken.a464_project_watermarks.viewmodels.SavedImageFactory
import ru.ytken.a464_project_watermarks.viewmodels.SavedImagesViewModel
import java.io.ByteArrayOutputStream
import java.util.*


class ButtonFragment : Fragment(
    R.layout.fragment_button,
) {
    private lateinit var scanbot: ScanbotSDK
    private lateinit var pageFileStorage: PageFileStorage
    private lateinit var pageProcess: PageProcessor
    private var pageId: String? = null
    private var bit:Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createSDK()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        galleryImageLauncher = registerForActivityResult(ImportImageContract(requireContext())){
                resultEntity ->
            val bitmapEntity = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, resultEntity)
            val repository = SavedImageRepositoryImpl(requireContext(),
                scanbot, pageFileStorage, pageProcess, bitmapEntity)
            val savedModel = ViewModelProvider(this, SavedImageFactory(repository))[SavedImagesViewModel::class.java]

            lifecycleScope.launch(Dispatchers.Main) {
                bit = savedModel.loadSavedImages()
            }
            if (bit!=null) {
                setFragmentResult("fromButtonToCrop", bundleOf("uri" to resultEntity.toString()))
                findNavController().navigate(R.id.action_buttonFragment_to_photoCropFragment)
            }
        }
        buttonTakePhoto.setOnClickListener {
            takePhoto()
        }

        buttonChoosePhotoFromStorage.setOnClickListener {
            galleryImageLauncher.launch(Unit)
            buttonChoosePhotoFromStorage.visibility = View.INVISIBLE
            buttonTakePhoto.visibility = View.INVISIBLE
            progressBarWaitForResult.visibility = View.VISIBLE
        }
    }

    private fun createSDK() {
        scanbot = ScanbotSDK(requireActivity())
        pageFileStorage = scanbot.createPageFileStorage()
        pageProcess = scanbot.createPageProcessor()
    }

    private fun takePhoto() {
        val cameraConfiguration = DocumentScannerConfiguration()
        resultLauncher.launch(cameraConfiguration)
    }

    private var resultLauncher = registerForActivityResultOk(DocumentScannerActivity.ResultContract()) { result ->
        if (result.resultOk) {
                val snappedPages: List<Page>? = result.result
                pageId = snappedPages?.get(0)?.pageId
                val image = pageFileStorage.getImage(pageId!!, PageFileStorage.PageFileType.DOCUMENT, BitmapFactory.Options())
                val bytes = ByteArrayOutputStream()
                Log.d("$image","imageimage")
                image!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                val path: String = MediaStore.Images.Media.insertImage(
                    requireActivity().contentResolver,
                    image,
                    "IMG_" + Calendar.getInstance().time,
                    null
            )
                val uri = Uri.parse(path)
                setFragmentResult("fromButtonToImage", bundleOf("uri" to uri.toString()))
                //pageFileStorage.remove(pageId!!)
            }
            findNavController().navigate(R.id.action_mainFragment_to_imageResultFragment)
    }
    companion object {
        lateinit var galleryImageLauncher: ActivityResultLauncher<Unit>
    }
}