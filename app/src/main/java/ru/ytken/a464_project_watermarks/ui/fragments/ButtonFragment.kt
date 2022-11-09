package ru.ytken.a464_project_watermarks.ui.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import kotlinx.android.synthetic.main.fragment_button.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.ui.MainActivity.Companion.galleryImageLauncher
import ru.ytken.a464_project_watermarks.ui.MainViewModel

class ButtonFragment() : Fragment(
    R.layout.fragment_button,
) {

    private val DOCUMENT_SCANNER_REQUEST_CODE_CONSTANT = 100
    private val TAG = ButtonFragment::class.simpleName

    private val vm: MainViewModel by activityViewModels()

    private lateinit var pageFileStorage: PageFileStorage

//    private lateinit var galleryImageLauncher: ActivityResultLauncher<Unit>

    private lateinit var pageProcess: PageProcessor
    private lateinit var image: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scanbotSDK = ScanbotSDK(requireActivity())
        pageFileStorage = scanbotSDK.createPageFileStorage()
        pageProcess = scanbotSDK.createPageProcessor()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonTakePhoto.setOnClickListener {
            val cameraConfiguration = DocumentScannerConfiguration()
            val intent = DocumentScannerActivity.newIntent(context!!, cameraConfiguration)
            startActivityForResult(intent, DOCUMENT_SCANNER_REQUEST_CODE_CONSTANT)
        }

        buttonChoosePhotoFromStorage.setOnClickListener {
                galleryImageLauncher.launch(Unit)
                buttonChoosePhotoFromStorage.visibility = View.INVISIBLE
                buttonTakePhoto.visibility = View.INVISIBLE
            progressBarWaitForResult.visibility = View.VISIBLE
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                DOCUMENT_SCANNER_REQUEST_CODE_CONSTANT -> {
                    val result: DocumentScannerActivity.Result = DocumentScannerActivity.extractResult(resultCode, data)
                    if (result.resultOk) {
                        val snappedPages: List<Page>? = result.result
                        val pageId = snappedPages?.get(0)?.pageId
                        val image: Bitmap? = pageFileStorage.getImage(pageId!!, PageFileStorage.PageFileType.DOCUMENT, BitmapFactory.Options())
                        vm.setInitImage(image!!)
                    }
                    findNavController().navigate(R.id.action_mainFragment_to_imageResultFragment)
                }
            }
        }
    }
}