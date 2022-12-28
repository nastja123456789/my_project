package ru.ytken.a464_project_watermarks.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.ytken.a464_project_watermarks.repository.TakedImageRepository

class TakedImageFactory(
    val takedImagesRepository: TakedImageRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(TakedImagesViewModel::class.java)){
            Log.d(ContentValues.TAG, "I am in TakedImageFactory")
            return TakedImagesViewModel(takedImagesRepository = takedImagesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}