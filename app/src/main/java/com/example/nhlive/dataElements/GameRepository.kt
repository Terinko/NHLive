package com.example.nhlive.dataElements

import android.util.Log
import com.example.nhlive.API.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GameRepository {

    suspend fun getTodaySchedule(): Result<ScheduleResponse> {
        return try {
            Log.d("API_CALL", "Calling NHL API: ${ApiClient.retrofit.baseUrl()}schedule/now")
            val response = ApiClient.apiService.getTodaySchedule()
            Log.d("API_SUCCESS", "API call succeeded. Games found: ${response.gameWeek.flatMap { it.games }.size}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("API_ERROR", "Failed to load games: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getTeamStats(): Result<TeamStatsResponse> {
        return try {
            Log.d("TEAM_STATS", "Fetching team stats...")
            val response = ApiClient.statsApiService.getTeamStats()
            Log.d("TEAM_STATS", "Successfully loaded stats for ${response.data.size} teams")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("TEAM_STATS", "Failed to fetch team stats: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getGameDetails(gameId: Int): Result<GameDetailsResponse> {
        return try {
            Log.d("GAME_DETAILS", "Fetching details for game: $gameId")
            val details = ApiClient.apiService.getGameDetails(gameId)
            Log.d("GAME_DETAILS", "Loaded details for game $gameId: Period ${details.displayPeriod}, Time: ${details.clock.timeRemaining}")
            Result.success(details)
        } catch (e: Exception) {
            Log.e("GAME_DETAILS", "Failed to fetch details for game $gameId: ${e.message}")
            Result.failure(e)
        }
    }

    //Update both at the same time to save lines
    data class LiveGameUpdate(
        val scheduleResponse: ScheduleResponse? = null,
        val gameDetails: Map<Int, GameDetailsResponse> = emptyMap()
    )

    //Stream of live game updates including scores and details
    fun getLiveGameUpdates(refreshIntervalMs: Long = 30000): Flow<LiveGameUpdate> = flow {
        while (true) {
            val gameDetailsMap = mutableMapOf<Int, GameDetailsResponse>()
            var latestSchedule: ScheduleResponse? = null

            //Refresh
            getTodaySchedule().onSuccess { schedule ->
                latestSchedule = schedule
                Log.d("LIVE_UPDATES", "Schedule refreshed with ${schedule.gameWeek.flatMap { it.games }.size} games")

                val liveGames = schedule.gameWeek.flatMap { it.games }.filter {
                    it.gameState == "LIVE" || it.gameState == "CRIT" || it.gameState == "FINAL"
                }

                for (game in liveGames) {
                    getGameDetails(game.id).onSuccess { details ->
                        gameDetailsMap[game.id] = details
                    }
                }
            }

            //Emit combined update back to viewModel
            emit(LiveGameUpdate(latestSchedule, gameDetailsMap))

            //TODO: Taken from stackoverflow ask about what this actually does
            kotlinx.coroutines.delay(refreshIntervalMs)
        }
    }
}