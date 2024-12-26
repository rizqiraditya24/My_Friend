package com.example.myfriend.api_repository

import com.crocodic.core.api.ApiObserver
import com.example.myfriend.api.ApiService
import com.example.myfriend.dataApi.DataProduct
import com.example.myfriend.response_api.ResponseDataProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ImplDataProductRepo @Inject constructor(private val apiService: ApiService): DataProductsRepo{

    override fun getProducts(keyword: String): Flow<List<DataProduct>> = flow {
        ApiObserver.run(
            {apiService.getProduct(keyword)},
            false,
            object : ApiObserver.ModelResponseListener<ResponseDataProduct> {
                override suspend fun onSuccess(response: ResponseDataProduct) {
                    emit(response.product)
                }

                override suspend fun onError(response: ResponseDataProduct) {
                    emit(emptyList())
                }
            }
        )
    }

    override fun sortProducts(sortBy: String, order: String): Flow<List<DataProduct>> = flow {
        ApiObserver.run(
            { apiService.sortProducts(sortBy, order) },
            false,
            object : ApiObserver.ModelResponseListener<ResponseDataProduct> {
                override suspend fun onSuccess(response: ResponseDataProduct) {
                    emit(response.product)
                }

                override suspend fun onError(response: ResponseDataProduct) {
                    emit(emptyList())
                }
            })
    }

    override fun filterProducts(filter: String): Flow<List<DataProduct>> = flow {
        ApiObserver.run(
            { apiService.filterProducts(filter) },
            false,
            object : ApiObserver.ModelResponseListener<ResponseDataProduct> {
                override suspend fun onSuccess(response: ResponseDataProduct) {
                    emit(response.product)
                }

                override suspend fun onError(response: ResponseDataProduct) {
                    emit(emptyList())
                }
            })
    }

    override fun pagingProducts(limit: Int, skip: Int): Flow<List<DataProduct>> {
       return flow {
           val response = apiService.pagingProducts(limit, skip)
           emit(response.product ?:return@flow)
       }
    }

}