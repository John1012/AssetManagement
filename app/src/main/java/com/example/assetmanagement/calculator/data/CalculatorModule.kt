package com.example.assetmanagement.calculator.data

import android.content.Context
import androidx.room.Room
import com.example.assetmanagement.calculator.data.local.CalculationDao
import com.example.assetmanagement.calculator.data.local.CalculationDatabase
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CalculatorModule {

    @Binds
    @Singleton
    abstract fun bindCalculationRepository(impl: CalculationRepositoryImpl): CalculationRepository

    companion object {
        @Provides
        @Singleton
        fun provideCalculationDatabase(@ApplicationContext context: Context): CalculationDatabase =
            Room.databaseBuilder(context, CalculationDatabase::class.java, "calculations.db").build()

        @Provides
        fun provideCalculationDao(db: CalculationDatabase): CalculationDao = db.calculationDao()
    }
}
