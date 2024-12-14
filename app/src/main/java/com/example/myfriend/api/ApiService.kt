package com.example.myfriend.api

import com.crocodic.core.api.ModelResponse
import com.example.myfriend.response_api.ResponseDataProduct
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("products/search")
    suspend fun getProduct(
        @Query("q") keyword: String
    ): ResponseDataProduct
}