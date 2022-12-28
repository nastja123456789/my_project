package ru.ytken.a464_project_watermarks.repository

import android.content.Intent
import android.graphics.Bitmap

interface TakedImageRepository {
    suspend fun GetResultImage(data: Intent?): Bitmap?
}