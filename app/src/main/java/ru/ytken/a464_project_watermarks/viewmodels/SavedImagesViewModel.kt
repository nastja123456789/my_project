package ru.ytken.a464_project_watermarks.viewmodels

import android.graphics.Bitmap
import android.util.Log
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
            }
                .onSuccess {
                        savedImages->
                if (savedImages!!.byteCount>1024*1024*100){
                    emitSavedImagesUiState(savedImages=null, error = "loading", isLoading = true)
                    Log.d("bigger","bigger")
                } else if (savedImages==null) {
                    Log.d("nulll","nulll")
                    emitSavedImagesUiState(savedImages = null)
                }
                else
                {
                    emitSavedImagesUiState(savedImages=savedImages, error = "loading", isLoading = true)
                    Log.d("to end","to end")
                }
            }.onFailure {
                    emitSavedImagesUiState(isLoading = false)
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