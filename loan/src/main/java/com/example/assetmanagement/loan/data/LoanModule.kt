package com.example.assetmanagement.loan.data

import android.content.Context
import androidx.room.Room
import com.example.assetmanagement.loan.data.local.LoanDao
import com.example.assetmanagement.loan.data.local.LoanDatabase
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoanModule {

    @Binds
    @Singleton
    abstract fun bindLoanRepository(impl: LoanRepositoryImpl): LoanRepository

    companion object {
        @Provides
        @Singleton
        fun provideLoanDatabase(@ApplicationContext context: Context): LoanDatabase =
            Room.databaseBuilder(context, LoanDatabase::class.java, "loans.db").build()

        @Provides
        fun provideLoanDao(db: LoanDatabase): LoanDao = db.loanDao()
    }
}
