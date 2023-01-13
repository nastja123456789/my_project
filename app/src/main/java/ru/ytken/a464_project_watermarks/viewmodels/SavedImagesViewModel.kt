package ru.ytken.a464_project_watermarks.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.repository.SavedImageRepository
import ru.ytken.a464_project_watermarks.utilities.Coroutines

class SavedImagesViewModel(private val savedImagesRepository: SavedImageRepository) : ViewModel() {
    private val savedImagesDataState = MutableLiveData<SavedImagesDataState>()
    val savedImagesUiState: LiveData<SavedImagesDataState> get() = savedImagesDataState
    fun loadSavedImages(bitmap: Bitmap) {
        Coroutines.io {
            runCatching {
                emitSavedImagesUiState(isLoading = true)
                savedImagesRepository.loadSavedImage(bitmap)

            }
                .onSuccess { savedImages->
                    if (savedImages == null) {
                        emitSavedImagesUiState(savedImages = null)
                    } else if (savedImages.byteCount < 1024 * 1024 * 100) {
                        emitSavedImagesUiState(savedImages = bitmap, error = "loading", isLoading = true)
                    } else {
                        emitSavedImagesUiState(savedImages = savedImages, error = "loading", isLoading = true)
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