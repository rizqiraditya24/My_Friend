package com.example.myfriend

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.crocodic.core.base.adapter.CorePagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.crocodic.core.base.viewmodel.CoreViewModel
import com.example.myfriend.api_repository.DataProductsRepo
import com.example.myfriend.data.Friend
import com.example.myfriend.data.FriendDao
import com.example.myfriend.dataApi.DataProduct
import com.example.myfriend.repo.FriendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val friendDao: FriendDao,
    private val dataProductsRepo: DataProductsRepo,
    private val repository: FriendRepository
) : CoreViewModel() {

    private val _product = MutableStateFlow<List<DataProduct>>(emptyList())
    val product: StateFlow<List<DataProduct>> = _product

    val queries = MutableStateFlow<Triple<String?, String?, String?>>(Triple(null, null, null))
    fun getPagingProducts(): Flow<PagingData<DataProduct>> {
        return queries.flatMapLatest {
            Pager(
                config = CorePagingSource.config(10),
                pagingSourceFactory = {
                    CorePagingSource(0) { page: Int, limit: Int ->
                        dataProductsRepo.pagingProducts(limit, page).first()
                    }
                }
            ).flow
                .map { pagingData ->
                    pagingData.filterDistinct() // Filter data duplikat
                }
                .cachedIn(viewModelScope)
        }
    }

    fun <T : Any> PagingData<T>.filterDistinct(): PagingData<T> {
        val seenIds = mutableSetOf<Any>()
        return this.filter { item ->
            val id = when (item) {
                is DataProduct -> item.id // Pastikan 'id' adalah field unik
                else -> null
            }
            if (id != null && seenIds.contains(id).not()) {
                seenIds.add(id)
                true
            } else {
                false
            }
        }
    }

    fun getProduct(keyword: String = "") = viewModelScope.launch {
        dataProductsRepo.getProducts(keyword).collect { it: List<DataProduct> ->
            _product.emit(it)
        }
    }

    fun sortProducts(sortBy: String = "", orderBy: String = "") = viewModelScope.launch {
        dataProductsRepo.sortProducts(sortBy, orderBy).collect {
            _product.emit(it)
        }
    }

    fun filterProducts(filter: String = "") = viewModelScope.launch {
        dataProductsRepo.filterProducts(filter).collect {
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
