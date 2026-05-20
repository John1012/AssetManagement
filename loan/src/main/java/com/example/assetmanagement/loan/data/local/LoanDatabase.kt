package com.example.assetmanagement.loan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LoanEntity::class], version = 1, exportSchema = false)
abstract class LoanDatabase : RoomDatabase() {
    abstract fun loanDao(): LoanDao
}
