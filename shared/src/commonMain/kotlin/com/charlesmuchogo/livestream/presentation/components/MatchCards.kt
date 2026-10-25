package com.charlesmuchogo.livestream.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.ui.theme.Floodlight
import com.charlesmuchogo.livestream.utils.clockTime
import com.charlesmuchogo.livestream.utils.untilLabel

@Composable
fun LiveMatchCard(match: Match, saved: Boolean, onOpen: () -> Unit, onToggleSave: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onOpen,
        modifier = modifier.width(272.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Floodlight.Surface),
        border = BorderStroke(1.dp, Floodlight.Outline),
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    match.league.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Floodlight.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 40.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TeamLine(match.homeTeam, match.homeLogo)
                    if (!match.isSingleEvent) TeamLine(match.awayTeam, match.awayLogo)
                }
                HorizontalDivider(color = Floodlight.Outline)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LivePill(text = match.startsAt?.let { "LIVE · SINCE ${it.clockTime()}" } ?: "LIVE", modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.Wifi, contentDescription = null, tint = Floodlight.Text, modifier = Modifier.padding(end = 6.dp).height(14.dp))
                    Text(streamsLabel(match.streamCount), style = MaterialTheme.typography.labelMedium)
                }
            }
            SaveButton(
                saved = saved,
                label = match.title,
                onToggle = onToggleSave,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
            )
        }
    }
}

@Composable
private fun TeamLine(name: String, logo: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TeamCrest(name = name, logo = logo)
        Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun FixtureRow(
    match: Match,
    saved: Boolean,
    onOpen: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
    showSport: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onOpen).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.width(62.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (match.isLive) {
                Text("LIVE", style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.labelLarge.fontSize), color = Floodlight.LiveText)
            } else {
                Text(match.startsAt?.clockTime() ?: "TBC", style = MaterialTheme.typography.titleLarge)
                match.startsAt?.let { Text(it.untilLabel(), style = MaterialTheme.typography.bodySmall, color = Floodlight.TextMuted) }
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(match.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (showSport) Tag(match.sportLabel)
                Text(match.league, style = MaterialTheme.typography.bodySmall, color = Floodlight.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        SaveButton(saved = saved, label = match.title, onToggle = onToggleSave)
    }
}

fun streamsLabel(count: Int): String = when (count) {
    0 -> "Links soon"
    1 -> "1 stream"
    else -> "$count streams"
}
