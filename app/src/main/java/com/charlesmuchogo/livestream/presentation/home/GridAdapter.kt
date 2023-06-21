package com.charlesmuchogo.livestream.presentation.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import coil.load
import com.charlesmuchogo.livestream.R
import com.charlesmuchogo.livestream.data.dataclasses.Channels

class GridAdapter(private val context: Context, private val imageList: List<Channels>) : BaseAdapter() {

    override fun getCount(): Int {
        return imageList.size
    }

    override fun getItem(position: Int): Any {
        return imageList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (convertView == null) {
            view = inflater.inflate(R.layout.grid_item, null)
        } else {
            view = convertView
        }

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val channelName = view.findViewById<TextView>(R.id.channelName)
        imageView.load(imageList[position].image)
        channelName.text = imageList[position].title

        view.setOnClickListener {
            findNavController(HomeFragment()).navigate(R.id.action_homeFragment_to_playerFragment)
        }


        return view
    }
}