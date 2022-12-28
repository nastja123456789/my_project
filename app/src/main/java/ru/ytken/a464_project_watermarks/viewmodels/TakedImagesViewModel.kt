package ru.ytken.a464_project_watermarks.viewmodels

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.ytken.a464_project_watermarks.repository.TakedImageRepository
import ru.ytken.a464_project_watermarks.utilities.Coroutines

class TakedImagesViewModel(private val takedImagesRepository: TakedImageRepository) : ViewModel() {
    private val takedImagesDataState = MutableLiveData<TakedImagesViewModel.TakedImagesDataState>()
    val takedImagesUiState: LiveData<TakedImagesViewModel.TakedImagesDataState> get() = takedImagesDataState
    fun GetResultImage(data: Intent?) {
        Log.d(ContentValues.TAG, "I am in TakedImageViewModel")
        Coroutines.io {
            val initBitmap = takedImagesRepository.GetResultImage(data)
            emitTakedImagesUiState(savedImages = initBitmap)
        }
    }
    private fun emitTakedImagesUiState(
        savedImages: Bitmap? = null,
    ) {
        val dataState = TakedImagesDataState(savedImages)
        takedImagesDataState.postValue(dataState)
    }

    data class TakedImagesDataState(
        var savedImages: Bitmap?
    )
}