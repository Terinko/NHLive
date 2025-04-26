package com.example.nhlive.dataElements

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamsDao {
    @Insert
    suspend fun insert(user: Teams): Long

    @Query("SELECT * FROM teams ORDER BY teamName ASC")
    fun getAllTeams(): Flow<List<Teams>>

    @Query("DELETE FROM teams WHERE id = :userId")
    suspend fun deleteById(userId: Long)

    companion object
}