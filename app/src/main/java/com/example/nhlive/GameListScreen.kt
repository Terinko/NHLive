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
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListScreen() {
    var scheduleResponse by remember { mutableStateOf<ScheduleResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDarkTheme by remember { mutableStateOf(false) } // State for theme toggle
    val coroutineScope = rememberCoroutineScope()

    // Fetch games on first composition with improved error handling
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // For debugging raw response
                try {
                    val rawResponse = ApiClient.apiService.getRawSchedule().string()
                    Log.d("API_RESPONSE", "Raw response: $rawResponse")
                } catch (e: Exception) {
                    Log.e("API_ERROR", "Failed to get raw response: ${e.message}")
                }

                // Actual API call
                Log.d("API_CALL", "Calling NHL API: ${ApiClient.retrofit.baseUrl()}schedule/now")
                scheduleResponse = ApiClient.apiService.getTodaySchedule()
                Log.d("API_SUCCESS", "API call succeeded. Games found: ${scheduleResponse?.gameWeek?.flatMap { it.games }?.size ?: 0}")
                errorMessage = null
            } catch (e: Exception) {
                // Enhanced error handling
                when (e) {
                    is JsonSyntaxException -> {
                        Log.e("API_ERROR", "JSON Syntax Error: ${e.message}")
                        Log.e("API_ERROR", "Cause: ${e.cause?.message}")
                        e.printStackTrace()
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

    NHLiveTheme(darkTheme = isDarkTheme) { // Apply theme dynamically
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("NHL Games") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        IconButton(onClick = { isDarkTheme = !isDarkTheme }) { // Toggle theme
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Filled.Settings else Icons.Filled.Settings,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background), // Set background color dynamically
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
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(allGames) { game ->
                                    GameItemComposable(game = game)
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
fun GameItemComposable(game: Game) {
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
            Text(
                text = when (game.gameState) {
                    "FUT" -> "Upcoming"
                    "PRE" -> "Pregame"
                    "LIVE" -> "LIVE"
                    "FINAL" -> "Final"
                    "CRIT" -> "Overtime"
                    else -> game.gameState
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(bottom = 5.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = "https://assets.nhle.com/logos/nhl/svg/${game.homeTeam.abbrev}_light.svg",
                        contentDescription = "Home Team Logo",
                        imageLoader = imageLoader,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "${game.homeTeam.placeName.default} ${game.homeTeam.commonName.default}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Text(
                    text = game.homeTeam.score?.toString() ?: "-",
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = "https://assets.nhle.com/logos/nhl/svg/${game.awayTeam.abbrev}_light.svg",
                        contentDescription = "Away Team Logo",
                        imageLoader = imageLoader,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "${game.awayTeam.placeName.default} ${game.awayTeam.commonName.default}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Text(
                    text = game.awayTeam.score?.toString() ?: "-",
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = game.formattedDateTime,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
