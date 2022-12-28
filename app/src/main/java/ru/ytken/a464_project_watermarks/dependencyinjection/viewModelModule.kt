package ru.ytken.a464_project_watermarks.dependencyinjection

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.ytken.a464_project_watermarks.viewmodels.SavedImagesViewModel
import ru.ytken.a464_project_watermarks.viewmodels.TakedImagesViewModel

val viewModelModule = module {
    viewModel {
        SavedImagesViewModel(savedImagesRepository = get())
        TakedImagesViewModel(takedImagesRepository = get())
    }
}