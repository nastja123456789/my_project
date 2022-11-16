package ru.ytken.a464_project_watermarks.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
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
import ru.ytken.a464_project_watermarks.repository.SavedImageFactory
import ru.ytken.a464_project_watermarks.repository.SavedImageRepository
import ru.ytken.a464_project_watermarks.repository.SavedImageRepositoryImpl
import ru.ytken.a464_project_watermarks.ui.fragments.ImageResultFragment
import ru.ytken.a464_project_watermarks.ui.fragments.PhotoCropFragment
import ru.ytken.a464_project_watermarks.viewmodels.SavedImagesViewModel


class MainActivity : AppCompatActivity() {

    lateinit var navController: NavController

    private lateinit var pageFileStorage: PageFileStorage

    private lateinit var pageProcess: PageProcessor

    lateinit var savedFactory: SavedImageFactory

    val viewModel: MainViewModel by viewModels()

    //val savedModel: SavedImagesViewModel by viewModels()
//    private val savedModel: SavedImagesViewModel by viewModels {
//        SavedImageFactory()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch(Dispatchers.Main) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            navController = navHostFragment.navController
        }
        ManifestPermission.checkPermission(this)

        val scanbotSDK = ScanbotSDK(this)
        pageFileStorage = scanbotSDK.createPageFileStorage()
        pageProcess = scanbotSDK.createPageProcessor()
        repository = SavedImageRepositoryImpl(this)
        val savedModel = ViewModelProvider(this, SavedImageFactory(repository))[SavedImagesViewModel::class.java]
        galleryImageLauncher = registerForActivityResult(ImportImageContract(this)){
                resultEntity ->
            lifecycleScope.launch(Dispatchers.Main) {
                Log.d("whywhy","vishlo")
                Log.d("now int","${savedModel.inter}")
                savedModel.loadSavedImages()
                savedModel.savedImagesUiState.observe(this@MainActivity) {
                    Log.d("ogoogo","vishlo")
                    val savedImagesDataState = it ?: return@observe
                    Log.d("ogo","ne vishlo")
                    //if (savedImagesDataState.isLoading) {
                        Log.d("set","may be set")
                        savedImagesDataState.savedImages = resultEntity
                        viewModel.setInitImage(resultEntity!!)
                    //}
                    //else Log.d("why doesn't loading?", "i dont know")
                }

                Log.d("load","savedmodel")
            }
            lifecycleScope.launch(Dispatchers.Main) {
                PhotoCropFragment.newInstance()
                findNavController(R.id.fragmentContainerView).navigate(R.id.action_buttonFragment_to_photoCropFragment)
            }
        }



//        galleryImageLauncher = registerForActivityResult(ImportImageContract(this)){
//                resultEntity ->
//            //lifecycleScope.launch(Dispatchers.Default) {
//                savedModel.savedImagesUiState.observe(this) {
//                    val savedImagesDataState = it ?: return@observe
//                    if (savedImagesDataState.isLoading) viewModel.setInitImage(resultEntity!!)
//                }
            //resultEntity?.let { bitmap ->
//                    processImageForAutoDocumentDetection(
//                        pageFileStorage,
//                        pageProcess,
//                        bitmap
//                    )

                //}
                    //}
      //  }
        //savedModel.loadSavedImages()
//        findNavController(R.id.fragmentContainerView).navigate(R.id.action_buttonFragment_to_photoCropFragment)
        //PhotoCropFragment.newInstance()
    }

//    suspend fun initial(activity: MainActivity) {
//        withContext(Dispatchers.Main) {
//            repository = SavedImageRepositoryImpl(activity)
//            val savedModel = ViewModelProvider(activity, SavedImageFactory(repository))[SavedImagesViewModel::class.java]
//            Log.d("error", "after provider")
//
//            Log.d("error", "after model")
//            findNavController(R.id.fragmentContainerView).navigate(R.id.action_buttonFragment_to_photoCropFragment)
//        }
//        //findNavController(R.id.fragmentContainerView).navigate(R.id.action_buttonFragment_to_photoCropFragment)
//
//    }

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
            findNavController(R.id.fragmentContainerView).navigate(R.id.action_buttonFragment_to_photoCropFragment)
        }
    }
    companion object {
        lateinit var galleryImageLauncher: ActivityResultLauncher<Unit>
        lateinit var repository: SavedImageRepository
    }
}