package com.example.nhlive

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    // Endpoint to get NHL schedule for today
    @GET("schedule/now")
    suspend fun getTodaySchedule(): ScheduleResponse

    // Option to get raw response for debugging
    @GET("schedule/now")
    suspend fun getRawSchedule(): ResponseBody

    // Endpoint to get game details
    @GET("gamecenter/{gameId}/boxscore")
    suspend fun getGameDetails(@Path("gameId") gameId: Int): GameDetailsResponse
}