package ru.ytken.a464_project_watermarks.dependencyinjection

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.ytken.a464_project_watermarks.repository.SavedImageRepository
import ru.ytken.a464_project_watermarks.repository.SavedImageRepositoryImpl
import ru.ytken.a464_project_watermarks.repository.TakedImageRepository
import ru.ytken.a464_project_watermarks.repository.TakedImageRepositoryImpl
import ru.ytken.a464_project_watermarks.ui.MainActivity
import ru.ytken.a464_project_watermarks.ui.fragments.ButtonFragment

val repositorymodule = module {
    factory<SavedImageRepository> { SavedImageRepositoryImpl(
        androidContext(),
        MainActivity.scanbotSDK,
        MainActivity.pageFileStorage,
        MainActivity.pageProcess,
        MainActivity.bm
    ) }
    factory<TakedImageRepository> { TakedImageRepositoryImpl(
        androidContext(),
        ButtonFragment.pageFileStorage
    ) }
}
