package ru.ytken.a464_project_watermarks.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavedImageRepositoryImpl(
    private val context: Context,
    private var scanbotSDK: ScanbotSDK,
    private var pageFileStorage: PageFileStorage,
    private var pageProcessor: PageProcessor,
    private var bitmap: Bitmap? = null
): SavedImageRepository {

    override suspend fun loadSavedImage(): Bitmap? {
        scanbotSDK = ScanbotSDK(context)
        pageFileStorage = scanbotSDK.createPageFileStorage()
        var initialBitmap: Bitmap?
            val notOrNot: String
            notOrNot = pageFileStorage.add(bitmap!!)
            withContext(Dispatchers.IO) {
                val pageId = notOrNot
                var page = Page(pageId, emptyList(), DetectionStatus.OK, ImageFilterType.NONE)
                page = pageProcessor.detectDocument(page)
                val image = pageFileStorage.getImage(
                    page.pageId,
                    PageFileStorage.PageFileType.DOCUMENT //cropped image
                )
                if (image!!.byteCount > 1024*1024*100) {
                    initialBitmap = null
                } else {
                    initialBitmap = image
                }
            }
        return initialBitmap
    }
}