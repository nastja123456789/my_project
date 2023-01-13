package ru.ytken.a464_project_watermarks.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.PageFileStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ytken.a464_project_watermarks.ManifestPermission
import ru.ytken.a464_project_watermarks.R
import java.io.File


class MainActivity : AppCompatActivity() {
    lateinit var navController: NavController
    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //lifecycleScope.launch(Dispatchers.Main) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            navController = navHostFragment.navController
        //}
        ManifestPermission.checkPermission(this)

    }

    companion object {
        lateinit var pageFileStorage: PageFileStorage
        lateinit var scanbotSDK: ScanbotSDK
        lateinit var pageProcess: PageProcessor
        lateinit var galleryImageLauncher: ActivityResultLauncher<Unit>
        lateinit var bm: Bitmap
    }

//    override fun onDestroy() {
//        clearApplicationData()
//        super.onDestroy()
//    }
//
//    fun clearApplicationData() {
//        val cache: File = applicationContext.cacheDir
//        val appDir = File(cache.getParent())
//        if (appDir.exists()) {
//            val children: Array<String> = appDir.list()
//            for (s in children) {
//                if (s != "lib") {
//                    deleteDir(File(appDir, s))
//                    Log.i("EEEEEERRRRRRROOOOOOORRRR",
//                        "**************** File /data/data/APP_PACKAGE/$s DELETED *******************")
//                }
//            }
//        }
//    }
//
//    fun deleteDir(dir: File?): Boolean {
//        if (dir != null && dir.isDirectory()) {
//            val children: Array<String> = dir.list()
//            for (i in children.indices) {
//                val success = deleteDir(File(dir, children[i]))
//                if (!success) {
//                    return false
//                }
//            }
//        }
//        return dir!!.delete()
//    }
}