package ru.ytken.a464_project_watermarks.ui.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import kotlinx.android.synthetic.main.fragment_button.*
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.ui.MainActivity.Companion.galleryImageLauncher
import ru.ytken.a464_project_watermarks.ui.MainViewModel

class ButtonFragment() : Fragment(
    R.layout.fragment_button,
) {

    private val DOCUMENT_SCANNER_REQUEST_CODE_CONSTANT = 100
    private val vm: MainViewModel by activityViewModels()
    private lateinit var pageFileStorage: PageFileStorage
    private lateinit var pageProcess: PageProcessor
    //private lateinit var repository: MakeImageRepository
    //private lateinit var makeModel: MakeImageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  repository = MakeImageRepositoryImpl(requireActivity())
      //  makeModel = ViewModelProvider(requireActivity(), MakeImageFactory(repository))[MakeImageViewModel::class.java]
        createSDK()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        val scanbotSDK = ScanbotSDK(requireActivity())
        pageFileStorage = scanbotSDK.createPageFileStorage()
        pageProcess = scanbotSDK.createPageProcessor()
    }

    private fun takePhoto() {
        val cameraConfiguration = DocumentScannerConfiguration()
        val intent = DocumentScannerActivity.newIntent(context!!, cameraConfiguration)
        startActivityForResult(intent, DOCUMENT_SCANNER_REQUEST_CODE_CONSTANT)
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
                        //vm.setInitImage(image!!)
        //                makeModel.saveImage(image!!)
        //                makeModel.saveImageUIState.observe(this) {
        //                    val saveImagesDataState = it ?: return@observe
        //                    if (saveImagesDataState.isLoading) {
                                //saveImagesDataState.uri = image
                                vm.setInitImage(image!!)
        //                    }
        //                }
                    }
                    findNavController().navigate(R.id.action_mainFragment_to_imageResultFragment)
                }
            }
        }
    }
}