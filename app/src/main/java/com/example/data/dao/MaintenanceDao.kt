package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MaintenanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance ORDER BY nextDue ASC, id DESC")
    fun getAllMaintenance(): Flow<List<MaintenanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenance(maintenance: MaintenanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<MaintenanceEntity>)

    @Update
    suspend fun updateMaintenance(maintenance: MaintenanceEntity)

    @Delete
    suspend fun deleteMaintenance(maintenance: MaintenanceEntity)

    @Query("DELETE FROM maintenance WHERE id = :id")
    suspend fun deleteMaintenanceById(id: Long)
}
