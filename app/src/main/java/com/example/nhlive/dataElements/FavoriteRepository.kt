package com.example.nhlive.dataElements

import kotlinx.coroutines.flow.Flow

class FavoriteRepository(private val userDao: TeamsDao) {
    val allFavorites: Flow<List<Teams>> = userDao.getAllTeams()
    suspend fun addFavorite(teamName: String) = userDao.insert(Teams(teamName = teamName))
    suspend fun removeFavorite(id: Long)        = userDao.deleteById(id)
}