package com.charlesmuchogo.livestream.presentation.live

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.charlesmuchogo.livestream.R
import com.charlesmuchogo.livestream.data.local.databaseclasses.Events
import com.charlesmuchogo.livestream.data.local.dataclasses.Channels
import com.charlesmuchogo.livestream.data.local.dataclasses.LiveEvent
import com.charlesmuchogo.livestream.presentation.favourites.FavouritesFragmentDirections


class LiveEventsRecyclerViewAdapter(private val dataList: List<LiveEvent>,private val viewModel: LiveViewModel ) :
    RecyclerView.Adapter<LiveEventsRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        val eventDate: TextView = itemView.findViewById(R.id.eventDate)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val vectorImage: ImageView = itemView.findViewById(R.id.vectorImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.live_event_card_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.eventName.text = dataList[position].eventName
       holder.eventDate.text = dataList[position].eventDate
       holder.imageView.load(dataList[position].image)

       holder.vectorImage.setOnClickListener {
           viewModel.favouriteEvent(Events(0,dataList[position].image, dataList[position].eventName, dataList[position].eventDate, dataList[position].link, true))
           Toast.makeText(holder.itemView.context, "Event added to favourites ", LENGTH_SHORT).show()
       }



        holder.itemView.setOnClickListener {
            val action = LiveFragmentDirections.actionLiveFragmentToPlayerFragment()
            val navController = it.findNavController()
            navController.navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}