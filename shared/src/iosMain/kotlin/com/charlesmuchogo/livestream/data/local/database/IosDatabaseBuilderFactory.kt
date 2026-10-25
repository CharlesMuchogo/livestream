package com.charlesmuchogo.livestream.data.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

class IosDatabaseBuilderFactory : DatabaseBuilderFactory {
    override fun create(): RoomDatabase.Builder<AppDatabase> =
        Room.databaseBuilder<AppDatabase>(name = "${documentDirectory()}/$DATABASE_NAME")

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(directory?.path)
    }
}
