package com.charlesmuchogo.livestream.di

import android.content.Context
import androidx.room.Room
import com.charlesmuchogo.livestream.data.local.database.AppDatabase
import com.charlesmuchogo.livestream.data.local.repository.EventRepository
import com.charlesmuchogo.livestream.data.remote.api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext app: Context): AppDatabase{
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "livestream.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideEventsRepository(appDatabase:AppDatabase): EventRepository{
        return  EventRepository(eventDao =  appDatabase.eventsDao())
    }
}