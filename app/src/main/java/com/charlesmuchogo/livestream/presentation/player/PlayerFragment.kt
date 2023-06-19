package com.charlesmuchogo.livestream.presentation.player

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import androidx.databinding.DataBindingUtil
import com.charlesmuchogo.livestream.R
import com.charlesmuchogo.livestream.databinding.FragmentPlayerBinding


class PlayerFragment : Fragment() {
    lateinit var binding: FragmentPlayerBinding
    lateinit var videoView: VideoView
    var videoUrl =
        "https://1l1l.to/ch75.php"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false)
        binding.handler = this
        videoView = binding.videoView
        val uri: Uri = Uri.parse(videoUrl)
        videoView.setVideoURI(uri)
        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(videoView)
        mediaController.setMediaPlayer(videoView)
        videoView.setMediaController(mediaController)
        videoView.start()

        return binding.root
    }

}