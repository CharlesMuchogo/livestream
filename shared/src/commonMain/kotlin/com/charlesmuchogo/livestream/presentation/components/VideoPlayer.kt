package com.charlesmuchogo.livestream.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.charlesmuchogo.livestream.domain.models.StreamKind
import com.charlesmuchogo.livestream.domain.models.StreamSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.delay

const val PAGE_REVEAL_TIMEOUT_MILLIS = 8_000L

@Composable
expect fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    onReady: () -> Unit = {},
    onFailed: () -> Unit = {},
)

@Composable
expect fun StreamWebView(
    url: String,
    focusPlayer: Boolean,
    modifier: Modifier = Modifier,
    onReady: () -> Unit = {},
    onFailed: () -> Unit = {},
)

@Composable
expect fun KeepScreenOn()

@Composable
fun StreamPlayer(
    source: StreamSource,
    focusPlayer: Boolean,
    onReady: () -> Unit,
    onFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KeepScreenOn()
    val ready by rememberUpdatedState(onReady)
    if (source.kind == StreamKind.Page) {
        LaunchedEffect(source.id) {
            delay(PAGE_REVEAL_TIMEOUT_MILLIS)
            ready()
        }
    }
    when (source.kind) {
        StreamKind.Direct -> VideoPlayer(url = source.url, modifier = modifier, onReady = onReady, onFailed = onFailed)
        StreamKind.Page -> StreamWebView(url = source.url, focusPlayer = focusPlayer, modifier = modifier, onReady = onReady, onFailed = onFailed)
    }
}
