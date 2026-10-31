package com.android4you.contactsprovider.di

import android.content.ContentResolver
import android.content.Context
import com.android4you.contactsprovider.data.repository.ContactRepositoryImpl
import com.android4you.contactsprovider.domain.repository.ContactRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
abstract class ContactModule {

    @Binds
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    companion object {
        @Provides
        fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
            context.contentResolver

        @Provides
        @IoDispatcher
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    }
}
