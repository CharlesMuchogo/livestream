package com.charlesmuchogo.livestream.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.delay
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.currentItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVKit.AVPlayerViewController
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.WebKit.WKAudiovisualMediaTypeNone
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKNavigationTypeLinkActivated
import platform.WebKit.WKUIDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTime
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWindowFeatures
import platform.darwin.NSObject

private const val READY_TIMEOUT_MILLIS = 20_000

@Composable
actual fun VideoPlayer(url: String, modifier: Modifier, onReady: () -> Unit, onFailed: () -> Unit) {
    val ready by rememberUpdatedState(onReady)
    val failed by rememberUpdatedState(onFailed)
    val player = remember(url) { NSURL.URLWithString(url)?.let { AVPlayer(uRL = it) } }
    val controller = remember(player) {
        AVPlayerViewController().apply {
            this.player = player
            showsPlaybackControls = true
        }
    }

    LaunchedEffect(player) {
        if (player == null) {
            failed()
            return@LaunchedEffect
        }
        var waited = 0
        while (waited < READY_TIMEOUT_MILLIS) {
            when (player.currentItem?.status) {
                AVPlayerItemStatusReadyToPlay -> {
                    ready()
                    return@LaunchedEffect
                }
                AVPlayerItemStatusFailed -> {
                    failed()
                    return@LaunchedEffect
                }
            }
            delay(300)
            waited += 300
        }
        failed()
    }
    DisposableEffect(player) {
        player?.play()
        onDispose { player?.pause() }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { player?.pause() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { player?.play() }

    UIKitViewController(factory = { controller }, modifier = modifier)
}

private class BridgeHandler(var onMessage: (String) -> Unit) : NSObject(), WKScriptMessageHandlerProtocol {
    override fun userContentController(userContentController: WKUserContentController, didReceiveScriptMessage: WKScriptMessage) {
        (didReceiveScriptMessage.body as? String)?.let(onMessage)
    }
}

private class StreamWebDelegate(
    private val allowedHost: String,
    var onReady: () -> Unit,
    var onFailed: () -> Unit,
) : NSObject(), WKNavigationDelegateProtocol, WKUIDelegateProtocol {
    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        val action = decidePolicyForNavigationAction
        val targetFrame = action.targetFrame
        val leavesSite = targetFrame?.mainFrame == true && !sameSite(action.request.URL?.host, allowedHost)
        if (leavesSite && action.navigationType != WKNavigationTypeLinkActivated) onFailed()
        decisionHandler(if (targetFrame == null || leavesSite) WKNavigationActionPolicy.WKNavigationActionPolicyCancel else WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
    }

    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        onReady()
    }

    override fun webView(webView: WKWebView, didFailProvisionalNavigation: WKNavigation?, withError: NSError) {
        onFailed()
    }

    override fun webView(
        webView: WKWebView,
        createWebViewWithConfiguration: WKWebViewConfiguration,
        forNavigationAction: WKNavigationAction,
        windowFeatures: WKWindowFeatures,
    ): WKWebView? = null
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun StreamWebView(url: String, focusPlayer: Boolean, modifier: Modifier, onReady: () -> Unit, onFailed: () -> Unit) {
    val ready by rememberUpdatedState(onReady)
    val failed by rememberUpdatedState(onFailed)
    val delegate = remember(url) {
        StreamWebDelegate(
            allowedHost = NSURL.URLWithString(url)?.host.orEmpty(),
            onReady = {},
            onFailed = {},
        )
    }
    delegate.onReady = { ready() }
    delegate.onFailed = { failed() }
    val bridge = remember(url) { BridgeHandler {} }
    bridge.onMessage = { message ->
        when (message) {
            BRIDGE_PLAYING -> ready()
            BRIDGE_BLOCKED -> failed()
        }
    }

    val webView = remember(url) {
        val configuration = WKWebViewConfiguration().apply {
            allowsInlineMediaPlayback = true
            mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeNone
            preferences.javaScriptCanOpenWindowsAutomatically = false
            userContentController.addScriptMessageHandler(bridge, name = BRIDGE_NAME)
            userContentController.addUserScript(
                WKUserScript(
                    source = PLAYER_FOCUS_SCRIPT,
                    injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentEnd,
                    forMainFrameOnly = false,
                ),
            )
        }
        WKWebView(frame = CGRectZero.readValue(), configuration = configuration).apply {
            navigationDelegate = delegate
            UIDelegate = delegate
            opaque = false
            backgroundColor = UIColor.blackColor
            scrollView.backgroundColor = UIColor.blackColor
            scrollView.bounces = false
            NSURL.URLWithString(url)?.let { loadRequest(NSURLRequest(uRL = it)) }
        }
    }
    LaunchedEffect(webView, focusPlayer) { webView.evaluateJavaScript(playerFocusToggleScript(focusPlayer), completionHandler = null) }
    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.configuration.userContentController.removeScriptMessageHandlerForName(BRIDGE_NAME)
            webView.navigationDelegate = null
            webView.UIDelegate = null
        }
    }

    UIKitView(factory = { webView }, modifier = modifier)
}

@Composable
actual fun KeepScreenOn() {
    DisposableEffect(Unit) {
        UIApplication.sharedApplication.idleTimerDisabled = true
        onDispose { UIApplication.sharedApplication.idleTimerDisabled = false }
    }
}
