package com.example.nhlive

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhlive.dataElements.GameDetailsResponse
import com.example.nhlive.API.GameRepository
import com.example.nhlive.dataElements.ScheduleResponse
import com.example.nhlive.dataElements.TeamStats
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import retrofit2.HttpException

class GameListViewModel(
    private val repository: GameRepository = GameRepository()
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
        val scheduleResponse: ScheduleResponse? = null,
        val teamStats: Map<Int, TeamStats> = emptyMap(),
        val gameDetails: Map<Int, GameDetailsResponse> = emptyMap(),
        val isDarkTheme: Boolean = false
    )

    private val _uiState = MutableLiveData(UiState())
    val uiState: LiveData<UiState> = _uiState

    init {
        loadSchedule()
    }
    //Start our coroutines to get data from the multiple API calls
    fun loadSchedule() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            try {
                repository.getTodaySchedule().fold(
                    onSuccess = { schedule ->
                        updateState {
                            it.copy(
                                scheduleResponse = schedule,
                                errorMessage = null
                            )
                        }
                        loadTeamStats()
                        startLiveUpdates()
                    },
                    onFailure = { error ->
                        val errorMsg = when (error) {
                            is JsonSyntaxException -> "Unable to parse API response: ${error.message}"
                            is HttpException -> "API error: ${error.code()}"
                            else -> "Failed to load games: ${error.message}"
                        }
                        updateState { it.copy(errorMessage = errorMsg) }
                    }
                )
            } finally {
                updateState { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadTeamStats() {
        viewModelScope.launch {
            repository.getTeamStats().onSuccess { response ->
                val teamStatsMap = response.data.associateBy { it.teamId }
                updateState { it.copy(teamStats = teamStatsMap) }
            }
        }
    }

    private fun startLiveUpdates() {
        viewModelScope.launch {
            // Use the new combined live update flow
            repository.getLiveGameUpdates().collect { update ->
                updateState { currentState ->
                    // Only update schedule if we got a new one (score updates)
                    val updatedSchedule = update.scheduleResponse ?: currentState.scheduleResponse

                    currentState.copy(
                        scheduleResponse = updatedSchedule,
                        gameDetails = update.gameDetails
                    )
                }
            }
        }
    }

    fun toggleTheme() {
        updateState { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    //Update state
    private fun updateState(update: (UiState) -> UiState) {
        _uiState.value = update(_uiState.value ?: UiState())
    }
}