package ru.ytken.a464_project_watermarks

import android.app.Application
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.core.contourdetector.ContourDetector

@Suppress("unused")
class App: Application() {
    override fun onCreate() {
        super.onCreate()

        ScanbotSDKInitializer()
            .contourDetectorType(ContourDetector.Type.ML_BASED)
            .prepareOCRLanguagesBlobs(true)
            .useCameraXRtuUi(true)
            .initialize(this)
    }
}