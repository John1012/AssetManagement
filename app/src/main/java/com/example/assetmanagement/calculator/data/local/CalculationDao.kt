package com.example.assetmanagement.calculator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Insert
    suspend fun insert(entity: CalculationEntity)

    @Query("SELECT * FROM calculations ORDER BY savedAt DESC")
    fun getAll(): Flow<List<CalculationEntity>>

    @Query("DELETE FROM calculations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
