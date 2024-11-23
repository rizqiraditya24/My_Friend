package com.example.myfriend.repo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepoModule {

    @Singleton
    @Binds
    abstract fun bindFriendRepository(friendRepositoryImp: FriendRepositoryImp): FriendRepository
}