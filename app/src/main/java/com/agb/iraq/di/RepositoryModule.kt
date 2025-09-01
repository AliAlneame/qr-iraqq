package com.agb.iraq.di

import com.agb.iraq.data.repository.ErpRepository
import com.agb.iraq.data.repository.IErpRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideSurveyRepository(repository: ErpRepository): IErpRepository

}
