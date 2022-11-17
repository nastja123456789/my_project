package ru.ytken.a464_project_watermarks.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import ru.ytken.a464_project_watermarks.rotateBitmap

class MainViewModel: ViewModel() {
    private val liveScanLettersText = MutableLiveData<String>()
    val scanLettersText: LiveData<String> = liveScanLettersText

    private val liveInitImage = MutableLiveData<Bitmap>()
    val initImage: LiveData<Bitmap> = liveInitImage

    private val liveHighlightedImage = MutableLiveData<Bitmap>()
    val highlightedImage: LiveData<Bitmap> = liveHighlightedImage

    private val liveScanImage = MutableLiveData<Bitmap>()
    val scanImage: LiveData<Bitmap> = liveScanImage

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
                                    liveHasText.value = true
                                } else {
                                    liveHasText.value = false
                                }


                                liveInitImage.value = maxBitmap
                                liveHighlightedImage.value = mutableImageBitmap

                            } }
                }
        }
    }

    fun setInitImage(bitmap: Bitmap?) {
        liveInitImage.value = bitmap
    }

    fun setScanImageToInit() {
        liveScanImage.value = highlightedImage.value
    }

    fun setLetterText(text: String) {
        liveScanLettersText.value = text
    }
    fun setImageToNull() {
        liveHighlightedImage.value = null
    }
}