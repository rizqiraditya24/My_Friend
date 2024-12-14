package com.example.myfriend.api_repository

import com.example.myfriend.data.Friend
import com.example.myfriend.dataApi.DataProduct
import kotlinx.coroutines.flow.Flow

interface DataProductsRepo {

    fun getProducts(keyword: String): Flow<List<DataProduct>>

    fun searchProducts(keyword: String): Flow<List<DataProduct>>
}