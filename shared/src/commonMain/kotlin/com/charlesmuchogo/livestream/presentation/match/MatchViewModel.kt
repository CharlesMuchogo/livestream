package com.charlesmuchogo.livestream.presentation.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charlesmuchogo.livestream.data.repository.LibraryRepository
import com.charlesmuchogo.livestream.data.repository.MatchRepository
import com.charlesmuchogo.livestream.domain.models.StreamSource
import com.charlesmuchogo.livestream.presentation.components.SERVER_UNREACHABLE
import com.charlesmuchogo.livestream.presentation.components.hostOf
import com.charlesmuchogo.livestream.presentation.components.siteKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MatchViewModel(
    private val matchId: Long,
    title: String,
    private val matchRepository: MatchRepository,
    private val libraryRepository: LibraryRepository,
    private val pollInterval: Duration = 60.seconds,
) : ViewModel() {
    private val screen = MutableStateFlow(MatchState(title = title))

    val state: StateFlow<MatchState> = combine(screen, libraryRepository.savedIds()) { current, saved ->
        current.copy(isSaved = matchId in saved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MatchState(title = title))

    init {
        viewModelScope.launch {
            load(showSpinner = true)
            while (true) {
                delay(pollInterval)
                load(showSpinner = false)
            }
        }
    }

    fun onAction(action: MatchAction) {
        when (action) {
            is MatchAction.OnPlay -> play(action.source)
            MatchAction.OnPlayerReady -> screen.update { if (it.phase == PlayerPhase.Loading) it.copy(phase = PlayerPhase.Playing) else it }
            MatchAction.OnPlayerFailed -> markCurrentBroken(sameSiteToo = true, autoAdvance = true)
            MatchAction.OnReportBroken -> markCurrentBroken(sameSiteToo = false, autoAdvance = true)
            MatchAction.OnDismissNotice -> screen.update { it.copy(notice = null) }
            MatchAction.OnToggleFullPage -> screen.update { it.copy(showFullPage = !it.showFullPage) }
            MatchAction.OnTryNext -> screen.value.nextSource?.let(::play)
            is MatchAction.OnChannelSelected -> screen.update { it.copy(channel = action.channel, showAll = false) }
            MatchAction.OnToggleVerifiedOnly -> screen.update { it.copy(verifiedOnly = !it.verifiedOnly, showAll = false) }
            is MatchAction.OnSortSelected -> screen.update { it.copy(sort = action.sort) }
            MatchAction.OnShowAll -> screen.update { it.copy(showAll = true) }
            is MatchAction.OnFullscreenChange -> screen.update { it.copy(isFullscreen = action.fullscreen) }
            MatchAction.OnToggleSave -> toggleSave()
            MatchAction.OnRetry -> viewModelScope.launch { load(showSpinner = true) }
            MatchAction.OnBack -> Unit
        }
    }

    private fun play(source: StreamSource) {
        screen.update { it.copy(currentStreamId = source.id, phase = PlayerPhase.Loading, showFullPage = false) }
    }

    private fun markCurrentBroken(sameSiteToo: Boolean, autoAdvance: Boolean) {
        val current = screen.value
        val failing = current.current ?: return
        val failingSite = siteKey(hostOf(failing.url))
        val broken = current.brokenIds + failing.id + if (sameSiteToo) {
            current.streams.filter { siteKey(hostOf(it.url)) == failingSite }.map { it.id }
        } else {
            emptyList()
        }
        val next = current.copy(brokenIds = broken).nextSource
        if (autoAdvance && next != null) {
            screen.update {
                it.copy(
                    brokenIds = broken,
                    currentStreamId = next.id,
                    phase = PlayerPhase.Loading,
                    showFullPage = false,
                    notice = "${failing.label} didn't play here. Switched to ${next.label}.",
                )
            }
        } else {
            screen.update { it.copy(brokenIds = broken, phase = PlayerPhase.Failed) }
        }
    }

    private suspend fun load(showSpinner: Boolean) {
        if (showSpinner) screen.update { it.copy(isLoading = true, error = null) }
        matchRepository.match(matchId).fold(
            onSuccess = { detail ->
                screen.update { current ->
                    val stillAvailable = current.currentStreamId?.takeIf { id -> detail.streams.any { it.id == id } }
                    val autoPick = stillAvailable ?: detail.streams.firstOrNull()?.id
                    current.copy(
                        title = detail.match.title,
                        match = detail.match,
                        streams = detail.streams,
                        currentStreamId = autoPick,
                        phase = when {
                            autoPick == null -> PlayerPhase.Idle
                            stillAvailable != null -> current.phase
                            else -> PlayerPhase.Loading
                        },
                        isLoading = false,
                        error = null,
                    )
                }
                libraryRepository.refreshSaved(detail.match)
            },
            onFailure = {
                screen.update { current -> current.copy(isLoading = false, error = if (current.match == null) SERVER_UNREACHABLE else current.error) }
            },
        )
    }

    private fun toggleSave() {
        val match = screen.value.match ?: return
        viewModelScope.launch {
            if (state.value.isSaved) libraryRepository.unsave(match.id) else libraryRepository.save(match)
        }
    }
}
