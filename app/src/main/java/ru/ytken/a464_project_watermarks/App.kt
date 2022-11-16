package ru.ytken.a464_project_watermarks

import android.app.Application
import android.util.Log
import io.scanbot.sap.IScanbotSDKLicenseErrorHandler
import io.scanbot.sap.SdkFeature
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.core.contourdetector.ContourDetector
import io.scanbot.sdk.util.log.LoggerProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.ytken.a464_project_watermarks.dependencyinjection.repositorymodule
import ru.ytken.a464_project_watermarks.dependencyinjection.viewModelModule

@Suppress("unused")
class App: Application() {
    override fun onCreate() {
        super.onCreate()

        ScanbotSDKInitializer()
            .contourDetectorType(ContourDetector.Type.ML_BASED)
            .prepareOCRLanguagesBlobs(true)
            .initialize(this)
        startKoin {
            androidContext(this@App)
            modules(listOf(repositorymodule, viewModelModule))
        }
    }
}