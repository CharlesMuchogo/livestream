package com.charlesmuchogo.livestream.data.remote.api


import com.charlesmuchogo.livestream.data.local.dataclasses.Channels
import retrofit2.http.GET

interface ApiService {
    @GET("product")
    suspend fun getProducts(): Channels

}