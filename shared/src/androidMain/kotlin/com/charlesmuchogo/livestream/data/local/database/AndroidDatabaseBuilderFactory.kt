package com.charlesmuchogo.livestream.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

class AndroidDatabaseBuilderFactory(private val context: Context) : DatabaseBuilderFactory {
    override fun create(): RoomDatabase.Builder<AppDatabase> {
        val appContext = context.applicationContext
        return Room.databaseBuilder<AppDatabase>(
            context = appContext,
            name = appContext.getDatabasePath(DATABASE_NAME).absolutePath,
        )
    }
}
