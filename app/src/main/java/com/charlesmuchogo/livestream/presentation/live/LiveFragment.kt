package com.charlesmuchogo.livestream.presentation.live

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.charlesmuchogo.livestream.R
import com.charlesmuchogo.livestream.data.dataclasses.Channels
import com.charlesmuchogo.livestream.data.dataclasses.LiveEvent
import com.charlesmuchogo.livestream.databinding.FragmentHomeBinding
import com.charlesmuchogo.livestream.databinding.FragmentLiveBinding


class LiveFragment : Fragment() {

    private lateinit var binding: FragmentLiveBinding
    private lateinit var adapter: LiveEventsRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_live, container, false)
        binding.handler = this
        recyclerView = binding.recyclerView

        val eventsList = listOf(
            LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "Supersports"),
            LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "BT Sport 8"),
            LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "BeIN Sport 8"),
            LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "ESPN USA"),
            LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "EuroSport 1 UK"),
            LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "EuroSport 1 UK"),
            LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "EuroSport 1 UK"),
            LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "EuroSport 1 UK"),
            LiveEvent(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", eventDate = "Today 14:30", eventName = "EuroSport 1 UK"),
        )
        adapter = LiveEventsRecyclerViewAdapter(eventsList)
        recyclerView.adapter = adapter

        return binding.root
    }

}