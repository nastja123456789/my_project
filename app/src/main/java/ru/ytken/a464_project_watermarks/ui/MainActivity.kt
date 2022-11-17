package ru.ytken.a464_project_watermarks.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.PageFileStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ytken.a464_project_watermarks.ImportImageContract
import ru.ytken.a464_project_watermarks.ManifestPermission
import ru.ytken.a464_project_watermarks.R

import ru.ytken.a464_project_watermarks.repository.SavedImageRepository
import ru.ytken.a464_project_watermarks.repository.SavedImageRepositoryImpl
import ru.ytken.a464_project_watermarks.ui.fragments.PhotoCropFragment
import ru.ytken.a464_project_watermarks.utilities.displayToast
import ru.ytken.a464_project_watermarks.viewmodels.SavedImageFactory
import ru.ytken.a464_project_watermarks.viewmodels.SavedImagesViewModel


class MainActivity : AppCompatActivity() {
    lateinit var navController: NavController
    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch(Dispatchers.Main) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            navController = navHostFragment.navController
        }
        ManifestPermission.checkPermission(this)

        scanbotSDK = ScanbotSDK(this)
        pageFileStorage = scanbotSDK.createPageFileStorage()
        pageProcess = scanbotSDK.createPageProcessor()
        galleryImageLauncher = registerForActivityResult(ImportImageContract(this)){
                resultEntity ->
            bm = resultEntity!!
            val repository = SavedImageRepositoryImpl(this,scanbotSDK, pageFileStorage, pageProcess, bm)
            val savedModel = ViewModelProvider(this, SavedImageFactory(repository))[SavedImagesViewModel::class.java]
            lifecycleScope.launch(Dispatchers.Main) {
                savedModel.loadSavedImages()
                savedModel.savedImagesUiState.observe(this@MainActivity) {
                    val savedImagesDataState = it ?: return@observe
                    if (savedImagesDataState.isLoading) {
                        if (resultEntity.byteCount<1024*1024*100) {
                            savedImagesDataState.savedImages = resultEntity
                            viewModel.setInitImage(resultEntity)
                        }else {
                            savedImagesDataState.savedImages = null
                            viewModel.setInitImage(null)
                        }
                   }
                }
            }
            lifecycleScope.launch(Dispatchers.Main) {
                PhotoCropFragment.newInstance()
                findNavController(R.id.fragmentContainerView).navigate(R.id.action_buttonFragment_to_photoCropFragment)
            }
        }
    }

    companion object {
        lateinit var galleryImageLauncher: ActivityResultLauncher<Unit>
        lateinit var scanbotSDK: ScanbotSDK
        lateinit var pageFileStorage: PageFileStorage
        lateinit var pageProcess: PageProcessor
        lateinit var bm: Bitmap
    }
}