package ru.ytken.a464_project_watermarks.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.ytken.a464_project_watermarks.repository.MakeImageRepository
import ru.ytken.a464_project_watermarks.utilities.Coroutines

class MakeImageViewModel(
    private val makeImageRepository: MakeImageRepository
    ) : ViewModel() {

    private val saveImageDataState = MutableLiveData<SaveImageDataState>()
    val saveImageUIState: LiveData<SaveImageDataState> get() = saveImageDataState

    fun saveImage(bitmap: Bitmap) {
        Coroutines.io {
            kotlin.runCatching {
                emitSaveFilteredImageUiState(isLoading = true)
                makeImageRepository.saveImageToGallery(bitmap)
            }.onSuccess { savedImageUri ->
                emitSaveFilteredImageUiState(uri = savedImageUri)
            }.onFailure {
                emitSaveFilteredImageUiState(error = it.message.toString()) }
        }
    }

    private fun emitSaveFilteredImageUiState(
        isLoading: Boolean = true,
        uri: Uri? = null,
        error: String? = null
    ) {
        val dateState = SaveImageDataState(isLoading, uri, error)
        saveImageDataState.postValue(dateState)
    }

    data class SaveImageDataState(
        val isLoading: Boolean,
        val uri: Uri?,
        val error: String?
    )
}