package com.charlesmuchogo.livestream.data.local.databaseclasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "events")
data class Events(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "image")val image: String,
    @ColumnInfo(name = "eventName")val eventName: String,
    @ColumnInfo(name = "eventDate")val eventDate: String,
    @ColumnInfo(name = "favourite")val favourite: Boolean,
    )