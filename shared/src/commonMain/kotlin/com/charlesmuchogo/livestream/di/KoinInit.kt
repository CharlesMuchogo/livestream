package com.charlesmuchogo.livestream.di

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

class KoinInit {
    fun init(appDeclaration: KoinAppDeclaration = {}): Koin =
        startKoin {
            appDeclaration()
            modules(commonModule(), platformModule())
        }.koin
}
