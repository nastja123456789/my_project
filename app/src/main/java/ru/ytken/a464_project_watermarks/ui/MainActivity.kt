package ru.ytken.a464_project_watermarks.ui

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ytken.a464_project_watermarks.ImportImageContract
import ru.ytken.a464_project_watermarks.ManifestPermission
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.ui.fragments.ImageResultFragment


class MainActivity : AppCompatActivity() {

    lateinit var navController: NavController

    private lateinit var pageFileStorage: PageFileStorage

    private lateinit var pageProcess: PageProcessor

    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        ManifestPermission.checkPermission(this)

        val scanbotSDK = ScanbotSDK(this)
        pageFileStorage = scanbotSDK.createPageFileStorage()
        pageProcess = scanbotSDK.createPageProcessor()
        galleryImageLauncher = registerForActivityResult(ImportImageContract(this)){
                resultEntity ->
            lifecycleScope.launch(Dispatchers.Default) {
                resultEntity?.let { bitmap ->
                    processImageForAutoDocumentDetection(
                        pageFileStorage,
                        pageProcess,
                        bitmap
                    )
                }
            }
        }
    }

    suspend fun processImageForAutoDocumentDetection(
        pageFileStorage: PageFileStorage,
        pageProcessor: PageProcessor,
        bitmap: Bitmap,
    ) {
        val page = withContext(Dispatchers.Default) {
            val pageId = pageFileStorage.add(bitmap)
            var page = Page(pageId, emptyList(), DetectionStatus.OK, ImageFilterType.NONE)
            page = pageProcessor.detectDocument(page)
            page
        }

        withContext(Dispatchers.Main) {
            val image = pageFileStorage.getImage(
                page.pageId,
                PageFileStorage.PageFileType.DOCUMENT //cropped image
            )
            viewModel.setInitImage(image!!)
            ImageResultFragment.newInstance()
            findNavController(R.id.fragmentContainerView).navigate(R.id.action_buttonFragment_to_photoCropFragment)
        }
    }
    companion object {
        lateinit var galleryImageLauncher: ActivityResultLauncher<Unit>
    }
}