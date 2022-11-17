package ru.ytken.a464_project_watermarks.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class MakeImageRepositoryImpl(
    private val context: Context
    ): MakeImageRepository {

    override suspend fun saveImageToGallery(bitmap: Bitmap): Uri {
        return try {
            val mediaStorageDirectory = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "Saved Images"
            )
            if (!mediaStorageDirectory.exists()) {
                mediaStorageDirectory.mkdirs()
            }
            val fileName = "IMG_${System.currentTimeMillis()}.jpg"
            val file = File(mediaStorageDirectory, fileName)
            saveFile(file, bitmap)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            return Uri.EMPTY
        }
    }

    private fun saveFile(file: File, bitmap: Bitmap) {
        with(FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
            flush()
            close()
        }
    }
}