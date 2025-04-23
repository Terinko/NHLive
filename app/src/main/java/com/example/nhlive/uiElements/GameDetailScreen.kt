package com.example.nhlive.uiElements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.example.nhlive.GameListViewModel
import com.example.nhlive.dataElements.Game
import com.example.nhlive.dataElements.GameDetailsResponse
import com.example.nhlive.dataElements.TeamStats
import com.example.nhlive.ui.theme.NHLiveTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    gameId: Int,
    viewModel: GameListViewModel,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.observeAsState(GameListViewModel.UiState())

    // Find the selected game in the schedule
    val game = uiState.scheduleResponse?.gameWeek?.flatMap { it.games }?.find { it.id == gameId }
    val gameDetails = uiState.gameDetails[gameId]
    val homeTeamStats = game?.let { uiState.teamStats[it.homeTeam.id] }
    val awayTeamStats = game?.let { uiState.teamStats[it.awayTeam.id] }

    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            add(SvgDecoder.Factory())
        }
        .build()

    NHLiveTheme(darkTheme = uiState.isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    actions = {
                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            IconButton(onClick = onBackPressed) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back"
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
            }
        ) { paddingValues ->
            if (game == null) {
                // Show loading or error state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Game details content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Game status section
                    GameStatusSection(game, gameDetails)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Teams scoreboard section
                    TeamsScoreboardSection(game, homeTeamStats, awayTeamStats, imageLoader)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Game details section if available
                    if (gameDetails != null) {
                        GamePeriodInfoSection(gameDetails)

                        Spacer(modifier = Modifier.height(24.dp))

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GameStatusSection(
    game: Game,
    gameDetails: GameDetailsResponse?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (game.gameState) {
                "LIVE", "CRIT" -> MaterialTheme.colorScheme.errorContainer
                "FINAL", "OFF" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (game.gameState) {
                    "FUT" -> "Upcoming Game"
                    "PRE" -> "Pregame"
                    "LIVE" -> "LIVE"
                    "FINAL" -> "Final"
                    "CRIT" -> "Critical Game Time"
                    "OFF" -> "Official Final Score"
                    else -> game.gameState
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = when (game.gameState) {
                    "LIVE", "CRIT" -> MaterialTheme.colorScheme.onErrorContainer
                    "FINAL", "OFF" -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                }
            )

            Text(
                text = if (game.gameState == "FUT" || game.gameState == "PRE") {
                    game.formattedDateTime
                } else if (game.gameState == "FINAL" || game.gameState == "OFF") {
                    "Game Complete"
                } else if (gameDetails != null) {
                    val period = gameDetails.displayPeriod
                    val periodText = when (period) {
                        1 -> "1st"
                        2 -> "2nd"
                        3 -> "3rd"
                        else -> "${period}th"
                    }

                    if (gameDetails.clock.inIntermission) {
                        "Intermission"
                    } else {
                        "$periodText - ${gameDetails.clock.timeRemaining}"
                    }
                } else {
                    "In Progress"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun TeamsScoreboardSection(
    game: Game,
    homeTeamStats: TeamStats?,
    awayTeamStats: TeamStats?,
    imageLoader: ImageLoader
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Away team column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            AsyncImage(
                model = "https://assets.nhle.com/logos/nhl/svg/${game.awayTeam.abbrev}_light.svg",
                contentDescription = "Away Team Logo",
                imageLoader = imageLoader,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Inside
            )

            Text(
                text = game.awayTeam.commonName.default,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Score column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            if (game.gameState != "FUT" && game.gameState != "PRE") {
                Text(
                    text = "${game.awayTeam.score ?: 0} - ${game.homeTeam.score ?: 0}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }

        // Home team column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            AsyncImage(
                model = "https://assets.nhle.com/logos/nhl/svg/${game.homeTeam.abbrev}_light.svg",
                contentDescription = "Home Team Logo",
                imageLoader = imageLoader,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Inside
            )

            Text(
                text = game.homeTeam.commonName.default,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
private fun GamePeriodInfoSection(gameDetails: GameDetailsResponse) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Period Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current Period:",
                    style = MaterialTheme.typography.bodyMedium
                )

                val periodText = when (gameDetails.displayPeriod) {
                    1 -> "1st Period"
                    2 -> "2nd Period"
                    3 -> "3rd Period"
                    4 -> "Overtime"
                    5 -> "Double Overtime"
                    6 -> "Triple Overtime"
                    else -> "${gameDetails.displayPeriod}th Period"
                }

                Text(
                    text = periodText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Time Remaining:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (gameDetails.clock.inIntermission) "Intermission" else gameDetails.clock.timeRemaining,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Clock Status:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (gameDetails.clock.running) "Running" else "Stopped",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (gameDetails.clock.running) Color.Green else Color.Red
                )
            }
        }
    }
}