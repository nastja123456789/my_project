package ru.ytken.a464_project_watermarks.ui

import android.Manifest
import android.content.DialogInterface
import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.text.Text
//import com.google.mlkit.vision.text.TextRecognition
//import com.google.mlkit.vision.text.latin.TextRecognizerOptions
//import com.googlecode.tesseract.android.TessBaseAPI
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import ru.ytken.a464_project_watermarks.rotateBitmap

//import ru.ytken.a464_project_watermarks.rotateBitmap
import java.io.InputStream

class MainViewModel: ViewModel() {
    val LOGTAG = MainViewModel::class.simpleName

    private val liveInitImage = MutableLiveData<Bitmap>()
    val initImage: LiveData<Bitmap> = liveInitImage

    private val liveHighlightedImage = MutableLiveData<Bitmap>()
    val highlightedImage: LiveData<Bitmap> = liveHighlightedImage

    private val liveScanImage = MutableLiveData<Bitmap>()
    val scanImage: LiveData<Bitmap> = liveScanImage

    private val liveScanLettersImage = MutableLiveData<Bitmap>()
    val scanLettersImage: LiveData<Bitmap> = liveScanLettersImage

    private val liveScanLettersText = MutableLiveData<String>()
    val scanLettersText: LiveData<String> = liveScanLettersText

    private val liveHasText = MutableLiveData<Boolean>()
    val hasText: LiveData<Boolean> = liveHasText

    var lineBounds: ArrayList<Int> = ArrayList<Int>()

    fun findTextInBitmap() {
        var imageBitmap = liveInitImage.value!!
        liveInitImage.value = imageBitmap
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        var maxBitmap: Bitmap = imageBitmap
        var maxText: Int = 0
        var maxBlocks: List<Text.TextBlock>? = null
        lineBounds.clear()

        for(i in 0..360 step 90 ) {
            val copyBitmap = imageBitmap.rotateBitmap(i).copy(Bitmap.Config.ARGB_8888,false)
            val image = InputImage.fromBitmap(copyBitmap, 0)
                viewModelScope.async(Dispatchers.Default) {
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            if (visionText.text.length > maxText) {
                                maxText = visionText.text.length
                                maxBitmap = copyBitmap
                                maxBlocks = visionText.textBlocks
                            }
                        }
                        .addOnCompleteListener {
                            if (i == 360) {
                                val mutableImageBitmap = maxBitmap.copy(Bitmap.Config.ARGB_8888,true)

                                val canvas = Canvas(mutableImageBitmap)
                                var shapeDrawable = ShapeDrawable(RectShape())
                                shapeDrawable.paint.style = Paint.Style.STROKE
                                shapeDrawable.paint.strokeWidth = 10F

                                if (maxBlocks != null) {
                                    for (block in maxBlocks!!) {
                                        for (line in block.lines) {

                                            line.boundingBox?.let {
                                                shapeDrawable.bounds = it
                                                lineBounds.add(it.centerY())
                                            }
                                            shapeDrawable.draw(canvas)
                                        }
                                    }
                                    Log.d(LOGTAG, "Текст распознан!")
                                    liveHasText.value = true
                                } else {
                                    Log.d(LOGTAG, "Текст не найден")
                                    liveHasText.value = false
                                }

                                liveInitImage.value = maxBitmap
                                liveHighlightedImage.value = mutableImageBitmap

                            } }
                }
        }
    }

    fun setInitImage(bitmap: Bitmap) {
        liveInitImage.value = bitmap
    }

    fun setScanImageToInit() {
        liveScanImage.value = highlightedImage.value
    }

    fun setCropImage(bitmap: Bitmap) {
        liveInitImage.value = bitmap
    }

    fun setLetterText(text: String) {
        liveScanLettersText.value = text
    }

}