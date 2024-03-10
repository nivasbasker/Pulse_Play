package com.zio.pulseplay.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zio.pulseplay.data.Song

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Song)

    @Query("SELECT * FROM songs ORDER BY time DESC LIMIT 5")
    suspend fun getTopFive(): List<Song>

    // Add more queries as needed
}