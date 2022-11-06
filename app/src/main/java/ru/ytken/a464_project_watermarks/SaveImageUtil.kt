package ru.ytken.a464_project_watermarks

import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object SaveImageUtil {

    val imageCollection:Uri = sdk29AndUp {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    lateinit var scanUri: Uri

    fun savePhotoToExternalStorage(context: Context, displayName: String, bmp: Bitmap): Boolean {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
            put(MediaStore.Images.Media.WIDTH, bmp.width)
        }
        return try {
            context.contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                scanUri = uri
                context.contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                        throw IOException("Couldn't compress the bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun loadPhotoFromExternalStorage() {

    }

    fun savePhotoToInternalStorage(context: Context, filename: String, bmp: Bitmap): Boolean {
        return try {
             context.openFileOutput(filename, MODE_PRIVATE).use { stream ->
                if(!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IOException("Couldn't save bitmap")
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    suspend fun loadPhotoFromInternalStorage(context: Context, filename: String): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            val files = context.filesDir.listFiles()
            files.filter {
                it.canRead() && it.isFile && it.name.equals(filename.replace(".jpg", ""))}.map {
                Log.d("NamesFiles", "filename: $filename it.name: ${it.name}")
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bmp
            }
        }
    }

}