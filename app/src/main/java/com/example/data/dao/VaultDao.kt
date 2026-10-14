package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VaultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault ORDER BY expiryDate ASC, id DESC")
    fun getAllVaultRecords(): Flow<List<VaultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVault(vault: VaultEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<VaultEntity>)

    @Update
    suspend fun updateVault(vault: VaultEntity)

    @Delete
    suspend fun deleteVault(vault: VaultEntity)

    @Query("DELETE FROM vault WHERE id = :id")
    suspend fun deleteVaultById(id: Long)

    @Query("SELECT COUNT(*) FROM vault")
    suspend fun getCount(): Int
}
