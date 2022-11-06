package ru.ytken.a464_project_watermarks

import android.app.Application

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("opencv_java4")
    }

}