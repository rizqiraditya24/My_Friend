package com.example.myfriend.api_repository

import com.example.myfriend.data.Friend
import com.example.myfriend.dataApi.DataProduct
import kotlinx.coroutines.flow.Flow

interface DataProductsRepo {

    fun getProducts(keyword: String): Flow<List<DataProduct>>

    fun sortProducts(sortBy: String, order: String): Flow<List<DataProduct>>

    fun filterProducts(filter: String): Flow<List<DataProduct>>

    fun pagingProducts(limit: Int, skip: Int): Flow<List<DataProduct>>

    fun getSlider() : Flow<List<DataProduct>>
}