package com.charlesmuchogo.livestream.navigation

import kotlinx.serialization.Serializable

@Serializable
data object LiveRoute

@Serializable
data class ScheduleRoute(val focusSearch: Boolean = false)

@Serializable
data object SavedRoute

@Serializable
data class MatchRoute(val id: Long, val title: String)
