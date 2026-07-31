package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.PresetSpec
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY lastUpdatedTimestamp DESC")
    fun getAllPresets(): Flow<List<PresetSpec>>

    @Query("SELECT * FROM presets WHERE id = :id LIMIT 1")
    suspend fun getPresetById(id: String): PresetSpec?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreset(preset: PresetSpec)

    @Query("DELETE FROM presets WHERE id = :id AND isFactoryPreset = 0")
    suspend fun deleteCustomPreset(id: String)
}
