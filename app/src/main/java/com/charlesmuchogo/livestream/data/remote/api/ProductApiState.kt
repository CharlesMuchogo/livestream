package com.charlesmuchogo.livestream.data.remote.api

import com.example.amazone.utils.enums.ApiStatus


data class ApiState<out T>(val status: ApiStatus, val data:T?, val message: String?) {
    companion object{
        fun <T> success(data: T?): ApiState<T> {
            return  ApiState(ApiStatus.SUCCESS, data,null)
        }

        fun <T>error(msg: String): ApiState<T> {
            return ApiState(ApiStatus.ERROR, null, msg)
        }
        fun <T>loading(): ApiState<T> {
            return ApiState(ApiStatus.LOADING, null, null)
        }
    }
}