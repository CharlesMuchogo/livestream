package com.charlesmuchogo.livestream.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import coil.load
import com.charlesmuchogo.livestream.R

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

        return view
    }
}