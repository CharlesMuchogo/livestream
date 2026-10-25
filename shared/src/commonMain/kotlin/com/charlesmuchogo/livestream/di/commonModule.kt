package com.charlesmuchogo.livestream.di

import com.charlesmuchogo.livestream.data.local.database.AppDatabase
import com.charlesmuchogo.livestream.data.local.database.DatabaseBuilderFactory
import com.charlesmuchogo.livestream.data.local.database.buildAppDatabase
import com.charlesmuchogo.livestream.data.remote.LivestreamApi
import com.charlesmuchogo.livestream.data.remote.createHttpClient
import com.charlesmuchogo.livestream.data.repository.LibraryRepository
import com.charlesmuchogo.livestream.data.repository.LibraryRepositoryImpl
import com.charlesmuchogo.livestream.data.repository.MatchRepository
import com.charlesmuchogo.livestream.data.repository.MatchRepositoryImpl
import com.charlesmuchogo.livestream.presentation.live.LiveViewModel
import com.charlesmuchogo.livestream.presentation.match.MatchViewModel
import com.charlesmuchogo.livestream.presentation.saved.SavedViewModel
import com.charlesmuchogo.livestream.presentation.schedule.ScheduleViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val BaseHttpClient = named("base-http-client")

fun commonModule(): Module = module {
    single { buildAppDatabase(get<DatabaseBuilderFactory>().create()) }
    single { get<AppDatabase>().savedMatchesDao() }
    single { get<AppDatabase>().followedLeaguesDao() }

    single { createHttpClient(get<HttpClient>(BaseHttpClient), get()) }
    single { LivestreamApi(get()) }

    single<MatchRepository> { MatchRepositoryImpl(api = get()) }
    single<LibraryRepository> { LibraryRepositoryImpl(savedMatchesDao = get(), followedLeaguesDao = get()) }

    viewModel { LiveViewModel(matchRepository = get(), libraryRepository = get()) }
    viewModelOf(::ScheduleViewModel)
    viewModelOf(::SavedViewModel)
    viewModel { (matchId: Long, title: String) ->
        MatchViewModel(matchId = matchId, title = title, matchRepository = get(), libraryRepository = get())
    }
}
