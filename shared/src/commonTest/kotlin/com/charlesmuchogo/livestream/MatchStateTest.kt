package com.charlesmuchogo.livestream

import com.charlesmuchogo.livestream.presentation.components.hostOf
import com.charlesmuchogo.livestream.presentation.components.sameSite
import com.charlesmuchogo.livestream.presentation.components.siteKey
import com.charlesmuchogo.livestream.presentation.match.MatchState
import com.charlesmuchogo.livestream.presentation.match.SourceSort
import com.charlesmuchogo.livestream.presentation.schedule.ScheduleViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MatchStateTest {
    private val streams = listOf(
        source(1, verified = true, ads = 2),
        source(2, verified = true, ads = 1),
        source(3, ads = 1, channel = "Sky Sports..."),
        source(4, ads = 3, channel = "Sky Sports"),
    )

    @Test
    fun brokenSourcesSinkAndNextSkipsThem() {
        val state = MatchState(title = "t", streams = streams, currentStreamId = 1, brokenIds = setOf(1, 2))
        assertEquals(listOf(3L, 4L, 1L, 2L), state.filteredSources.map { it.id })
        assertEquals(3L, state.nextSource?.id)
    }

    @Test
    fun filtersByChannelVerifiedAndSortsByAds() {
        val state = MatchState(title = "t", streams = streams)
        assertEquals(listOf("Sky Sports" to 2, "Willow" to 2), state.channels.map { it.name to it.count }.sortedBy { it.first })
        assertEquals(listOf(3L, 4L), state.copy(channel = "Sky Sports").filteredSources.map { it.id })
        assertEquals(listOf(1L, 2L), state.copy(verifiedOnly = true).filteredSources.map { it.id })
        assertEquals(listOf(2L, 3L, 1L, 4L), state.copy(sort = SourceSort.FewestAds).filteredSources.map { it.id })
    }

    @Test
    fun nextStepsForwardAndWraps() {
        val list = listOf(source(1), source(2), source(3))
        assertEquals(3L, MatchState(title = "t", streams = list, currentStreamId = 2).nextSource?.id)
        assertEquals(1L, MatchState(title = "t", streams = list, currentStreamId = 3).nextSource?.id)
        assertEquals(1L, MatchState(title = "t", streams = list, currentStreamId = 3, brokenIds = setOf(2)).nextSource?.id)
    }

    @Test
    fun lastSourceHasNoNext() {
        val state = MatchState(title = "t", streams = listOf(source(1)), currentStreamId = 1)
        assertEquals(null, state.nextSource)
    }

    @Test
    fun sameSiteAllowsSubdomainsOnly() {
        assertTrue(sameSite("www.ripplestream.cyou", "ripplestream.cyou"))
        assertTrue(sameSite("embed.ripplestream.cyou", "ripplestream.cyou"))
        assertTrue(sameSite("ripplestream.cyou", "live.ripplestream.cyou"))
        assertFalse(sameSite("ads.example.com", "ripplestream.cyou"))
        assertFalse(sameSite(null, "ripplestream.cyou"))
    }

    @Test
    fun siteKeyGroupsSubdomains() {
        assertEquals("totalsportek.today", siteKey(hostOf("https://live4.totalsportek.today/Inter-vs-Fiorentina/77475")))
        assertEquals("totalsportek.today", siteKey(hostOf("https://www.totalsportek.today:443/x?y=1")))
        assertEquals("fxtrend.st", siteKey(hostOf("https://fxtrend.st/event/a")))
    }

    @Test
    fun scheduleGroupsLiveLeaguesFirst() {
        val groups = ScheduleViewModel.groupByLeague(listOf(match(1, leagueId = 7, live = false), match(2, leagueId = 8, live = true), match(3, leagueId = 7, live = false)))
        assertEquals(listOf("League 8", "League 7"), groups.map { it.league })
        assertEquals(2, groups[1].matches.size)
    }
}
