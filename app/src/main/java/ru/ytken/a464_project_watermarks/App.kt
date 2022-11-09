package ru.ytken.a464_project_watermarks

import android.app.Application
import android.util.Log
import io.scanbot.sap.IScanbotSDKLicenseErrorHandler
import io.scanbot.sap.SdkFeature
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.core.contourdetector.ContourDetector
import io.scanbot.sdk.util.log.LoggerProvider

class App: Application() {

    override fun onCreate() {
        super.onCreate()
//        System.loadLibrary("opencv_java4")

        ScanbotSDKInitializer()
            .license(this, LICENSE_KEY) // Please add a valid trial license key here. See the notes below!
            .contourDetectorType(ContourDetector.Type.ML_BASED)
            .prepareOCRLanguagesBlobs(true)
            .initialize(this)
    }

    companion object {
        private val LICENSE_KEY =
            "SQYpgpY/b7bm+xECMbxeZfmwgZVZ8F" +
                    "qnT+38yD8nbtNzSqgdyL6rkUXNjrtE" +
                    "ItPFz4swB8b0JJMHCjrRPZeS2L4Ld+" +
                    "62l5jEE8l3gLHy4ErM44Ikv3eYjAJh" +
                    "Z2GghZa48tI8ZH9qIRt0YmnMtB63Tx" +
                    "YlAHTS3OadOvYrQO/xUeJlX7z4Gged" +
                    "QyfT0Yt7H6gvFXAulZSo9lhFtt+dHG" +
                    "gEiVSdQhOMYZJyP11Q6+rGBLz16CmJ" +
                    "mhBU6KSj1JDQLuRQ3GZaws0ZWiWaFl" +
                    "TB8ppVZ/FcTF3PnNKpqKgdmxZThrsU" +
                    "n1KLp6mwUe20++2iCFNMy/Aw989f9i" +
                    "YJ6G4bV99ojw==\nU2NhbmJvdFNESw" +
                    "pydS55dGtlbi5hNDY0X3Byb2plY3Rf" +
                    "d2F0ZXJtYXJrcwoxNjY4NDcwMzk5Cj" +
                    "gzODg2MDcKMTk=\n"
    }
}