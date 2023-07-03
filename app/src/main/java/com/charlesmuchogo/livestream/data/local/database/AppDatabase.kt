package com.charlesmuchogo.livestream.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.charlesmuchogo.livestream.data.local.dao.ChannelsDao
import com.charlesmuchogo.livestream.data.local.dao.EventsDao
import com.charlesmuchogo.livestream.data.local.databaseclasses.Channels
import com.charlesmuchogo.livestream.data.local.databaseclasses.Events

@Database(entities = [Events::class, Channels::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventsDao(): EventsDao
    abstract fun channelsDao(): ChannelsDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "livestream"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }
}