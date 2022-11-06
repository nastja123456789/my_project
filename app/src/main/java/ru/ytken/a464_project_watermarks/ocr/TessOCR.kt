package ru.ytken.a464_project_watermarks.ocr

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object TessOCR : TessBaseAPI() {

    lateinit var DATA_PATH: String
    private val TAG = "TessOCR"
    private lateinit var assetManager: AssetManager

    fun initDirs(context: Context) {
        assetManager = context.assets

        DATA_PATH = "${(context.getExternalFilesDir(null)?.absoluteFile ?: "")}/DemoOCR/"
        val paths = arrayOf(DATA_PATH, DATA_PATH + "tessdata")

        for (p in paths) {
            val dir = File(p)
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    Log.v(TAG, "Error: Creation of directory $p failed")
                    break
                }
                else
                    Log.v(TAG, "Created directory $p")
            }
        }

    }

    fun initLang(lang: String): Boolean {
        if (!(File(DATA_PATH + "tessdata/$lang.traineddata")).exists()) {
            try {
                val inputStream = assetManager.open("$lang.traineddata")
                Log.e(TAG, "InputStream opened")
                val langFile = File(DATA_PATH + "tessdata/$lang.traineddata")
                if (langFile.createNewFile())
                    Log.e(TAG, "File ${langFile.absolutePath} created")
                else
                    Log.e(TAG, "File ${langFile.absolutePath} not created")
                val outputStream = FileOutputStream(langFile)
                Log.e(TAG, "OutputStream opened")

                val buf = ByteArray(1024)
                var len : Int = inputStream.read(buf)
                while (len > 0) {
                    outputStream.write(buf, 0, len)
                    len = inputStream.read(buf)
                }
                inputStream.close()
                outputStream.close()
                Log.v(TAG, "Copied $lang traineddata")
            } catch (e : IOException) { Log.e(TAG, "Was unable to copy $lang traineddata $e") }
        }

        return TessOCR.init(DATA_PATH, lang)
    }

    fun setTessImage(bitmap: Bitmap) {
        TessOCR.setImage(bitmap)
    }

}