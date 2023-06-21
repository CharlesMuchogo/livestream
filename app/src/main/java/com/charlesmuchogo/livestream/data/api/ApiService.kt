package com.charlesmuchogo.livestream.data.api


import com.charlesmuchogo.livestream.data.dataclasses.Channels
import retrofit2.http.GET

interface ApiService {
    @GET("product")
    suspend fun getProducts(): Channels

}