package com.charlesmuchogo.livestream.presentation.favourites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.charlesmuchogo.livestream.R
import com.charlesmuchogo.livestream.data.local.database.AppDatabase
import com.charlesmuchogo.livestream.data.local.databaseclasses.Events
import com.charlesmuchogo.livestream.data.local.repository.EventRepository
import com.charlesmuchogo.livestream.databinding.FragmentFavouritesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavouritesFragment : Fragment() {


    private lateinit var binding: FragmentFavouritesBinding
    private lateinit var adapter: FavouritesRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    private val viewModel by viewModels<FavouritesViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favourites, container, false)
        binding.handler = this
        recyclerView = binding.recyclerView
        adapter = FavouritesRecyclerViewAdapter(viewModel.eventsList.value)
        recyclerView.adapter = adapter
        return binding.root
    }
}


