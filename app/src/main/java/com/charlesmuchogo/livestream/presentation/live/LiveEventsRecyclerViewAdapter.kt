package com.charlesmuchogo.livestream.presentation.live

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.charlesmuchogo.livestream.R
import com.charlesmuchogo.livestream.data.dataclasses.Channels
import com.charlesmuchogo.livestream.data.dataclasses.LiveEvent


class LiveEventsRecyclerViewAdapter(private val dataList: List<LiveEvent>) :
    RecyclerView.Adapter<LiveEventsRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        val eventDate: TextView = itemView.findViewById(R.id.eventDate)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
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
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}