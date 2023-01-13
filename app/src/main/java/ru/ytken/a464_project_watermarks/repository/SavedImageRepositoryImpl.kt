package ru.ytken.a464_project_watermarks.repository

import android.content.Context
import android.graphics.Bitmap
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
): SavedImageRepository {

    override suspend fun loadSavedImage(bitmap: Bitmap): Bitmap? {
        scanbotSDK = ScanbotSDK(context)
        pageFileStorage = scanbotSDK.createPageFileStorage()
        var initialBitmap: Bitmap?
        val notOrNot: String = pageFileStorage.add(bitmap)
        withContext(Dispatchers.Main) {
            var page = Page(notOrNot, emptyList(), DetectionStatus.OK, ImageFilterType.NONE)
            page = pageProcessor.detectDocument(page)
            val image = pageFileStorage.getImage(
                page.pageId,
                PageFileStorage.PageFileType.DOCUMENT //cropped image
            )
            initialBitmap = image
        }
        return initialBitmap
    }
}