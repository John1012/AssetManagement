package com.example.assetmanagement.loan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Insert
    suspend fun insert(entity: LoanEntity)

    @Query("SELECT * FROM loans ORDER BY savedAt DESC")
    fun getAll(): Flow<List<LoanEntity>>

    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun deleteById(id: Long)
}
