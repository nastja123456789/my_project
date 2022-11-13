package ru.ytken.a464_project_watermarks.domain.repository

import android.graphics.Bitmap
import android.net.Uri
import ru.ytken.a464_project_watermarks.domain.models.Image
import ru.ytken.a464_project_watermarks.domain.models.Status

interface MainRepository {
    fun getImage() : Image
}