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

    override fun searchProducts(keyword: String): Flow<List<DataProduct>> = flow {
        ApiObserver.run(
            { apiService.getProduct(keyword) }, // Panggil endpoint pencarian di ApiService
            false,
            object : ApiObserver.ModelResponseListener<ResponseDataProduct> {
                override suspend fun onSuccess(response: ResponseDataProduct) {
                    emit(response.product) // Emit hasil pencarian
                }

                override suspend fun onError(response: ResponseDataProduct) {
                    emit(emptyList()) // Emit list kosong jika ada error
                }
            }
        )
    }

}