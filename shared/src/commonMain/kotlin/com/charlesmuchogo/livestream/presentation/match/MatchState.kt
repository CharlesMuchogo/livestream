package com.charlesmuchogo.livestream.presentation.match

import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.domain.models.StreamSource

enum class PlayerPhase { Idle, Loading, Playing, Failed }

enum class SourceSort { Recommended, FewestAds }

data class ChannelFilter(val name: String, val count: Int)

data class MatchState(
    val title: String,
    val match: Match? = null,
    val streams: List<StreamSource> = emptyList(),
    val currentStreamId: Long? = null,
    val phase: PlayerPhase = PlayerPhase.Idle,
    val brokenIds: Set<Long> = emptySet(),
    val channel: String? = null,
    val verifiedOnly: Boolean = false,
    val sort: SourceSort = SourceSort.Recommended,
    val showAll: Boolean = false,
    val isSaved: Boolean = false,
    val isFullscreen: Boolean = false,
    val notice: String? = null,
    val showFullPage: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    val current: StreamSource? get() = streams.firstOrNull { it.id == currentStreamId }

    val channels: List<ChannelFilter>
        get() = streams.groupingBy { it.channel.trimEnd('.', ' ') }.eachCount()
            .filterKeys { it.isNotBlank() }
            .map { (name, count) -> ChannelFilter(name, count) }
            .sortedByDescending { it.count }
            .take(5)

    val filteredSources: List<StreamSource>
        get() {
            val filtered = streams.filter { source ->
                (channel == null || source.channel.trimEnd('.', ' ') == channel) && (!verifiedOnly || source.verified)
            }
            val ranked = filtered.sortedBy { it.id in brokenIds }
            return if (sort == SourceSort.FewestAds) ranked.sortedWith(compareBy<StreamSource> { it.id in brokenIds }.thenBy { it.ads }) else ranked
        }

    val visibleSources: List<StreamSource>
        get() = if (showAll) filteredSources else filteredSources.take(COLLAPSED_SOURCE_COUNT)

    val nextSource: StreamSource?
        get() {
            val start = streams.indexOfFirst { it.id == currentStreamId }
            val ordered = if (start < 0) streams else streams.drop(start + 1) + streams.take(start + 1)
            return ordered.firstOrNull { it.id != currentStreamId && it.id !in brokenIds }
        }

    companion object {
        const val COLLAPSED_SOURCE_COUNT = 6
    }
}
