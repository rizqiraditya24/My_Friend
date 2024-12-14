package com.example.myfriend.response_api

import com.crocodic.core.api.ModelResponse
import com.example.myfriend.dataApi.DataProduct
import com.google.gson.annotations.SerializedName

data class ResponseDataProduct(
    @SerializedName("products")
    val product: List<DataProduct>
): ModelResponse()
