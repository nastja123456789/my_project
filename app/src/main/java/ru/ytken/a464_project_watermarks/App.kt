package ru.ytken.a464_project_watermarks

import android.app.Application
import io.scanbot.sdk.ScanbotSDKInitializer
import io.scanbot.sdk.core.contourdetector.ContourDetector
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.ytken.a464_project_watermarks.dependencyinjection.repositorymodule
import ru.ytken.a464_project_watermarks.dependencyinjection.viewModelModule

@Suppress("unused")
class App: Application() {
    override fun onCreate() {
        super.onCreate()

        ScanbotSDKInitializer()
            //.license(this, LICENSE_KEY)
            .contourDetectorType(ContourDetector.Type.ML_BASED)
            .prepareOCRLanguagesBlobs(true)
            .initialize(this)
        startKoin {
            androidContext(this@App)
            modules(listOf(repositorymodule, viewModelModule))
        }
    }
    companion object {
        val LICENSE_KEY =
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
//
//        //ScanbotSDKInitializer().license(this, LICENSE_KEY).initialize(this)
    }
}