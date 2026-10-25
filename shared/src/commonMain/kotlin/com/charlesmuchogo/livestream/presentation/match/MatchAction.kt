package com.charlesmuchogo.livestream.presentation.match

import com.charlesmuchogo.livestream.domain.models.StreamSource

sealed interface MatchAction {
    data object OnBack : MatchAction
    data object OnToggleSave : MatchAction
    data class OnPlay(val source: StreamSource) : MatchAction
    data object OnPlayerReady : MatchAction
    data object OnPlayerFailed : MatchAction
    data object OnReportBroken : MatchAction
    data object OnDismissNotice : MatchAction
    data object OnToggleFullPage : MatchAction
    data object OnTryNext : MatchAction
    data class OnChannelSelected(val channel: String?) : MatchAction
    data object OnToggleVerifiedOnly : MatchAction
    data class OnSortSelected(val sort: SourceSort) : MatchAction
    data object OnShowAll : MatchAction
    data class OnFullscreenChange(val fullscreen: Boolean) : MatchAction
    data object OnRetry : MatchAction
}
