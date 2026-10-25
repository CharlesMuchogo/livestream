package com.charlesmuchogo.livestream.presentation.match

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charlesmuchogo.livestream.domain.models.StreamKind
import com.charlesmuchogo.livestream.domain.models.StreamSource
import com.charlesmuchogo.livestream.presentation.components.ErrorState
import com.charlesmuchogo.livestream.presentation.components.LivePill
import com.charlesmuchogo.livestream.presentation.components.Skeleton
import com.charlesmuchogo.livestream.presentation.components.StreamPlayer
import com.charlesmuchogo.livestream.presentation.components.TeamCrest
import com.charlesmuchogo.livestream.ui.theme.Floodlight
import com.charlesmuchogo.livestream.utils.clockTime
import com.charlesmuchogo.livestream.utils.dayLabel
import com.charlesmuchogo.livestream.utils.untilLabel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val PAGE_PLAYER_WEIGHT = 1.4f

@Composable
fun MatchRoot(
    matchId: Long,
    title: String,
    onBack: () -> Unit,
    viewModel: MatchViewModel = koinViewModel(key = "match-$matchId") { parametersOf(matchId, title) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MatchScreen(
        state = state,
        onAction = { action -> if (action == MatchAction.OnBack) onBack() else viewModel.onAction(action) },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MatchScreen(state: MatchState, onAction: (MatchAction) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Floodlight.Ink)) {
        val landscape = maxWidth > maxHeight
        val fullscreen = landscape || state.isFullscreen
        BackHandler(enabled = state.isFullscreen) { onAction(MatchAction.OnFullscreenChange(false)) }

        if (fullscreen) {
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                PlayerSurface(state, onAction, Modifier.fillMaxSize())
                if (!landscape) {
                    IconButton(
                        onClick = { onAction(MatchAction.OnFullscreenChange(false)) },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.55f)),
                        modifier = Modifier.safeDrawingPadding().padding(8.dp).align(Alignment.TopStart),
                    ) { Icon(Icons.Filled.Close, contentDescription = "Exit full screen", tint = Color.White) }
                }
            }
            return@BoxWithConstraints
        }

        Column(Modifier.fillMaxSize()) {
            TopBar(state, onAction)
            val playerSize = if (state.current?.kind == StreamKind.Page && state.showFullPage) Modifier.weight(PAGE_PLAYER_WEIGHT) else Modifier.aspectRatio(16f / 9f)
            Box(Modifier.fillMaxWidth().then(playerSize).background(Color.Black)) {
                PlayerSurface(state, onAction, Modifier.fillMaxSize())
            }
            PlayerBar(state, onAction)
            AnimatedVisibility(state.notice != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().background(Floodlight.SurfaceRaised).padding(start = 16.dp, end = 4.dp),
                ) {
                    Text(state.notice.orEmpty(), style = MaterialTheme.typography.bodySmall, color = Floodlight.Text, modifier = Modifier.weight(1f).padding(vertical = 10.dp))
                    IconButton(onClick = { onAction(MatchAction.OnDismissNotice) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = Floodlight.TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
            LaunchedEffect(state.notice) {
                if (state.notice != null) {
                    kotlinx.coroutines.delay(6_000)
                    onAction(MatchAction.OnDismissNotice)
                }
            }
            SourcesList(state, onAction, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TopBar(state: MatchState, onAction: (MatchAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { onAction(MatchAction.OnBack) },
            colors = IconButtonDefaults.iconButtonColors(containerColor = Floodlight.Surface),
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        Column(Modifier.weight(1f).padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(state.title.uppercase(), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            state.match?.let { match ->
                val subtitle = buildString {
                    append(match.league)
                    match.startsAt?.let { append(if (match.isLive) " · since ${it.clockTime()}" else " · ${it.dayLabel()} ${it.clockTime()}") }
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Floodlight.TextMuted, maxLines = 1)
            }
        }
        IconButton(
            onClick = { onAction(MatchAction.OnToggleSave) },
            enabled = state.match != null,
            colors = IconButtonDefaults.iconButtonColors(containerColor = Floodlight.Surface),
        ) {
            Icon(
                if (state.isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (state.isSaved) "Remove from Saved" else "Save match",
                tint = if (state.isSaved) Floodlight.Live else Floodlight.Text,
            )
        }
    }
}

@Composable
private fun PlayerSurface(state: MatchState, onAction: (MatchAction) -> Unit, modifier: Modifier) {
    val current = state.current
    Box(modifier, contentAlignment = Alignment.Center) {
        when {
            state.isLoading && state.match == null -> CircularProgressIndicator(color = Floodlight.Volt)
            state.error != null -> Text(state.error, color = Floodlight.TextMuted, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
            current == null -> WaitingForLinks(state)
            else -> {
                key(current.id) {
                    StreamPlayer(
                        source = current,
                        focusPlayer = !state.showFullPage,
                        onReady = { onAction(MatchAction.OnPlayerReady) },
                        onFailed = { onAction(MatchAction.OnPlayerFailed) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                AnimatedVisibility(state.phase == PlayerPhase.Loading, enter = fadeIn(), exit = fadeOut()) {
                    Column(
                        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    ) {
                        CircularProgressIndicator(color = Floodlight.Volt, strokeWidth = 3.dp, modifier = Modifier.size(34.dp))
                        Text("Connecting to ${current.label}…", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC9CFDB))
                    }
                }
                AnimatedVisibility(state.phase == PlayerPhase.Failed, enter = fadeIn(), exit = fadeOut()) {
                    Column(
                        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    ) {
                        Text("${current.label} isn't working", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        val next = state.nextSource
                        if (next != null) {
                            Button(onClick = { onAction(MatchAction.OnTryNext) }) {
                                Icon(Icons.Filled.SkipNext, contentDescription = null)
                                Text("Try ${next.label}", modifier = Modifier.padding(start = 6.dp))
                            }
                        } else {
                            Text("That was the last source. New links usually appear within a few minutes.", style = MaterialTheme.typography.bodySmall, color = Floodlight.TextMuted, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaitingForLinks(state: MatchState) {
    val match = state.match ?: return
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            TeamCrest(match.homeTeam, match.homeLogo, size = 52.dp)
            if (!match.isSingleEvent) TeamCrest(match.awayTeam, match.awayLogo, size = 52.dp)
        }
        val kickoff = match.startsAt
        Text(
            when {
                match.isLive -> "No links yet for this match"
                kickoff != null -> "Kick-off ${kickoff.dayLabel().lowercase()} at ${kickoff.clockTime()} · ${kickoff.untilLabel()}"
                else -> "Kick-off time to be confirmed"
            },
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Floodlight.TextMuted, modifier = Modifier.size(16.dp))
            Text("Streams usually appear about 30 minutes before the start. Checking every minute.", style = MaterialTheme.typography.bodySmall, color = Floodlight.TextMuted)
        }
    }
}

@Composable
private fun PlayerBar(state: MatchState, onAction: (MatchAction) -> Unit) {
    val current = state.current ?: return
    Row(
        modifier = Modifier.fillMaxWidth().background(Floodlight.Surface).padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (state.match?.isLive == true) LivePill()
            Text(
                listOf(current.label, current.channel, current.quality).filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = Floodlight.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (current.kind == StreamKind.Page) {
            IconButton(onClick = { onAction(MatchAction.OnToggleFullPage) }) {
                Icon(
                    if (state.showFullPage) Icons.Filled.FitScreen else Icons.Filled.Web,
                    contentDescription = if (state.showFullPage) "Show the player only" else "Show the full page",
                )
            }
        }
        IconButton(onClick = { onAction(MatchAction.OnReportBroken) }) {
            Icon(Icons.Outlined.Flag, contentDescription = "This stream isn't working")
        }
        if (state.nextSource != null) {
            IconButton(onClick = { onAction(MatchAction.OnTryNext) }) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next source")
            }
        }
        IconButton(onClick = { onAction(MatchAction.OnFullscreenChange(true)) }) {
            Icon(Icons.Filled.Fullscreen, contentDescription = "Full screen")
        }
    }
}

@Composable
private fun SourcesList(state: MatchState, onAction: (MatchAction) -> Unit, modifier: Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (state.error != null && state.match == null) {
            item(key = "error") { ErrorState(state.error, onRetry = { onAction(MatchAction.OnRetry) }) }
            return@LazyColumn
        }
        if (state.isLoading && state.match == null) {
            items(5, key = { "source-skeleton-$it" }) { Skeleton(64.dp, Modifier.fillMaxWidth(), corner = 16.dp) }
            return@LazyColumn
        }
        if (state.streams.isEmpty()) return@LazyColumn

        item(key = "sources-title") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("SOURCES", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 6.dp))
                Text("${state.filteredSources.size}", style = MaterialTheme.typography.titleLarge, color = Floodlight.TextFaint, modifier = Modifier.weight(1f))
                Text("Verified only", style = MaterialTheme.typography.labelMedium, color = Color(0xFFC9CFDB), modifier = Modifier.padding(end = 8.dp))
                Switch(
                    checked = state.verifiedOnly,
                    onCheckedChange = { onAction(MatchAction.OnToggleVerifiedOnly) },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Floodlight.Volt,
                        checkedThumbColor = Floodlight.Ink,
                        uncheckedTrackColor = Floodlight.SurfaceRaised,
                        uncheckedThumbColor = Floodlight.Text,
                        uncheckedBorderColor = Floodlight.Outline,
                    ),
                )
            }
        }
        if (state.channels.size > 1) {
            item(key = "channels") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item(key = "all") { ChannelChip("All channels", state.streams.size, state.channel == null) { onAction(MatchAction.OnChannelSelected(null)) } }
                    items(state.channels, key = { it.name }) { channel ->
                        ChannelChip(channel.name, channel.count, state.channel == channel.name) { onAction(MatchAction.OnChannelSelected(channel.name)) }
                    }
                }
            }
        }
        item(key = "sort") {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SourceSort.entries.forEachIndexed { index, sort ->
                    SegmentedButton(
                        selected = state.sort == sort,
                        onClick = { onAction(MatchAction.OnSortSelected(sort)) },
                        shape = SegmentedButtonDefaults.itemShape(index, SourceSort.entries.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Floodlight.SurfaceRaised,
                            activeContentColor = Floodlight.Text,
                            inactiveContainerColor = Floodlight.Surface,
                            inactiveContentColor = Floodlight.TextMuted,
                            activeBorderColor = Floodlight.Outline,
                            inactiveBorderColor = Floodlight.Outline,
                        ),
                        icon = {},
                    ) { Text(if (sort == SourceSort.Recommended) "Recommended" else "Fewest ads", style = MaterialTheme.typography.labelLarge) }
                }
            }
        }
        if (state.filteredSources.isEmpty()) {
            item(key = "no-sources") {
                Text(
                    "No verified sources on this channel yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Floodlight.TextMuted,
                    modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, Floodlight.Outline), RoundedCornerShape(16.dp)).padding(20.dp),
                )
            }
        }
        items(state.visibleSources, key = { it.id }) { source ->
            SourceRow(
                source = source,
                isCurrent = source.id == state.currentStreamId,
                phase = state.phase,
                isBroken = source.id in state.brokenIds,
                onClick = { onAction(MatchAction.OnPlay(source)) },
                modifier = Modifier.animateItem(),
            )
        }
        if (!state.showAll && state.filteredSources.size > MatchState.COLLAPSED_SOURCE_COUNT) {
            item(key = "show-all") {
                OutlinedButton(onClick = { onAction(MatchAction.OnShowAll) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Show all ${state.filteredSources.size} sources")
                }
            }
        }
    }
}

@Composable
private fun ChannelChip(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text("$label  $count", style = MaterialTheme.typography.labelLarge) },
        shape = RoundedCornerShape(20.dp),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected, borderColor = Floodlight.Outline, selectedBorderColor = Floodlight.Text),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Floodlight.Surface,
            labelColor = Floodlight.Text,
            selectedContainerColor = Floodlight.Text,
            selectedLabelColor = Floodlight.Ink,
        ),
    )
}

@Composable
private fun SourceRow(
    source: StreamSource,
    isCurrent: Boolean,
    phase: PlayerPhase,
    isBroken: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = when {
        isCurrent && phase == PlayerPhase.Loading -> "Connecting…"
        isCurrent && phase == PlayerPhase.Failed -> "Not working"
        isCurrent -> "Now playing"
        isBroken -> "Reported not working"
        else -> null
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCurrent) Color(0xFF161C14) else Floodlight.Surface)
            .border(BorderStroke(1.5.dp, if (isCurrent) Floodlight.Volt else Floodlight.Divider), RoundedCornerShape(16.dp))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(if (isCurrent) Floodlight.Volt else Floodlight.SurfaceRaised),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = if (isCurrent) Floodlight.Ink else Floodlight.Text, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(source.label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (source.verified) Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = Floodlight.Verified, modifier = Modifier.size(16.dp))
            }
            Text(
                listOfNotNull(status, source.channel.ifBlank { null }, source.language.ifBlank { null }).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    isBroken || (isCurrent && phase == PlayerPhase.Failed) -> Floodlight.Warn
                    isCurrent -> Floodlight.Volt
                    else -> Floodlight.TextMuted
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                source.quality.ifBlank { "?" },
                style = MaterialTheme.typography.labelSmall,
                color = if (source.isHd) Floodlight.Ink else Color(0xFFC9CFDB),
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (source.isHd) Floodlight.Text else Floodlight.Outline).padding(horizontal = 7.dp, vertical = 3.dp),
            )
            Text(
                if (source.ads == 1) "1 ad" else "${source.ads} ads",
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    source.ads <= 1 -> Floodlight.Good
                    source.ads == 2 -> Color(0xFFC9CFDB)
                    else -> Floodlight.Warn
                },
            )
        }
    }
}
