package com.charlesmuchogo.livestream.presentation.live

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charlesmuchogo.livestream.data.local.database.AppDatabase
import com.charlesmuchogo.livestream.data.local.databaseclasses.Events
import com.charlesmuchogo.livestream.data.local.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveViewModel @Inject constructor(
    appDatabase: AppDatabase,)  : ViewModel() {
    private  val repository = EventRepository(appDatabase.eventsDao())


    fun favouriteEvent( events: Events) {
        viewModelScope.launch {
            try {
                repository.insertEvent(events);
            } catch (e: Exception) {
                Log.e("FavouritesViewModel", "Failed to favourite this event ${e.message}")
            }
        }
    }

}