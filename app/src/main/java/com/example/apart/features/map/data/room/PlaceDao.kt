package com.example.apart.features.map.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(placeEntity: PlaceEntity)

    @Query("SELECT * FROM places")
    fun getAll(): Flow<List<PlaceEntity>>

    @Delete
    suspend fun delete(placeEntity: PlaceEntity)

    @Update
    suspend fun update(placeEntity: PlaceEntity)
}