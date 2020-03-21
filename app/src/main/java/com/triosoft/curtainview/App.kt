package com.triosoft.curtainview

import android.app.Application
import com.triosoft.android.viewslibrary.BuildConfig
import timber.log.Timber

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String? {
                    return super.createStackElementTag(element) + " : " + element.lineNumber
                }
            })
        }
    }
}