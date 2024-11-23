package com.example.myfriend.repo

import com.example.myfriend.data.Friend
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    suspend fun searchFriend(keyword: String): Flow<List<Friend>>
}