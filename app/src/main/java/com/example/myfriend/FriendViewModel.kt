package com.example.myfriend

import androidx.lifecycle.viewModelScope
import com.example.myfriend.data.Friend
import com.example.myfriend.data.FriendDao
import com.example.myfriend.repo.FriendRepository
import com.crocodic.core.base.viewmodel.CoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel@Inject constructor(
    private val friendDao: FriendDao,
    private val friendRepository: FriendRepository
) : CoreViewModel() {

    private val _friends = MutableSharedFlow<List<Friend>>()
    val friends = _friends.asSharedFlow()

    fun getFriends(keyword: String) = viewModelScope.launch {
        friendRepository.searchFriend(keyword).collect{
            _friends.emit(it)
        }
    }

    // Mengambil semua data teman dari database melalui FriendDao
    fun getFriend() = viewModelScope.launch {
        friendDao.getAll().collect { friendList ->
            _friends.emit(friendList)
        }
    }

    // Mengambil data teman berdasarkan ID dari database melalui FriendDao
    fun getFriendById(id: Int) = friendDao.getItemById(id)

    // Menyisipkan data teman baru ke dalam database
    // Fungsi ini bersifat suspend dan harus dipanggil dari dalam coroutine
    suspend fun insertFriend(data: Friend) {
        friendDao.insert(data)
    }

    // Memperbarui data teman yang ada dalam database
    // Fungsi ini bersifat suspend dan harus dipanggil dari dalam coroutine
    suspend fun editFriend(data: Friend) {
        friendDao.updateFriend(data)
    }

    // Menghapus data teman dari database
    // Fungsi ini bersifat suspend dan harus dipanggil dari dalam coroutine
    suspend fun deleteFriend(data: Friend) {
        friendDao.delete(data)
    }

    override fun apiLogout() {

    }

    override fun apiRenewToken() {

    }
}
