package ru.ytken.a464_project_watermarks.repository

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TakedImageRepositoryImpl(
    private val context: Context,
    //private var scanbotSDK: ScanbotSDK,
    private var pageFileStorage: PageFileStorage,
    //private var ResultCode: resultCode,

    //private var bitmap: Bitmap? = null
): TakedImageRepository {
    override suspend fun GetResultImage(data: Intent?): Bitmap? {
        Log.d(TAG, "I am in TakedImageRepositoryImpl")
        var initialBitmap: Bitmap? = null
        val result: DocumentScannerActivity.Result = DocumentScannerActivity.extractResult(AppCompatActivity.RESULT_OK, data)
        if (result.resultOk) {
            val snappedPages: List<Page>? = result.result
            val pageId = snappedPages?.get(0)?.pageId
            val image = pageFileStorage.getImage(pageId!!, PageFileStorage.PageFileType.DOCUMENT, BitmapFactory.Options())
            initialBitmap = image
            pageFileStorage.remove(pageId!!)
        }

        return initialBitmap
    }
}