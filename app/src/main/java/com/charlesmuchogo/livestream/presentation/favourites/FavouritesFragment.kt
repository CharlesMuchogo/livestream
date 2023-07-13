    package com.charlesmuchogo.livestream.presentation.favourites

    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.databinding.DataBindingUtil
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.viewModels
    import androidx.recyclerview.widget.RecyclerView
    import com.charlesmuchogo.livestream.R
    import com.charlesmuchogo.livestream.databinding.FragmentFavouritesBinding
    import com.charlesmuchogo.livestream.presentation.player.PlayerFragmentRecyclerView
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
            adapter = FavouritesRecyclerViewAdapter(ArrayList(viewModel.eventsList.value ?: emptyList()), viewModel)
            Log.d("FavouritesViewModel", "got events favourites fragment: ${viewModel.eventsList}")

            recyclerView.adapter = adapter

            viewModel.eventsList.observe(viewLifecycleOwner) { events ->
                adapter.updateData(events)
            }
            return binding.root
        }

    }


