package com.example.nhlive.uiElements

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nhlive.GameListViewModel
import com.example.nhlive.dataElements.GameSorter
import com.example.nhlive.ui.theme.NHLiveTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListScreenWithRefresh(
    viewModel: GameListViewModel,
    onGameClick: (Int) -> Unit = {},
    onPOTWClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.observeAsState(GameListViewModel.UiState())
    NHLiveTheme(darkTheme = uiState.isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = { onPOTWClick() }) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Player of the Week"
                                )
                            }
                            IconButton(onClick = { viewModel.toggleTheme() }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Toggle Theme"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    expandedHeight = 30.dp
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.loadSchedule() },
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.50f),
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = ""
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.errorMessage != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading games",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (uiState.scheduleResponse?.gameWeek.isNullOrEmpty()) {
                    Text(
                        text = "No games scheduled for today",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column {
                        Text(
                            text = "Live Scores:",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 15.dp, start = 15.dp, bottom = 5.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                                .testTag("game_list")
                        ) {
                            val allGames =
                                uiState.scheduleResponse?.gameWeek?.flatMap { it.games }
                                    ?: emptyList()
                            val sortedGames = GameSorter.sortGamesByStatus(allGames)

                            items(sortedGames) { game ->
                                val homeTeamStats = uiState.teamStats[game.homeTeam.id]
                                val awayTeamStats = uiState.teamStats[game.awayTeam.id]
                                val gameDetails = uiState.gameDetails[game.id]

                                GameItemComposable(
                                    game = game,
                                    homeTeamStats = homeTeamStats,
                                    awayTeamStats = awayTeamStats,
                                    gameDetailsResponse = gameDetails,
                                    onGameClick = onGameClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
