package com.zubcic.project2_antoniozubcic.model.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zubcic.project2_antoniozubcic.model.CameraDbEntity

@Dao
interface CameraDao {

    @Query("SELECT * FROM cameras")
    fun cameras(): List<CameraDbEntity>

    @Query("SELECT * FROM cameras WHERE id = :id")
    fun getCamera(id: Int): CameraDbEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCamera(camera: CameraDbEntity)

    @Delete
    suspend fun deleteCamera(camera: CameraDbEntity)

    @Update
    suspend fun updateCamera(camera: CameraDbEntity)
}