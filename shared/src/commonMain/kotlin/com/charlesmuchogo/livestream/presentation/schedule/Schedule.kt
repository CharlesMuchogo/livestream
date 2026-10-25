package com.charlesmuchogo.livestream.presentation.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.presentation.components.EmptyNote
import com.charlesmuchogo.livestream.presentation.components.ErrorState
import com.charlesmuchogo.livestream.presentation.components.FixtureRow
import com.charlesmuchogo.livestream.presentation.components.Skeleton
import com.charlesmuchogo.livestream.ui.theme.Floodlight
import com.charlesmuchogo.livestream.utils.dayLabel
import com.charlesmuchogo.livestream.utils.scheduleDayParts
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScheduleRoot(
    focusSearch: Boolean,
    onOpenMatch: (Match) -> Unit,
    viewModel: ScheduleViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScheduleScreen(
        state = state,
        focusSearch = focusSearch,
        onAction = { action ->
            if (action is ScheduleAction.OnMatchClick) onOpenMatch(action.match) else viewModel.onAction(action)
        },
    )
}

@Composable
fun ScheduleScreen(state: ScheduleState, focusSearch: Boolean, onAction: (ScheduleAction) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusSearch) { if (focusSearch) focusRequester.requestFocus() }

    Scaffold(containerColor = Floodlight.Ink) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = padding.calculateTopPadding() + 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "title") {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("SCHEDULE", style = MaterialTheme.typography.displaySmall, modifier = Modifier.weight(1f))
                    Text("Your local time", style = MaterialTheme.typography.bodySmall, color = Floodlight.TextMuted)
                }
            }
            item(key = "search") {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { onAction(ScheduleAction.OnQueryChange(it)) },
                    placeholder = { Text("Search teams or leagues") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = if (state.query.isNotEmpty()) {
                        { IconButton(onClick = { onAction(ScheduleAction.OnQueryChange("")) }) { Icon(Icons.Filled.Close, contentDescription = "Clear search") } }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Floodlight.Surface,
                        focusedContainerColor = Floodlight.Surface,
                        unfocusedBorderColor = Floodlight.Outline,
                        focusedBorderColor = Floodlight.Volt,
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )
            }
            if (!state.isSearching && state.days.isNotEmpty()) {
                item(key = "days") { DayPicker(state, onAction) }
            }
            if (state.isSearching) {
                item(key = "search-title") {
                    Text("Results across the next 7 days", style = MaterialTheme.typography.bodySmall, color = Floodlight.TextMuted)
                }
            }

            when {
                state.error != null -> item(key = "error") { ErrorState(state.error, onRetry = { onAction(ScheduleAction.OnRetry) }) }
                state.isLoading -> items(4, key = { "skeleton-$it" }) { Skeleton(120.dp, Modifier.fillMaxWidth()) }
                state.groups.isEmpty() -> item(key = "empty") {
                    EmptyNote(if (state.isSearching) "No matches for “${state.query.trim()}”." else "No matches listed for this day yet.")
                }
                else -> items(state.groups, key = { it.key }) { group ->
                    LeagueSection(
                        group = group,
                        expanded = group.key !in state.collapsed,
                        savedIds = state.savedIds,
                        showDay = state.isSearching,
                        onAction = onAction,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayPicker(state: ScheduleState, onAction: (ScheduleAction) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.days, key = { it.date }) { day ->
            val selected = day.date == state.selectedDate
            val (weekday, date) = scheduleDayParts(day.date)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .width(72.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) Floodlight.Volt else Floodlight.Surface)
                    .border(BorderStroke(1.dp, if (selected) Floodlight.Volt else Floodlight.Outline), RoundedCornerShape(18.dp))
                    .clickable(role = Role.Tab) { onAction(ScheduleAction.OnDaySelected(day.date)) },
            ) {
                val ink = if (selected) Floodlight.Ink else Floodlight.Text
                Text(weekday.uppercase(), style = MaterialTheme.typography.labelSmall, color = ink.copy(alpha = 0.8f))
                Text(date, style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.headlineSmall.fontSize), color = ink)
                Text(if (day.live > 0) "${day.live} live" else "${day.count}", style = MaterialTheme.typography.bodySmall, color = if (day.live > 0 && !selected) Floodlight.LiveText else ink.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun LeagueSection(
    group: LeagueGroup,
    expanded: Boolean,
    savedIds: Set<Long>,
    showDay: Boolean,
    onAction: (ScheduleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Floodlight.Surface)
            .border(BorderStroke(1.dp, Floodlight.Divider), RoundedCornerShape(20.dp)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { onAction(ScheduleAction.OnToggleGroup(group.key)) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(sportTone(group.sport)))
            Column(Modifier.weight(1f)) {
                Text(group.league, style = MaterialTheme.typography.titleSmall)
                val summary = buildString {
                    append(group.sportLabel)
                    append(" · ")
                    if (group.liveCount > 0) append("${group.liveCount} live · ")
                    append(if (group.matches.size == 1) "1 match" else "${group.matches.size} matches")
                }
                Text(summary, style = MaterialTheme.typography.bodySmall, color = Floodlight.TextMuted)
            }
            Icon(
                Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse ${group.league}" else "Expand ${group.league}",
                tint = Floodlight.TextMuted,
                modifier = Modifier.rotate(rotation),
            )
        }
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(start = 16.dp, end = 4.dp)) {
                group.matches.forEach { match ->
                    HorizontalDivider(color = Floodlight.Divider)
                    if (showDay && !match.isLive) {
                        match.startsAt?.let { Text(it.dayLabel().uppercase(), style = MaterialTheme.typography.labelSmall, color = Floodlight.TextFaint, modifier = Modifier.padding(top = 10.dp)) }
                    }
                    FixtureRow(
                        match = match,
                        saved = match.id in savedIds,
                        showSport = false,
                        onOpen = { onAction(ScheduleAction.OnMatchClick(match)) },
                        onToggleSave = { onAction(ScheduleAction.OnToggleSave(match)) },
                    )
                }
            }
        }
    }
}

fun sportTone(sport: String) = when (sport) {
    "soccer" -> Floodlight.Volt
    "ice-hockey" -> Floodlight.Verified
    "basketball" -> Floodlight.Warn
    "american-football", "ncaa" -> androidx.compose.ui.graphics.Color(0xFFFF8A65)
    "cricket" -> Floodlight.Good
    else -> Floodlight.TextMuted
}
