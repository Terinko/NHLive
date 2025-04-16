package com.example.nhlive.API

import com.example.nhlive.dataElements.GameDetailsResponse
import com.example.nhlive.dataElements.ScheduleResponse
import com.example.nhlive.dataElements.TeamStatsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Endpoint to get NHL schedule for today
    @GET("schedule/now")
    suspend fun getTodaySchedule(): ScheduleResponse

    // New endpoint for team stats
    @GET("stats/rest/en/team/summary")
    suspend fun getTeamStats(
        @Query("sort") sort: String = "points",
        @Query("cayenneExp") cayenneExp: String = "seasonId=20232024 and gameTypeId=2"
    ): TeamStatsResponse

    // New Endpoint for game details
    // need to add the gameId to the endpoint
    @GET("gamecenter/{gameId}/play-by-play")
    suspend fun getGameDetails(@Path("gameId") gameId: Int): GameDetailsResponse
}