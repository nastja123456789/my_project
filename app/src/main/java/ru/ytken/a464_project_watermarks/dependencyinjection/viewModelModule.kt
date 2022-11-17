package ru.ytken.a464_project_watermarks.dependencyinjection

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.ytken.a464_project_watermarks.viewmodels.MakeImageViewModel
import ru.ytken.a464_project_watermarks.viewmodels.SavedImagesViewModel

val viewModelModule = module {
    viewModel {
        SavedImagesViewModel(savedImagesRepository = get())
        MakeImageViewModel(makeImageRepository = get())
    }
}