package ru.ytken.a464_project_watermarks.ui.viewmodel


import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SeeScanFragmentViewModel: ViewModel() {
    private val liveScanLettersText = MutableLiveData<String>()

    private val liveScanImage = MutableLiveData<Bitmap>()
    val scanImage: LiveData<Bitmap> = liveScanImage

    var lineBounds: ArrayList<Int> = ArrayList()

    fun setLetterText(text: String) {
        liveScanLettersText.value = text
    }
}
