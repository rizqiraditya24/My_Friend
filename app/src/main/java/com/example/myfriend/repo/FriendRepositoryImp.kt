package com.example.myfriend.repo

import com.example.myfriend.data.Friend
import com.example.myfriend.data.FriendDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FriendRepositoryImp @Inject constructor(private val friendDao: FriendDao) : FriendRepository {
    override suspend fun searchFriend(keyword: String): Flow<List<Friend>> {
        return friendDao.searchFriend(keyword)
    }
}