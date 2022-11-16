package ru.ytken.a464_project_watermarks.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.ytken.a464_project_watermarks.repository.SavedImageRepository
import ru.ytken.a464_project_watermarks.utilities.Coroutines

class SavedImagesViewModel(private val savedImagesRepository: SavedImageRepository) : ViewModel() {
    private val savedImagesDataState = MutableLiveData<SavedImagesDataState>()
    val savedImagesUiState: LiveData<SavedImagesDataState> get() = savedImagesDataState
    fun loadSavedImages() {
        Coroutines.io {
            runCatching {
                emitSavedImagesUiState(isLoading = true)
                savedImagesRepository.loadSavedImage()
            }.onSuccess { savedImages->
                if (savedImages.equals(null)){
                    emitSavedImagesUiState(error = "No image found")
                }else{
                    emitSavedImagesUiState(savedImages=savedImages)
                }
            }.onFailure {
                emitSavedImagesUiState(error = it.message.toString())
            }
        }
    }

    private fun emitSavedImagesUiState(
        isLoading: Boolean = false,
        savedImages: Bitmap? = null,
        error: String? = null
    ) {
        val dataState = SavedImagesDataState(isLoading, savedImages, error)
        savedImagesDataState.postValue(dataState)
    }

    data class SavedImagesDataState(
        val isLoading: Boolean,
        var savedImages: Bitmap?,
        val error: String?
    )
}
