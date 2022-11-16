package ru.ytken.a464_project_watermarks.dependencyinjection

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.ytken.a464_project_watermarks.repository.SavedImageRepository
import ru.ytken.a464_project_watermarks.repository.SavedImageRepositoryImpl

val repositorymodule = module {
    factory<SavedImageRepository> { SavedImageRepositoryImpl(androidContext()) }
}