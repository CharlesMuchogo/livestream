package com.charlesmuchogo.livestream.presentation.favourites

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charlesmuchogo.livestream.data.local.database.AppDatabase
import com.charlesmuchogo.livestream.data.local.databaseclasses.Events
import com.charlesmuchogo.livestream.data.local.dataclasses.LiveEvent
import com.charlesmuchogo.livestream.data.local.repository.EventRepository
import com.example.amazone.utils.enums.ApiStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class FavouritesViewModel @Inject constructor(
    appDatabase: AppDatabase,)  : ViewModel() {
    private  val repository = EventRepository(appDatabase.eventsDao())


    val eventsList: MutableLiveData<List<Events>> = MutableLiveData()



    init {
        getFavourites()
        //getEvents()
    }



    private fun getEvents() {
        viewModelScope.launch {
            try {
                repository.getAllEvents().collect { events ->
                    eventsList.value = events
                }
            } catch (e: Exception) {
                Log.e("FavouritesViewModel", "Failed to get events: ${e.message}")
            }
        }
    }

    private fun getFavourites() {
        viewModelScope.launch {
            try {
                repository.getFavourites().collect { events ->
                    eventsList.value = events
                }
            } catch (e: Exception) {
                Log.e("FavouritesViewModel", "Failed to get events: ${e.message}")
            }
        }
    }


    fun favouriteEvent(event: Events) {
        viewModelScope.launch {
            try {
                repository.favouriteEvent(event.id, !event.favourite)
            } catch (e: Exception) {
                Log.e("FavouritesViewModel", "Failed to favourite this event ${e.message}")
            }
        }
    }






//    private fun insertData(){
//        viewModelScope.launch {
//            repository.getAllEvents().runCatching {
//                repository.insertEvent(eventList)
//            }
//        }
//    }

}