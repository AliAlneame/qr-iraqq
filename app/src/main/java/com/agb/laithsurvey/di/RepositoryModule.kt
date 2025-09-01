package com.agb.laithsurvey.di

import com.agb.laithsurvey.data.repository.ErpRepository
import com.agb.laithsurvey.data.repository.IErpRepository
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