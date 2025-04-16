package com.example.nhlive

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.example.nhlive.ui.theme.NHLiveTheme
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.text.set

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListScreen() {
    var scheduleResponse by remember { mutableStateOf<ScheduleResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDarkTheme by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Change this to store TeamStats objects instead of TeamRecordResponse
    val teamStats = remember { mutableStateMapOf<Int, TeamStats>() }

    // Add a map to store game details for live games
    val gameDetails = remember { mutableStateMapOf<Int, GameDetailsResponse>() }

    // Fetch games on first composition
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // Actual API call
                Log.d("API_CALL", "Calling NHL API: ${ApiClient.retrofit.baseUrl()}schedule/now")
                scheduleResponse = ApiClient.apiService.getTodaySchedule()
                Log.d("API_SUCCESS", "API call succeeded. Games found: ${scheduleResponse?.gameWeek?.flatMap { it.games }?.size ?: 0}")
                errorMessage = null
            } catch (e: Exception) {
                // Error handling
                when (e) {
                    is JsonSyntaxException -> {
                        Log.e("API_ERROR", "JSON Syntax Error: ${e.message}")
                        errorMessage = "Unable to parse API response: ${e.message}"
                    }
                    is HttpException -> {
                        val errorBody = e.response()?.errorBody()?.string() ?: "Unknown error"
                        Log.e("API_ERROR", "HTTP Error: ${e.code()}, Body: $errorBody")
                        errorMessage = "API error: ${e.code()}"
                    }
                    else -> {
                        Log.e("API_ERROR", "General Error: ${e.message}")
                        errorMessage = "Failed to load games: ${e.message}"
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Fetch team stats once (not for each game)
    LaunchedEffect(scheduleResponse) {
        if (scheduleResponse != null) {
            coroutineScope.launch {
                try {
                    Log.d("TEAM_STATS", "Fetching team stats...")
                    // Use statsApiService instead of apiService
                    val statsResponse = ApiClient.statsApiService.getTeamStats()

                    // Store team stats by team ID for easy lookup
                    for (stat in statsResponse.data) {
                        teamStats[stat.teamId] = stat
                        Log.d("TEAM_STATS", "Loaded stats for ${stat.teamFullName}: ${stat.wins}-${stat.losses}-${stat.otLosses}")
                    }

                    Log.d("TEAM_STATS", "Successfully loaded stats for ${teamStats.size} teams")
                } catch (e: Exception) {
                    Log.e("TEAM_STATS", "Failed to fetch team stats: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    // Fetch game details for live games and periodically update them
    LaunchedEffect(scheduleResponse) {
        if (scheduleResponse != null) {
            coroutineScope.launch {
                while (true) {
                    val liveGames = scheduleResponse?.gameWeek?.flatMap { it.games }?.filter {
                        it.gameState == "LIVE" || it.gameState == "CRIT" || it.gameState == "FINAL"
                    } ?: emptyList()

                    for (game in liveGames) {
                        try {
                            Log.d("GAME_DETAILS", "Fetching details for game: ${game.id}")
                            val details = ApiClient.apiService.getGameDetails(game.id)
                            gameDetails[game.id] = details
                            Log.d("GAME_DETAILS", "Loaded details for game ${game.id}: Period ${details.displayPeriod}, Time: ${details.clock.timeRemaining}")
                        } catch (e: Exception) {
                            Log.e("GAME_DETAILS", "Failed to fetch details for game ${game.id}: ${e.message}")
                        }
                    }

                    // Update every 30 seconds for live games
                    delay(30000)
                }
            }
        }
    }

    NHLiveTheme(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    expandedHeight = 30.dp,
                    actions = {
                        Row (
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Filled.AccountCircle else Icons.Filled.AccountCircle,
                                    contentDescription = "Player of the Week"
                                )
                            }
                            IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Filled.Settings else Icons.Filled.Settings,
                                    contentDescription = "Toggle Theme"
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator()
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = errorMessage ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    scheduleResponse == null -> Text("No games found")
                    else -> {
                        val allGames = scheduleResponse!!.gameWeek.flatMap { it.games }

                        if (allGames.isEmpty()) {
                            Text("No games scheduled for today")
                        } else {
                            Column {
                                Text(
                                    text = "Live Scores:",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 15.dp, start = 15.dp, bottom = 5.dp)
                                )
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(allGames) { game ->
                                        GameItemComposable(
                                            game = game,
                                            homeTeamStats = teamStats[game.homeTeam.id],
                                            awayTeamStats = teamStats[game.awayTeam.id],
                                            gameDetailsResponse = gameDetails[game.id]
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameItemComposable(
    game: Game,
    homeTeamStats: TeamStats?,
    awayTeamStats: TeamStats?,
    gameDetailsResponse: GameDetailsResponse?
) {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            add(SvgDecoder.Factory())
        }
        .build()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (game.gameState) {
                        "FUT" -> "Upcoming"
                        "PRE" -> "Pregame"
                        "LIVE" -> "LIVE"
                        "FINAL" -> "Final"
                        "CRIT" -> "Overtime"
                        "OFF" -> "Official Score"
                        else -> game.gameState
                    },
                    color = when (game.gameState) {
                        "LIVE" -> Color.Red
                        "FINAL" -> Color.Green
                        "CRIT" -> Color.Yellow
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Text(
                    // Display formatted date/time or period info based on game state
                    text = if (game.gameState == "FUT" || game.gameState == "PRE") {
                        game.formattedDateTime
                    } else if (game.gameState == "FINAL" || game.gameState == "OFF") {
                        " "
                    } else if (gameDetailsResponse != null) {
                        // Display period and time for live games
                        val period = gameDetailsResponse.displayPeriod
                        val periodText = when (period) {
                            1 -> "1st"
                            2 -> "2nd"
                            3 -> "3rd"
                            else -> "${period}th"
                        }

                        if (gameDetailsResponse.clock.inIntermission) {
                            "Intermission"
                        } else {
                            "$periodText Period - ${gameDetailsResponse.clock.timeRemaining}"
                        }
                    } else if (game.gameState == "CRIT") {
                        "In OT"
                    } else {
                        "In Game"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // Home Team Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = "https://assets.nhle.com/logos/nhl/svg/${game.homeTeam.abbrev}_light.svg",
                        contentDescription = "Home Team Logo",
                        imageLoader = imageLoader,
                        modifier = Modifier
                            .size(50.dp),
                        contentScale = ContentScale.Inside
                    )
                    Column(modifier = Modifier.padding(start = 5.dp)) {
                        // TODO: surround with row and add optional star icon after name if favorite team
                        Text(
                            text = "${game.homeTeam.placeName.default} ${game.homeTeam.commonName.default}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        // Team Record display with loading state
                        when {
                            homeTeamStats != null -> {
                                Text(
                                    text = "(${homeTeamStats.wins}-${homeTeamStats.losses}-${homeTeamStats.otLosses})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            else -> {
                                Text(
                                    text = "Loading record...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = game.homeTeam.score?.toString() ?: "-",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            // Away Team Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = "https://assets.nhle.com/logos/nhl/svg/${game.awayTeam.abbrev}_light.svg",
                        contentDescription = "Away Team Logo",
                        imageLoader = imageLoader,
                        modifier = Modifier
                            .size(50.dp),
                        contentScale = ContentScale.Inside
                    )
                    Column(modifier = Modifier.padding(start = 5.dp)) {
                        Text(
                            text = "${game.awayTeam.placeName.default} ${game.awayTeam.commonName.default}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        // Team Record display with loading state
                        when {
                            awayTeamStats != null -> {
                                Text(
                                    text = "(${awayTeamStats.wins}-${awayTeamStats.losses}-${awayTeamStats.otLosses})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            else -> {
                                Text(
                                    text = "Loading record...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = game.awayTeam.score?.toString() ?: "-",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}