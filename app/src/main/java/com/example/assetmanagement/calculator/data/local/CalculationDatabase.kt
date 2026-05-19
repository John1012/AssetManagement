package com.example.assetmanagement.calculator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CalculationEntity::class], version = 1, exportSchema = false)
abstract class CalculationDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao
}
