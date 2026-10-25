package com.charlesmuchogo.livestream

import androidx.compose.ui.window.ComposeUIViewController
import com.charlesmuchogo.livestream.di.KoinInit
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.UIKit.UIViewController

private var appStarted = false

@OptIn(ExperimentalForeignApi::class)
fun startApp() {
    if (appStarted) return
    appStarted = true
    AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)
    KoinInit().init()
}

fun MainViewController(): UIViewController {
    startApp()
    return ComposeUIViewController { App() }
}
