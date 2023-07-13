package com.charlesmuchogo.livestream.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.charlesmuchogo.livestream.R
import com.charlesmuchogo.livestream.databinding.FragmentPlayerBinding
import com.charlesmuchogo.livestream.presentation.favourites.FavouritesRecyclerViewAdapter
import com.charlesmuchogo.livestream.presentation.favourites.FavouritesViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerFragment : Fragment() {
    private lateinit var binding: FragmentPlayerBinding
    private lateinit var player: SimpleExoPlayer

    private val videoUrl = "https://fcc3ddae59ed.us-west-2.playback.live-video.net/api/video/v1/us-west-2.893648527354.channel.DmumNckWFTqz.m3u8"

    private lateinit var adapter: PlayerFragmentRecyclerView
    private lateinit var recyclerView: RecyclerView
    private val viewModel by viewModels<FavouritesViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false)
        binding.handler = this

        recyclerView = binding.recyclerView
        adapter = PlayerFragmentRecyclerView(ArrayList(viewModel.eventsList.value ?: emptyList()), viewModel)
        recyclerView.adapter = adapter
        viewModel.eventsList.observe(viewLifecycleOwner) { events ->
            adapter.updateData(events)
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializePlayer()
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(requireContext()).build()

        val dataSourceFactory = DefaultDataSourceFactory(
            requireContext(),
            Util.getUserAgent(requireContext(), "YourApplicationName")
        )

        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(videoUrl))

        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true

        binding.playerView.player = player
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
    }
}
