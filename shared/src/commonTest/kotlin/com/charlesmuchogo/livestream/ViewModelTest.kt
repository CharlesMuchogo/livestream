package com.charlesmuchogo.livestream

import com.charlesmuchogo.livestream.domain.models.HomeFeed
import com.charlesmuchogo.livestream.domain.models.MatchDetail
import com.charlesmuchogo.livestream.presentation.live.LiveAction
import com.charlesmuchogo.livestream.presentation.live.LiveViewModel
import com.charlesmuchogo.livestream.presentation.match.MatchAction
import com.charlesmuchogo.livestream.presentation.match.MatchViewModel
import com.charlesmuchogo.livestream.presentation.match.PlayerPhase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val viewModels = mutableListOf<ViewModel>()

    private fun <T : ViewModel> track(viewModel: T): T = viewModel.also { viewModels += it }

    private fun viewModelTest(block: suspend TestScope.() -> Unit): TestResult = runTest(dispatcher) {
        try {
            block()
        } finally {
            viewModels.forEach { it.viewModelScope.cancel() }
        }
    }

    private fun TestScope.collect(flow: kotlinx.coroutines.flow.Flow<*>) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect {} }
    }

    @Test
    fun liveShowsFollowedLeaguesFirstAndTogglesSave() = viewModelTest {
        val repo = FakeMatchRepository(Result.success(HomeFeed(emptyList(), listOf(match(1, leagueId = 1, streams = 20), match(2, leagueId = 2, streams = 5)), emptyList(), null)))
        val library = FakeLibraryRepository().apply { followed.value = setOf(2L) }
        val viewModel = track(LiveViewModel(repo, library, refreshInterval = 24.hours))
        collect(viewModel.state)
        collect(viewModel.events)
        runCurrent()

        assertEquals(listOf(2L, 1L), viewModel.state.value.live.map { it.id })
        assertEquals(false, viewModel.state.value.isLoading)

        viewModel.onAction(LiveAction.OnToggleSave(match(1)))
        runCurrent()
        assertEquals(setOf(1L), viewModel.state.value.savedIds)
        viewModel.onAction(LiveAction.OnToggleSave(match(1)))
        runCurrent()
        assertEquals(emptySet(), viewModel.state.value.savedIds)
    }

    @Test
    fun liveShowsErrorOnlyWhenNothingToShow() = viewModelTest {
        val repo = FakeMatchRepository(Result.failure(IllegalStateException("offline")))
        val viewModel = track(LiveViewModel(repo, FakeLibraryRepository(), refreshInterval = 24.hours))
        collect(viewModel.state)
        runCurrent()
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun matchAutoPicksBestSourceAndMovesOnWhenItFails() = viewModelTest {
        val detail = MatchDetail(match(9), listOf(source(1, verified = true), source(2), source(3)))
        val viewModel = track(MatchViewModel(9, "Home 9 v Away 9", FakeMatchRepository(detailResult = Result.success(detail)), FakeLibraryRepository(), pollInterval = 24.hours))
        collect(viewModel.state)
        runCurrent()

        assertEquals(1L, viewModel.state.value.currentStreamId)
        assertEquals(PlayerPhase.Loading, viewModel.state.value.phase)

        viewModel.onAction(MatchAction.OnPlayerReady)
        runCurrent()
        assertEquals(PlayerPhase.Playing, viewModel.state.value.phase)

        viewModel.onAction(MatchAction.OnPlayerFailed)
        runCurrent()
        assertTrue(1L in viewModel.state.value.brokenIds)
        assertEquals(2L, viewModel.state.value.currentStreamId)
        assertEquals(PlayerPhase.Loading, viewModel.state.value.phase)
        assertNotNull(viewModel.state.value.notice)
    }

    @Test
    fun failingSourceSkipsItsWholeSite() = viewModelTest {
        val sameSite = listOf(
            source(1).copy(url = "https://live.bounce.today/a"),
            source(2).copy(url = "https://live4.bounce.today/a"),
            source(3).copy(url = "https://good.st/a"),
        )
        val viewModel = track(MatchViewModel(9, "t", FakeMatchRepository(detailResult = Result.success(MatchDetail(match(9), sameSite))), FakeLibraryRepository(), pollInterval = 24.hours))
        collect(viewModel.state)
        runCurrent()

        viewModel.onAction(MatchAction.OnPlayerFailed)
        runCurrent()
        assertEquals(setOf(1L, 2L), viewModel.state.value.brokenIds)
        assertEquals(3L, viewModel.state.value.currentStreamId)

        viewModel.onAction(MatchAction.OnPlayerFailed)
        runCurrent()
        assertEquals(PlayerPhase.Failed, viewModel.state.value.phase)
    }

    @Test
    fun matchWithoutStreamsWaitsIdle() = viewModelTest {
        val viewModel = track(MatchViewModel(9, "t", FakeMatchRepository(detailResult = Result.success(MatchDetail(match(9, live = false), emptyList()))), FakeLibraryRepository(), pollInterval = 24.hours))
        collect(viewModel.state)
        runCurrent()
        assertEquals(PlayerPhase.Idle, viewModel.state.value.phase)
        assertEquals(null, viewModel.state.value.current)
    }
}
