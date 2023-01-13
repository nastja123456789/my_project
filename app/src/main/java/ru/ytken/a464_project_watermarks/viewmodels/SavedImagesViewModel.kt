package ru.ytken.a464_project_watermarks.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.ytken.a464_project_watermarks.repository.SavedImageRepository

class SavedImagesViewModel(private val savedImagesRepository: SavedImageRepository) : ViewModel() {
    private val savedImagesDataState = MutableLiveData<SavedImagesDataState>()
    val savedImagesUiState: LiveData<SavedImagesDataState> get() = savedImagesDataState
    fun loadSavedImages():Bitmap? {
        runCatching {
                emitSavedImagesUiState(isLoading = true)
                savedImagesRepository.loadSavedImage()
        }
            .onSuccess {
                        savedImages->
                return if (savedImages!!.byteCount>1024*1024*100){
                    emitSavedImagesUiState(savedImages=null, error = "loading", isLoading = true)
                    null
                } else {
                    emitSavedImagesUiState(savedImages=savedImages, error = "loading", isLoading = true)
                    savedImages
                }
            }
            .onFailure {
                    emitSavedImagesUiState(isLoading = false)
                    return@onFailure
            }
        return null
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