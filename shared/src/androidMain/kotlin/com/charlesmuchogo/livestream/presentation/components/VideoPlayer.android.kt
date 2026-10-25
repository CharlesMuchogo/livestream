package com.charlesmuchogo.livestream.presentation.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

@Composable
actual fun VideoPlayer(url: String, modifier: Modifier, onReady: () -> Unit, onFailed: () -> Unit) {
    val context = LocalContext.current
    val ready by rememberUpdatedState(onReady)
    val failed by rememberUpdatedState(onFailed)
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) ready()
            }

            override fun onPlayerError(error: PlaybackException) {
                failed()
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { player.pause() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { player.play() }

    AndroidView(
        factory = { viewContext -> PlayerView(viewContext).apply { this.player = player } },
        update = { view -> view.player = player },
        modifier = modifier,
    )
}

private const val REVEAL_PROGRESS = 70

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun StreamWebView(url: String, focusPlayer: Boolean, modifier: Modifier, onReady: () -> Unit, onFailed: () -> Unit) {
    val ready by rememberUpdatedState(onReady)
    val failed by rememberUpdatedState(onFailed)
    val allowedHost = remember(url) { Uri.parse(url).host.orEmpty() }
    val holder = remember(url) { arrayOfNulls<WebView>(1) }

    val supportsFrameScripts = remember { WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT) }
    LaunchedEffect(focusPlayer) { holder[0]?.evaluateJavascript(playerFocusToggleScript(focusPlayer), null) }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { holder[0]?.onPause() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { holder[0]?.onResume() }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            FrameLayout(context).apply {
                setBackgroundColor(Color.BLACK)
                val container = this
                val webView = WebView(context).apply {
                    setBackgroundColor(Color.BLACK)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(true)
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            if (!request.isForMainFrame || request.isRedirect || sameSite(request.url.host, allowedHost)) return false
                            if (!request.hasGesture()) failed()
                            return true
                        }

                        override fun onPageFinished(view: WebView, finishedUrl: String?) {
                            if (!supportsFrameScripts) view.evaluateJavascript(PLAYER_FOCUS_SCRIPT, null)
                            ready()
                        }

                        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                            if (request.isForMainFrame) failed()
                        }

                        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                            if (request.isForMainFrame && errorResponse.statusCode >= 400) failed()
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        private var fullscreenView: View? = null

                        override fun onProgressChanged(view: WebView, newProgress: Int) {
                            if (newProgress >= REVEAL_PROGRESS) ready()
                        }

                        override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean = false

                        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                            fullscreenView?.let(container::removeView)
                            fullscreenView = view
                            container.addView(view, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
                        }

                        override fun onHideCustomView() {
                            fullscreenView?.let(container::removeView)
                            fullscreenView = null
                        }
                    }
                    if (supportsFrameScripts) WebViewCompat.addDocumentStartJavaScript(this, PLAYER_FOCUS_SCRIPT, setOf("*"))
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
                        WebViewCompat.addWebMessageListener(this, BRIDGE_NAME, setOf("*")) { _, message, _, _, _ ->
                            when (message.data) {
                                BRIDGE_PLAYING -> ready()
                                BRIDGE_BLOCKED -> failed()
                            }
                        }
                    }
                    loadUrl(url)
                }
                holder[0] = webView
                addView(webView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            }
        },
        onRelease = {
            holder[0]?.apply {
                stopLoading()
                destroy()
            }
            holder[0] = null
        },
    )
}

@Composable
actual fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
