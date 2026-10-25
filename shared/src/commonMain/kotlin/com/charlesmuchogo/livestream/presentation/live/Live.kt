package com.charlesmuchogo.livestream.presentation.live

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.domain.models.SportFilter
import com.charlesmuchogo.livestream.presentation.components.EmptyNote
import com.charlesmuchogo.livestream.presentation.components.ErrorState
import com.charlesmuchogo.livestream.presentation.components.FixtureRow
import com.charlesmuchogo.livestream.presentation.components.LiveMatchCard
import com.charlesmuchogo.livestream.presentation.components.PulsingDot
import com.charlesmuchogo.livestream.presentation.components.Skeleton
import com.charlesmuchogo.livestream.ui.theme.Floodlight
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LiveRoot(
    onOpenMatch: (Match) -> Unit,
    onSearch: () -> Unit,
    viewModel: LiveViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { snackbar.currentSnackbarData?.dismiss(); snackbar.showSnackbar(it) }
    }
    LifecycleResumeEffect(viewModel) {
        viewModel.onAction(LiveAction.OnResume)
        onPauseOrDispose { }
    }
    LiveScreen(
        state = state,
        snackbar = snackbar,
        onAction = { action ->
            when (action) {
                is LiveAction.OnMatchClick -> onOpenMatch(action.match)
                LiveAction.OnSearchClick -> onSearch()
                else -> viewModel.onAction(action)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScreen(state: LiveState, snackbar: SnackbarHostState, onAction: (LiveAction) -> Unit) {
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, containerColor = Floodlight.Ink) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(LiveAction.OnRefresh) },
            modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "header") { Header(onSearch = { onAction(LiveAction.OnSearchClick) }) }
                item(key = "chips") { SportChips(state, onAction) }

                if (state.error != null && state.live.isEmpty() && state.upNext.isEmpty()) {
                    item(key = "error") {
                        ErrorState(state.error, onRetry = { onAction(LiveAction.OnRefresh) }, modifier = Modifier.padding(20.dp))
                    }
                    return@LazyColumn
                }

                item(key = "live-title") { LiveTitle(state) }
                item(key = "live-row") { LiveRow(state, onAction) }
                item(key = "up-next-title") {
                    Text(
                        "Coming up",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 4.dp),
                    )
                }
                if (state.isLoading) {
                    items(5, key = { "fixture-skeleton-$it" }) {
                        Skeleton(56.dp, Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp), corner = 14.dp)
                    }
                } else if (state.upNext.isEmpty()) {
                    item(key = "up-next-empty") {
                        EmptyNote("Nothing else in ${state.selectedSportLabel} for the next 24 hours.", Modifier.padding(20.dp))
                    }
                } else {
                    items(state.upNext, key = { "up-${it.id}" }) { match ->
                        Column(modifier = Modifier.padding(horizontal = 20.dp).animateItem()) {
                            FixtureRow(
                                match = match,
                                saved = match.id in state.savedIds,
                                showSport = state.selectedSport == SportFilter.ALL,
                                onOpen = { onAction(LiveAction.OnMatchClick(match)) },
                                onToggleSave = { onAction(LiveAction.OnToggleSave(match)) },
                            )
                            HorizontalDivider(color = Floodlight.Divider)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(onSearch: () -> Unit) {
    val today = remember {
        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } + " " + date.day + " " +
            date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(today.uppercase(), style = MaterialTheme.typography.labelSmall, color = Floodlight.TextMuted)
            Text(
                buildAnnotatedString {
                    append("LIVESTREAM")
                    withStyle(SpanStyle(color = Floodlight.Volt)) { append(".") }
                },
                style = MaterialTheme.typography.displaySmall,
            )
        }
        IconButton(
            onClick = onSearch,
            colors = IconButtonDefaults.iconButtonColors(containerColor = Floodlight.Surface),
            modifier = Modifier.size(48.dp),
        ) {
            Icon(Icons.Filled.Search, contentDescription = "Search matches")
        }
    }
}

@Composable
private fun SportChips(state: LiveState, onAction: (LiveAction) -> Unit) {
    val sports = state.sports.ifEmpty { listOf(SportFilter(SportFilter.ALL, "All", 0, 0)) }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 6.dp),
    ) {
        items(sports, key = { it.id }) { sport ->
            val selected = sport.id == state.selectedSport
            FilterChip(
                selected = selected,
                onClick = { onAction(LiveAction.OnSportSelected(sport.id)) },
                label = { Text(sport.label, style = MaterialTheme.typography.labelLarge) },
                trailingIcon = if (sport.live > 0) {
                    {
                        Badge(
                            containerColor = if (selected) Floodlight.Ink else Floodlight.Live,
                            contentColor = if (selected) Floodlight.Volt else Floodlight.Ink,
                        ) { Text(sport.live.toString()) }
                    }
                } else null,
                shape = RoundedCornerShape(20.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = Floodlight.Outline,
                    selectedBorderColor = Floodlight.Volt,
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Floodlight.Surface,
                    labelColor = Floodlight.Text,
                    selectedContainerColor = Floodlight.Volt,
                    selectedLabelColor = Floodlight.Ink,
                ),
            )
        }
    }
}

@Composable
private fun LiveTitle(state: LiveState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PulsingDot(size = 9.dp)
        Text("Live now", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        if (!state.isLoading) {
            Text("${state.liveCount} on now", style = MaterialTheme.typography.bodyMedium, color = Floodlight.TextMuted)
        }
    }
}

@Composable
private fun LiveRow(state: LiveState, onAction: (LiveAction) -> Unit) {
    when {
        state.isLoading -> LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false,
        ) {
            items(2) { Skeleton(188.dp, Modifier.width(272.dp), corner = 22.dp) }
        }
        state.live.isEmpty() -> Box(Modifier.padding(horizontal = 20.dp)) {
            EmptyNote("Nothing live in ${state.selectedSportLabel} right now. Next up below.")
        }
        else -> LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.live, key = { it.id }) { match ->
                LiveMatchCard(
                    match = match,
                    saved = match.id in state.savedIds,
                    onOpen = { onAction(LiveAction.OnMatchClick(match)) },
                    onToggleSave = { onAction(LiveAction.OnToggleSave(match)) },
                )
            }
        }
    }
}
