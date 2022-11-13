package ru.ytken.a464_project_watermarks.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.WindowManager
import ru.ytken.a464_project_watermarks.domain.models.Image
import ru.ytken.a464_project_watermarks.domain.repository.MainRepository


class MainRepositoryImpl: MainRepository {

    override fun getImage(): Image {
        val w:Int = WindowManager.LayoutParams.WRAP_CONTENT
        val h: Int = WindowManager.LayoutParams.WRAP_CONTENT

        val width: Int = 400
        val height: Int = 400
        val conf = Bitmap.Config.ARGB_8888
        val bitmap = Bitmap.createBitmap(width, height, conf)
        return Image(bitmap)
    }
}