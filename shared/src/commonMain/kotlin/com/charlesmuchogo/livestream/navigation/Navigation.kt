package com.charlesmuchogo.livestream.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.presentation.live.LiveRoot
import com.charlesmuchogo.livestream.presentation.match.MatchRoot
import com.charlesmuchogo.livestream.presentation.saved.SavedRoot
import com.charlesmuchogo.livestream.presentation.schedule.ScheduleRoot
import com.charlesmuchogo.livestream.ui.theme.Floodlight
import kotlin.reflect.KClass

private data class Tab(val route: Any, val routeClass: KClass<*>, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(LiveRoute, LiveRoute::class, "Live", Icons.Filled.LiveTv),
    Tab(ScheduleRoute(), ScheduleRoute::class, "Schedule", Icons.Filled.CalendarMonth),
    Tab(SavedRoute, SavedRoute::class, "Saved", Icons.Filled.Favorite),
)

@Composable
fun Navigation(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination: NavDestination? = backStackEntry?.destination
    val showBottomBar = destination?.hasRoute<MatchRoute>() != true

    fun openMatch(match: Match) = navController.navigate(MatchRoute(id = match.id, title = match.title)) { launchSingleTop = true }

    fun openTab(route: Any) = navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    Scaffold(
        containerColor = Floodlight.Ink,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Floodlight.Surface) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = destination?.hasRoute(tab.routeClass) == true,
                            onClick = { openTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Floodlight.Ink,
                                selectedTextColor = Floodlight.Volt,
                                indicatorColor = Floodlight.Volt,
                                unselectedIconColor = Floodlight.TextMuted,
                                unselectedTextColor = Floodlight.TextMuted,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        val bottom = PaddingValues(bottom = padding.calculateBottomPadding())
        NavHost(
            navController = navController,
            startDestination = LiveRoute,
            modifier = Modifier.padding(bottom).consumeWindowInsets(bottom),
        ) {
            composable<LiveRoute> {
                LiveRoot(onOpenMatch = ::openMatch, onSearch = { openTab(ScheduleRoute(focusSearch = true)) })
            }
            composable<ScheduleRoute> { entry ->
                ScheduleRoot(focusSearch = entry.toRoute<ScheduleRoute>().focusSearch, onOpenMatch = ::openMatch)
            }
            composable<SavedRoute> {
                SavedRoot(onOpenMatch = ::openMatch, onBrowseLive = { openTab(LiveRoute) })
            }
            composable<MatchRoute> { entry ->
                val route = entry.toRoute<MatchRoute>()
                MatchRoot(matchId = route.id, title = route.title, onBack = { navController.popBackStack() })
            }
        }
    }
}
