package com.charlesmuchogo.livestream.presentation.schedule

import com.charlesmuchogo.livestream.domain.models.Match

sealed interface ScheduleAction {
    data class OnDaySelected(val date: String) : ScheduleAction
    data class OnQueryChange(val query: String) : ScheduleAction
    data class OnToggleGroup(val key: String) : ScheduleAction
    data class OnMatchClick(val match: Match) : ScheduleAction
    data class OnToggleSave(val match: Match) : ScheduleAction
    data object OnRetry : ScheduleAction
}
