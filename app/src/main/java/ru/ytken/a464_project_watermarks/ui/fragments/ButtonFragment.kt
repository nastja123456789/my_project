package ru.ytken.a464_project_watermarks.ui.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
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
import ru.ytken.a464_project_watermarks.repository.TakedImageRepositoryImpl
import ru.ytken.a464_project_watermarks.ui.MainActivity.Companion.galleryImageLauncher
import ru.ytken.a464_project_watermarks.ui.MainViewModel
import ru.ytken.a464_project_watermarks.viewmodels.TakedImageFactory
import ru.ytken.a464_project_watermarks.viewmodels.TakedImagesViewModel

class ButtonFragment() : Fragment(
    R.layout.fragment_button,
) {

    private val DOCUMENT_SCANNER_REQUEST_CODE_CONSTANT = 100
    private val vm: MainViewModel by activityViewModels()
    //private lateinit var pageFileStorage: PageFileStorage
    //private lateinit var pageProcess: PageProcessor
    var pageId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        scanbotSDK = ScanbotSDK(requireActivity())
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
                    buttonChoosePhotoFromStorage.visibility = View.INVISIBLE
                    buttonTakePhoto.visibility = View.INVISIBLE
                    progressBarWaitForResult.visibility = View.VISIBLE
                    val takedRepository = TakedImageRepositoryImpl(context!!,pageFileStorage)
                    val takedModel = ViewModelProvider(this, TakedImageFactory(takedRepository))[TakedImagesViewModel::class.java]
                    lifecycleScope.launch(Dispatchers.Main) {
                    takedModel.GetResultImage(data)
                    takedModel.takedImagesUiState.observe(requireActivity()) {
                        val takedImagesDataState = it ?: return@observe
                        vm.setInitImage(takedImagesDataState.savedImages)
                        findNavController().navigate(R.id.action_mainFragment_to_imageResultFragment)
                    }
                    }
                }
            }
        }
    }
    companion object{
        lateinit var pageFileStorage: PageFileStorage
        lateinit var scanbotSDK: ScanbotSDK
        lateinit var pageProcess: PageProcessor
    }
}