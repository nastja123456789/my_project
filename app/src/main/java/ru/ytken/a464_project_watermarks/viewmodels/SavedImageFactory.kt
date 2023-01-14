package ru.ytken.a464_project_watermarks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.ytken.a464_project_watermarks.ui.ImageResultFragmentViewModel
import ru.ytken.a464_project_watermarks.ui.SeeScanFragmentViewModel

class SavedImageFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ImageResultFragmentViewModel::class.java)){
            return ImageResultFragmentViewModel() as T
        }
        if(modelClass.isAssignableFrom(SeeScanFragmentViewModel::class.java)){
            return SeeScanFragmentViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}