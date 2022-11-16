package ru.ytken.a464_project_watermarks.ui

import android.os.Bundle
import android.util.Log
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
import ru.ytken.a464_project_watermarks.repository.SavedImageFactory
import ru.ytken.a464_project_watermarks.repository.SavedImageRepository
import ru.ytken.a464_project_watermarks.repository.SavedImageRepositoryImpl
import ru.ytken.a464_project_watermarks.ui.fragments.PhotoCropFragment
import ru.ytken.a464_project_watermarks.viewmodels.SavedImagesViewModel


class MainActivity : AppCompatActivity() {

    lateinit var navController: NavController

    private lateinit var pageFileStorage: PageFileStorage

    private lateinit var pageProcess: PageProcessor

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

        val scanbotSDK = ScanbotSDK(this)
        pageFileStorage = scanbotSDK.createPageFileStorage()
        pageProcess = scanbotSDK.createPageProcessor()
        repository = SavedImageRepositoryImpl(this)
        val savedModel = ViewModelProvider(this, SavedImageFactory(repository))[SavedImagesViewModel::class.java]
        galleryImageLauncher = registerForActivityResult(ImportImageContract(this)){
                resultEntity ->
            lifecycleScope.launch(Dispatchers.Main) {
                savedModel.loadSavedImages()
                savedModel.savedImagesUiState.observe(this@MainActivity) {
                    val savedImagesDataState = it ?: return@observe
                    //if (savedImagesDataState.isLoading) {
                        Log.d("set","may be set")
                        savedImagesDataState.savedImages = resultEntity
                        viewModel.setInitImage(resultEntity!!)
                    //}
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
        lateinit var repository: SavedImageRepository
    }
}