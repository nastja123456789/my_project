package ru.ytken.a464_project_watermarks.ui

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import kotlinx.android.synthetic.main.fragment_button.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ytken.a464_project_watermarks.ImportImageContract
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.ui.fragments.ImageResultFragment


class MainActivity : AppCompatActivity() {

    lateinit var navController: NavController

    private lateinit var pageFileStorage: PageFileStorage

    private lateinit var pageProcess: PageProcessor

    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ru.ytken.a464_project_watermarks.R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(ru.ytken.a464_project_watermarks.R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        Dexter.withActivity(this)
            .withPermissions(
                permissions
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(this@MainActivity, getString(ru.ytken.a464_project_watermarks.R.string.allPermission), Toast.LENGTH_SHORT).show()
                        }
                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(this@MainActivity, getString(ru.ytken.a464_project_watermarks.R.string.denyPermission), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?,
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener {
                Toast.makeText(this, it.name, Toast.LENGTH_SHORT).show()
            }
            .check()

//        ButtonFragment.newInstance()
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

        Log.d("create","create activity")
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
            viewModel.findTextInBitmap()
            ImageResultFragment.newInstance()
            findNavController(R.id.fragmentContainerView).navigate(R.id.action_buttonFragment_to_photoCropFragment)
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("restart","restart activity")
    }

    override fun onResume() {
        super.onResume()
        Log.d("resume","resume activity")
    }

    override fun onDestroy() {
        Log.d("destroy","destroy activity")
        super.onDestroy()
    }


    companion object {
        val permissions = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
        lateinit var galleryImageLauncher: ActivityResultLauncher<Unit>
    }
}