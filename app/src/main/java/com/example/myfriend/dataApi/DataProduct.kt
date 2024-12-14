package com.example.myfriend.dataApi

import com.google.gson.annotations.SerializedName

data class DataProduct(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String
)
