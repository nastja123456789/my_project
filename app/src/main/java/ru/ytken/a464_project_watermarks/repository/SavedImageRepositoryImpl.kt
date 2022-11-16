package ru.ytken.a464_project_watermarks.repository

import android.content.Context
import android.graphics.Bitmap
import io.scanbot.sdk.core.contourdetector.DetectionStatus
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.persistence.PageFileStorage
import io.scanbot.sdk.process.ImageFilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavedImageRepositoryImpl(
    context: Context,
): SavedImageRepository {

    companion object {
        private lateinit var pageFileStorage: PageFileStorage
        private lateinit var pageProcessor: PageProcessor
        private lateinit var bitmap: Bitmap
    }

    override suspend fun loadSavedImage(): Bitmap {

        val initialBitmap: Bitmap
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
            initialBitmap = image!!
        }
        return initialBitmap
    }
}