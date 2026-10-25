package com.charlesmuchogo.livestream.di

import com.charlesmuchogo.livestream.data.local.database.DatabaseBuilderFactory
import com.charlesmuchogo.livestream.data.local.database.IosDatabaseBuilderFactory
import com.charlesmuchogo.livestream.data.remote.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseBuilderFactory> { IosDatabaseBuilderFactory() }
    single { ApiConfig(baseUrl = LOCAL_API_URL) }
    single(BaseHttpClient) { HttpClient(Darwin) }
}
