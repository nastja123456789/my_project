package ru.ytken.a464_project_watermarks.repository

import android.graphics.Bitmap

interface SavedImageRepository {
    fun loadSavedImage(): Bitmap?
}