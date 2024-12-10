package com.example.myfriend.data

import androidx.room.*
import com.crocodic.core.data.CoreDao
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao : CoreDao<Friend> {

    // Mengambil item teman berdasarkan ID. Mengembalikan Flow<Friend?> untuk observasi data.
    @Query("SELECT * from friend WHERE id = :id")
    fun getItemById(id: Int): Flow<Friend?>

    @Update
    suspend fun updateFriend(friend: Friend)

    // Mengambil semua entitas teman dari database. Mengembalikan Flow<List<Friend>> untuk observasi data.
    @Query("SELECT * FROM friend")
    fun getAll(): Flow<List<Friend>>

    @Query("SELECT * FROM friend WHERE name LIKE :keyword OR school LIKE :keyword")
    fun findFriend(keyword: String): Flow<List<Friend>>

    @Query("SELECT * FROM friend")
    fun findFriend(): Flow<List<Friend>>

    suspend fun searchFriend(keyword: String? = null): Flow<List<Friend>>{
        return if(keyword.isNullOrEmpty()) findFriend() else findFriend("%$keyword%")
    }
}
