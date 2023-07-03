package com.charlesmuchogo.livestream.data.local.repository

import androidx.lifecycle.LiveData
import com.charlesmuchogo.livestream.data.local.dao.EventsDao
import com.charlesmuchogo.livestream.data.local.databaseclasses.Events

class EventRepository(private val eventDao: EventsDao) {
     suspend fun getAllEvents(): List<Events> {
        return eventDao.getAll()
    }

    suspend fun insertEvent(events: List<Events>) {
        eventDao.upsertAll(events)
    }
}