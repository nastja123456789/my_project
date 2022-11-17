package ru.ytken.a464_project_watermarks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.ytken.a464_project_watermarks.repository.MakeImageRepository

class MakeImageFactory(
    val saveRepository: MakeImageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SavedImagesViewModel::class.java)){
            return MakeImageViewModel(saveRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}