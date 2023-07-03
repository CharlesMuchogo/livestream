package com.charlesmuchogo.livestream.presentation.favourites

import android.util.Log
import androidx.lifecycle.LiveData
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


    val eventsList = MutableStateFlow(
       listOf(LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "Supersports"),
           LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "BT Sport 8"),
           LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "BeIN Sport 8"),
           LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "ESPN USA"),
           LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "EuroSport 1 UK"),
           LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "EuroSport 1 UK"),
           LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "EuroSport 1 UK"),
           LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "EuroSport 1 UK"),
           LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "EuroSport 1 UK"),
       )
    )

    val eventList: List<Events> =
       listOf(
           Events(id = 0, image ="https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventName ="Supersports", eventDate = "Today 14:30", favourite = false  )

    )

    init {
        getEvents()
        insertData()
    }



    private fun getEvents(){
        viewModelScope.launch {
            repository.getAllEvents().runCatching {
            }
        }
    }

    private fun insertData(){
        viewModelScope.launch {
            repository.getAllEvents().runCatching {
                repository.insertEvent(eventList)
            }
        }
    }




}