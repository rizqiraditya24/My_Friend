package com.example.myfriend.mvvm

import android.content.Context
import com.example.myfriend.data.MyDatabase
import com.crocodic.core.data.CoreSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Singleton
    @Provides
    fun provideCoreSession(@ApplicationContext context: Context) = CoreSession(context)

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context):MyDatabase =
        MyDatabase.getInstance(context)

    @Singleton
    @Provides
    fun provideFriendDao(database: MyDatabase) = database.friendDao()
}
