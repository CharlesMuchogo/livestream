package com.charlesmuchogo.livestream.domain.models

enum class StreamKind { Direct, Page }

data class StreamSource(
    val id: Long,
    val label: String,
    val streamer: String,
    val channel: String,
    val quality: String,
    val language: String,
    val ads: Int,
    val verified: Boolean,
    val kind: StreamKind,
    val url: String,
) {
    val isHd: Boolean get() = quality.equals("HD", ignoreCase = true)
}

data class MatchDetail(
    val match: Match,
    val streams: List<StreamSource>,
)
