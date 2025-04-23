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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Details") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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

                    // Game timeline section
                    GameTimeline(game, gameDetails)

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Venue and broadcast information
                GameVenueSection(game)
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
            modifier = Modifier.padding(16.dp),
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
                        "$periodText Period - ${gameDetails.clock.timeRemaining}"
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
        horizontalArrangement = Arrangement.SpaceBetween
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
                contentScale = ContentScale.FillBounds
            )

            Text(
                text = game.awayTeam.placeName.default,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = game.awayTeam.commonName.default,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (awayTeamStats != null) {
                Text(
                    text = "(${awayTeamStats.wins}-${awayTeamStats.losses}-${awayTeamStats.otLosses})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                contentScale = ContentScale.FillBounds
            )

            Text(
                text = game.homeTeam.placeName.default,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = game.homeTeam.commonName.default,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (homeTeamStats != null) {
                Text(
                    text = "(${homeTeamStats.wins}-${homeTeamStats.losses}-${homeTeamStats.otLosses})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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

@Composable
private fun GameVenueSection(game: Game) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Game Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Divider()

            if (game.gameCenterLink.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Game Center:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Available",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // TV broadcasts if available
            if (game.tvBroadcasts.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TV Broadcast:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        game.tvBroadcasts.take(2).forEach { broadcast ->
                            Text(
                                text = "${broadcast.network} (${broadcast.market})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Time zone information
            if (game.venueTimezone.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Local Time Zone:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = game.venueTimezone,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Season information
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Season:",
                    style = MaterialTheme.typography.bodyMedium
                )

                val seasonText = if (game.season > 0) {
                    val start = game.season / 10000
                    val end = start + 1
                    "$start-$end"
                } else {
                    "N/A"
                }

                Text(
                    text = seasonText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GameTimeline(
    game: Game,
    gameDetails: GameDetailsResponse?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Game Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Game timeline events based on state
            when (game.gameState) {
                "FUT" -> {
                    // Future game
                    TimelineItem(
                        icon = Icons.Default.AccountCircle,
                        title = "Game scheduled",
                        subtitle = game.formattedDateTime,
                        isActive = true
                    )
                    TimelineItem(
                        icon = Icons.Default.Build,
                        title = "Game Start",
                        subtitle = "Upcoming",
                        isActive = false
                    )
                    TimelineItem(
                        icon = Icons.Default.Notifications,
                        title = "First Period",
                        subtitle = "Not started",
                        isActive = false
                    )
                }
                "PRE" -> {
                    // Pregame
                    TimelineItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Game scheduled",
                        subtitle = game.formattedDateTime,
                        isActive = true,
                        isCompleted = true
                    )
                    TimelineItem(
                        icon = Icons.Default.Add,
                        title = "Pregame",
                        subtitle = "Teams warming up",
                        isActive = true
                    )
                    TimelineItem(
                        icon = Icons.Default.Notifications,
                        title = "First Period",
                        subtitle = "Starting soon",
                        isActive = false
                    )
                }
                "LIVE", "CRIT" -> {
                    // Live game
                    TimelineItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Game Start",
                        subtitle = "In progress",
                        isActive = true,
                        isCompleted = true
                    )

                    // Current period
                    if (gameDetails != null) {
                        val period = gameDetails.displayPeriod
                        val periodText = when (period) {
                            1 -> "1st Period"
                            2 -> "2nd Period"
                            3 -> "3rd Period"
                            4 -> "Overtime"
                            5 -> "Double Overtime"
                            else -> "${period}th Period"
                        }

                        if (gameDetails.clock.inIntermission) {
                            TimelineItem(
                                icon = Icons.Default.CheckCircle,
                                title = "Period ${period-1} Complete",
                                subtitle = "Intermission",
                                isActive = true,
                                isCompleted = true
                            )
                            TimelineItem(
                                icon = Icons.Default.Notifications,
                                title = periodText,
                                subtitle = "Starting soon",
                                isActive = true
                            )
                        } else {
                            TimelineItem(
                                icon = Icons.Default.CheckCircle,
                                title = periodText,
                                subtitle = gameDetails.clock.timeRemaining + " remaining",
                                isActive = true
                            )
                        }

                        if (period < 3) {
                            TimelineItem(
                                icon = Icons.Outlined.Person,
                                title = "Game End",
                                subtitle = "Upcoming",
                                isActive = false
                            )
                        } else if (period == 3 && gameDetails.clock.secondsRemaining > 0) {
                            TimelineItem(
                                icon = Icons.Default.Check,
                                title = "Game End",
                                subtitle = "Approaching",
                                isActive = true
                            )
                        }
                    }
                }
                "FINAL", "OFF" -> {
                    // Completed game
                    TimelineItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Game Start",
                        subtitle = "Completed",
                        isActive = true,
                        isCompleted = true
                    )
                    TimelineItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Game Progress",
                        subtitle = "All periods complete",
                        isActive = true,
                        isCompleted = true
                    )
                    TimelineItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Game End",
                        subtitle = "Final Score: ${game.awayTeam.score} - ${game.homeTeam.score}",
                        isActive = true,
                        isCompleted = true
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean,
    isCompleted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timeline node
        Surface(
            shape = CircleShape,
            color = when {
                isCompleted -> MaterialTheme.colorScheme.primary
                isActive -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            border = if (!isCompleted && isActive) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = when {
                    isCompleted -> MaterialTheme.colorScheme.onPrimary
                    isActive -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                modifier = Modifier.padding(8.dp)
            )
        }

        // Event details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }

    // Connector line
    Divider(
        modifier = Modifier
            .height(16.dp)
            .padding(start = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}