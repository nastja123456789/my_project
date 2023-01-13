package ru.ytken.a464_project_watermarks.ui

import android.os.Bundle
import android.util.Log
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
    private lateinit var navController: NavController

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


    }

    companion object {
        //lateinit var galleryImageLauncher: ActivityResultLauncher<Unit>
        lateinit var scanbotSDK: ScanbotSDK
        lateinit var pageFileStorage: PageFileStorage
        lateinit var pageProcess: PageProcessor
    }

    override fun onDestroy() {
        clearApplicationData()
        super.onDestroy()
    }

    private fun clearApplicationData() {
        val cache: File = applicationContext.cacheDir
        val appDir = cache.parent?.let { File(it) }
        if (appDir!!.exists()) {
            val children: Array<String> = appDir.list() as Array<String>
            for (s in children) {
                if (s != "lib") {
                    deleteDir(File(appDir, s))
                    Log.i("EEEEEERRRRRRROOOOOOORRRR",
                        "**************** File /data/data/APP_PACKAGE/$s DELETED *******************")
                }
            }
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if ((dir != null) && dir.isDirectory) {
            val children: Array<String> = dir.list() as Array<String>
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir!!.delete()
    }
}