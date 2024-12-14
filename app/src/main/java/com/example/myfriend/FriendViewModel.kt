package com.example.myfriend

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.crocodic.core.base.viewmodel.CoreViewModel
import com.example.myfriend.api_repository.DataProductsRepo
import com.example.myfriend.data.Friend
import com.example.myfriend.data.FriendDao
import com.example.myfriend.dataApi.DataProduct
import com.example.myfriend.repo.FriendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val friendDao: FriendDao,
    private val dataProductsRepo: DataProductsRepo,
    private val repository: FriendRepository
) : CoreViewModel() {

    private val _product = MutableStateFlow<List<DataProduct>>(emptyList())
    val product: StateFlow<List<DataProduct>> = _product


    fun getProduct(keyword: String = "") = viewModelScope.launch {
        dataProductsRepo.getProducts(keyword).collect { it: List<DataProduct> ->
            _product.emit(it)
        }
    }


    fun getFriend() = friendDao.getAll()

    fun getFriendById(id: Int) = friendDao.getItemById(id)

    suspend fun insertFriend(data: Friend) {
        friendDao.insert(data)
    }

    suspend fun editFriend(data: Friend) {
        friendDao.update(data)
    }

    suspend fun deleteFriend(data: Friend) {
        friendDao.delete(data)
    }

    suspend fun searchFriend(keyword: String): Flow<List<Friend>> {
        return repository.searchFriend(keyword)
    }

    fun searchProducts(keyword: String): Flow<List<DataProduct>> = flow {
        val filteredProducts = _product.value.filter { product ->
            product.title.contains(keyword, ignoreCase = true) ||
                    product.description.contains(keyword, ignoreCase = true)
        }
        emit(filteredProducts)
    }


    override fun apiLogout() {
        TODO("Not yet implemented")
    }

    override fun apiRenewToken() {
        TODO("Not yet implemented")
    }

}
