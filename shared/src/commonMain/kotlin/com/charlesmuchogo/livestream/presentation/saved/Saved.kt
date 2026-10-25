package com.charlesmuchogo.livestream.presentation.saved

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charlesmuchogo.livestream.domain.models.League
import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.presentation.components.ErrorState
import com.charlesmuchogo.livestream.presentation.components.LivePill
import com.charlesmuchogo.livestream.presentation.components.Skeleton
import com.charlesmuchogo.livestream.presentation.components.TeamCrest
import com.charlesmuchogo.livestream.presentation.components.streamsLabel
import com.charlesmuchogo.livestream.presentation.schedule.sportTone
import com.charlesmuchogo.livestream.ui.theme.Floodlight
import com.charlesmuchogo.livestream.utils.clockTime
import com.charlesmuchogo.livestream.utils.dayLabel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SavedRoot(
    onOpenMatch: (Match) -> Unit,
    onBrowseLive: () -> Unit,
    viewModel: SavedViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    SavedScreen(
        state = state,
        snackbar = snackbar,
        onAction = { action ->
            when (action) {
                is SavedAction.OnMatchClick -> onOpenMatch(action.match)
                SavedAction.OnBrowseLive -> onBrowseLive()
                is SavedAction.OnRemove -> {
                    viewModel.onAction(action)
                    scope.launch {
                        snackbar.currentSnackbarData?.dismiss()
                        val result = snackbar.showSnackbar("Removed ${action.match.title}", actionLabel = "Undo", duration = SnackbarDuration.Short)
                        if (result == SnackbarResult.ActionPerformed) viewModel.onAction(SavedAction.OnUndoRemove(action.match))
                    }
                }
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
fun SavedScreen(state: SavedState, snackbar: SnackbarHostState, onAction: (SavedAction) -> Unit) {
    Scaffold(containerColor = Floodlight.Ink, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = padding.calculateTopPadding() + 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "title") { Text("SAVED", style = MaterialTheme.typography.displaySmall) }
            item(key = "tabs") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    SavedTab.entries.forEachIndexed { index, tab ->
                        val count = if (tab == SavedTab.Matches) state.matches.size else state.followedIds.size
                        SegmentedButton(
                            selected = state.tab == tab,
                            onClick = { onAction(SavedAction.OnTabSelected(tab)) },
                            shape = SegmentedButtonDefaults.itemShape(index, SavedTab.entries.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Floodlight.Text,
                                activeContentColor = Floodlight.Ink,
                                inactiveContainerColor = Floodlight.Surface,
                                inactiveContentColor = Floodlight.TextMuted,
                                activeBorderColor = Floodlight.Outline,
                                inactiveBorderColor = Floodlight.Outline,
                            ),
                            icon = {},
                        ) { Text("${tab.name}  $count", style = MaterialTheme.typography.labelLarge) }
                    }
                }
            }
            when (state.tab) {
                SavedTab.Matches -> savedMatches(state, onAction)
                SavedTab.Leagues -> leagues(state, onAction)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.savedMatches(state: SavedState, onAction: (SavedAction) -> Unit) {
    if (state.matches.isEmpty()) {
        item(key = "empty") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, Floodlight.Outline), RoundedCornerShape(20.dp)).padding(32.dp),
            ) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = Floodlight.TextFaint, modifier = Modifier.size(36.dp))
                Text("Nothing saved yet", style = MaterialTheme.typography.titleMedium)
                Text("Tap the heart on any match to keep it here.", style = MaterialTheme.typography.bodyMedium, color = Floodlight.TextMuted, textAlign = TextAlign.Center)
                Button(onClick = { onAction(SavedAction.OnBrowseLive) }) { Text("Browse live matches") }
            }
        }
        return
    }
    items(state.matches, key = { it.id }) { match ->
        SavedMatchCard(match, onAction, Modifier.animateItem())
    }
}

@Composable
private fun SavedMatchCard(match: Match, onAction: (SavedAction) -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Floodlight.Surface)
            .border(BorderStroke(1.dp, Floodlight.Divider), RoundedCornerShape(20.dp))
            .clickable { onAction(SavedAction.OnMatchClick(match)) }
            .padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            TeamCrest(match.homeTeam, match.homeLogo, size = 30.dp)
            if (!match.isSingleEvent) TeamCrest(match.awayTeam, match.awayLogo, size = 30.dp)
        }
        Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(match.league.uppercase(), style = MaterialTheme.typography.labelSmall, color = Floodlight.TextMuted)
            Text(match.homeTeam, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!match.isSingleEvent) Text(match.awayTeam, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (match.isLive) {
                LivePill("LIVE · ${streamsLabel(match.streamCount).uppercase()}")
            } else {
                Text(
                    match.startsAt?.let { "${it.dayLabel()} · ${it.clockTime()}" } ?: "Time to be confirmed",
                    style = MaterialTheme.typography.bodySmall,
                    color = Floodlight.TextMuted,
                )
            }
        }
        IconButton(onClick = { onAction(SavedAction.OnRemove(match)) }) {
            Icon(Icons.Filled.Favorite, contentDescription = "Remove ${match.title} from Saved", tint = Floodlight.Live)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.leagues(state: SavedState, onAction: (SavedAction) -> Unit) {
    item(key = "leagues-intro") {
        Text(
            "Live matches from leagues you follow show first on the Live tab.",
            style = MaterialTheme.typography.bodySmall,
            color = Floodlight.TextMuted,
        )
    }
    item(key = "leagues-filter") {
        OutlinedTextField(
            value = state.leagueFilter,
            onValueChange = { onAction(SavedAction.OnLeagueFilterChange(it)) },
            placeholder = { Text("Find a league") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Floodlight.Surface,
                focusedContainerColor = Floodlight.Surface,
                unfocusedBorderColor = Floodlight.Outline,
                focusedBorderColor = Floodlight.Volt,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    when {
        state.leaguesError != null && state.leagues.isEmpty() -> item(key = "leagues-error") {
            ErrorState(state.leaguesError, onRetry = { onAction(SavedAction.OnRetryLeagues) })
        }
        state.leaguesLoading && state.leagues.isEmpty() -> items(6, key = { "league-skeleton-$it" }) {
            Skeleton(64.dp, Modifier.fillMaxWidth(), corner = 18.dp)
        }
        else -> items(state.visibleLeagues, key = { "league-${it.id}" }) { league ->
            LeagueRow(league, followed = league.id in state.followedIds, onToggle = { onAction(SavedAction.OnToggleFollow(league)) }, modifier = Modifier.animateItem())
        }
    }
}

@Composable
private fun LeagueRow(league: League, followed: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Floodlight.Surface)
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(sportTone(league.sport)))
        Column(Modifier.weight(1f)) {
            Text(league.name, style = MaterialTheme.typography.titleSmall)
            val detail = buildString {
                append(league.sportLabel)
                append(" · ")
                if (league.live > 0) append("${league.live} live · ")
                append("${league.matches} listed")
            }
            Text(detail, style = MaterialTheme.typography.bodySmall, color = Floodlight.TextMuted)
        }
        Switch(
            checked = followed,
            onCheckedChange = { onToggle() },
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
