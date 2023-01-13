package ru.ytken.a464_project_watermarks.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.persistence.PageFileStorage.PageFileType.DOCUMENT
import io.scanbot.sdk.process.ImageFilterType

class SavedImageRepositoryImpl(
    private val context: Context,
    private var scanbotSDK: ScanbotSDK,
    private var pageFileStorage: PageFileStorage,
    private var pageProcessor: PageProcessor,
    private var bitmap: Bitmap? = null
): SavedImageRepository {

    override fun loadSavedImage(): Bitmap? {
        scanbotSDK = ScanbotSDK(context)
        pageFileStorage = scanbotSDK.createPageFileStorage()
        var initialBitmap: Bitmap?
        val notOrNot: String = pageFileStorage.add(bitmap!!)
            var page = Page(notOrNot, emptyList(), DetectionStatus.OK, ImageFilterType.NONE)
            page = pageProcessor.detectDocument(page)
            val image = this@SavedImageRepositoryImpl.pageFileStorage.getImage(
                page.pageId,
                DOCUMENT //cropped image
            )
            image.also { initialBitmap = it }
        return initialBitmap
    }
}