package com.charlesmuchogo.livestream.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.charlesmuchogo.livestream.R
import com.charlesmuchogo.livestream.data.local.dataclasses.Channels
import com.charlesmuchogo.livestream.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var gridView: GridView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.handler = this

        gridView = binding.gridView
        val channelList = listOf(
            Channels(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", title = "Supersports"),
            Channels(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", title = "BT Sport 8"),
            Channels(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", title = "BeIN Sport 8"),
            Channels(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", title = "ESPN USA"),
            Channels(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", title = "EuroSport 1 UK"),
            Channels(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", title = "EuroSport 1 UK"),
            Channels(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", title = "EuroSport 1 UK"),
            Channels(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", title = "EuroSport 1 UK"),
            Channels(image = "https://firebasestorage.googleapis.com/v0/b/flutter-notifications-a462c.appspot.com/o/images%2Fss_logo_slogan.png?alt=media&token=f21675b0-8296-4b50-8fa4-8ce18c95ecba", title = "EuroSport 1 UK"),
            )

        val adapter = GridAdapter(requireContext(), channelList)
        gridView.adapter = adapter



        return binding.root
    }


}