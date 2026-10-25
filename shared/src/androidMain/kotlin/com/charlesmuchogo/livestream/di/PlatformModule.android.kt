package com.charlesmuchogo.livestream.di

import com.charlesmuchogo.livestream.data.local.database.AndroidDatabaseBuilderFactory
import com.charlesmuchogo.livestream.data.local.database.DatabaseBuilderFactory
import com.charlesmuchogo.livestream.data.remote.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseBuilderFactory> { AndroidDatabaseBuilderFactory(context = androidContext()) }
    single { ApiConfig(baseUrl = LOCAL_API_URL) }
    single(BaseHttpClient) { HttpClient(OkHttp) }
}
