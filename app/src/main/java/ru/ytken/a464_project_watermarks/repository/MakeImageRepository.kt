package ru.ytken.a464_project_watermarks.repository

import android.graphics.Bitmap
import android.net.Uri

interface MakeImageRepository {
    suspend fun saveImageToGallery(bitmap: Bitmap): Uri
}