package com.charlesmuchogo.livestream.presentation.favourites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.charlesmuchogo.livestream.R
import com.charlesmuchogo.livestream.data.local.databaseclasses.Events
import com.charlesmuchogo.livestream.data.local.dataclasses.LiveEvent


class FavouritesRecyclerViewAdapter(private val dataList:  MutableList<Events>, private val favouritesViewModel: FavouritesViewModel) :
    RecyclerView.Adapter<FavouritesRecyclerViewAdapter.ViewHolder>() {
    fun updateData(newData: List<Events>) {
        dataList.clear()
        dataList.addAll(newData)
        notifyDataSetChanged()
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventName)
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
       holder.imageView.load(dataList[position].image)
        if(dataList[position].favourite){
            holder.vectorImage.setImageResource(R.drawable.baseline_heart_24)
        }else{
            holder.vectorImage.setImageResource(R.drawable.ic_outlined_heart_black_24dp)
        }
        holder.vectorImage.setOnClickListener {
            favouritesViewModel.favouriteEvent(dataList[position])
        }
        holder.itemView.setOnClickListener {
            val action = FavouritesFragmentDirections.actionFavouritesFragmentToPlayerFragment()
            val navController = it.findNavController()
            navController.navigate(action)
        }
    }



    override fun getItemCount(): Int {
        return dataList.size
    }
}