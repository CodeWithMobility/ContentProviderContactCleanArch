package com.android4you.contactsprovider.di

import android.content.Context
import com.android4you.contactsprovider.data.repository.ContactRepositoryImpl
import com.android4you.contactsprovider.domain.repository.ContactRepository
import com.android4you.contactsprovider.domain.usecase.GetContactsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ContactModule {

    @Provides
    fun provideContactRepository(@ApplicationContext context: Context): ContactRepository {
        return ContactRepositoryImpl(context.contentResolver)
    }

    @Provides
    fun provideGetContactsUseCase(repository: ContactRepository): GetContactsUseCase {
        return GetContactsUseCase(repository)
    }
}
