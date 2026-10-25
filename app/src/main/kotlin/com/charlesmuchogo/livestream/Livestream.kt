package com.charlesmuchogo.livestream

import android.app.Application
import android.content.pm.ApplicationInfo
import android.webkit.WebView
import com.charlesmuchogo.livestream.di.KoinInit
import org.koin.android.ext.koin.androidContext

class Livestream : Application() {
    override fun onCreate() {
        super.onCreate()
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        KoinInit().init {
            androidContext(this@Livestream)
        }
    }
}
