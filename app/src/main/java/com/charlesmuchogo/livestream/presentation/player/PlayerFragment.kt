package com.charlesmuchogo.livestream.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.charlesmuchogo.livestream.R
import com.charlesmuchogo.livestream.databinding.FragmentPlayerBinding
import com.google.android.exoplayer2.DefaultLivePlaybackSpeedControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource


class PlayerFragment : Fragment() {
    lateinit var binding: FragmentPlayerBinding

    var videoUrl =  "https://vikistream.com/embed2.php?player=desktop&live=bein1fr"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false)
        binding.handler = this

        initializePlayer()
        return binding.root
    }

    private fun initializePlayer() {
        val player =
            ExoPlayer.Builder(requireContext())
                .setLivePlaybackSpeedControl(
                    DefaultLivePlaybackSpeedControl.Builder().setFallbackMaxPlaybackSpeed(1.04f).build()
                )
                .build()

        // create a media item.
        val mediaItem =
            MediaItem.Builder()
                .setUri("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4")
                .setLiveConfiguration(
                    MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.02f).build()
                )
                .build()

        player.setMediaItem(mediaItem)


        val mediaSource = ProgressiveMediaSource.Factory(
            DefaultDataSource.Factory(requireContext()) // <- context
        )
            .createMediaSource(mediaItem)

        // Finally assign this media source to the player
        player.apply {
            setMediaSource(mediaSource)
            playWhenReady = true // start playing when the exoplayer has setup
            seekTo(0, 0L) // Start from the beginning
            prepare() // Change the state from idle.
        }.also {
            // Do not forget to attach the player to the view
            binding.playerView.player = it
        }
    }
}