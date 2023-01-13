package ru.ytken.a464_project_watermarks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.ytken.a464_project_watermarks.repository.SavedImageRepository

class SavedImageFactory(
    private val savedImagesRepository: SavedImageRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SavedImagesViewModel::class.java)){
            return SavedImagesViewModel(savedImagesRepository = savedImagesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}