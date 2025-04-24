package com.example.nhlive.uiElements

import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.nhlive.dataElements.GameStoryResponse
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
    val gameStory = uiState.gameStories[gameId]

    LaunchedEffect(gameId) {
        try {
            viewModel.loadGameStory(gameId)
        } catch (e: Exception) {
            Log.e("GameDetailScreen", "Error loading game story: ${e.message}", e)
        }
    }

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
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    // Teams scoreboard section
                    TeamsScoreboardSection(game, imageLoader, gameDetails)

                    HorizontalDivider(modifier = Modifier.padding(top = 15.dp, bottom = 15.dp), thickness = 1.dp)

                    // Favorites Buttons
                    FavoritesSection()

                    HorizontalDivider(modifier = Modifier.padding(top = 15.dp, bottom = 15.dp), thickness = 1.dp)

                    Log.i("GameStory", "GameStory: $gameStory, GameId: $gameId")
                    if (gameStory?.summary != null) {
                        GameStorySection(gameStory)
                    }else{
                        Text(
                            text = "No Game Stats Available\nGame Has Not Started",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(20.dp),
                            textAlign = TextAlign.Center
                        )
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
            modifier = Modifier
                .padding(16.dp)
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
    imageLoader: ImageLoader,
    gameDetails: GameDetailsResponse?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home team column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(color = Color.White, shape = CircleShape)
                    .border(width = 2.dp, color = Color.Black, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "https://assets.nhle.com/logos/nhl/svg/${game.homeTeam.abbrev}_light.svg",
                    contentDescription = "Home Team Logo",
                    imageLoader = imageLoader,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Inside
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = game.homeTeam.commonName.default,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Home score
        // Game time
        // Away score
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 30.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // if game is live, show the score and time
                if (game.gameState == "LIVE" || game.gameState == "CRIT") {
                    Text(
                        text = "${game.homeTeam.score ?: 0}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    // show current period and time remaining
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
//                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                    ) {
                        Text(
                            // display 1st, 2nd, 3rd, or OT
                            text = if (gameDetails != null) {
                                when (val period = gameDetails.displayPeriod) {
                                    1 -> "1st"
                                    2 -> "2nd"
                                    3 -> "3rd"
                                    4 -> "OT"
                                    5 -> "2OT"
                                    6 -> "3OT"
                                    else -> "${period}th"
                                }
                            } else {
                                "-th"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (gameDetails != null) {
                                if (gameDetails.clock.inIntermission) {
                                    "Inter"
                                } else {
                                    gameDetails.clock.timeRemaining
                                }
                            } else {
                                "00:00"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = "${game.awayTeam.score ?: 0}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else if (game.gameState == "FINAL" || game.gameState == "OFF") {
                    // if game is final or off, show the final score and say "Final"
                    Text(
                        text = "${game.homeTeam.score ?: 0}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Final",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                    )
                    Text(
                        text = "${game.awayTeam.score ?: 0}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else if (game.gameState == "FUT" || game.gameState == "PRE") {
                    // if game is pre or future, show the start date and time and "-" for score
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = game.formattedDateTimeStacked,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                    )
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Away team column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(color = Color.White, shape = CircleShape)
                    .border(width = 2.dp, color = Color.Black, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "https://assets.nhle.com/logos/nhl/svg/${game.awayTeam.abbrev}_light.svg",
                    contentDescription = "Away Team Logo",
                    imageLoader = imageLoader,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Inside
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = game.awayTeam.commonName.default,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FavoritesSection() {
    var buttonColorHome by remember { mutableStateOf(Color.Transparent) }
    var buttonColorAway by remember { mutableStateOf(Color.Transparent) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            modifier = Modifier
                .padding(1.dp)
                .width(140.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.small
                )
                .background(
                    buttonColorHome,
                    shape = MaterialTheme.shapes.small
                ),
            onClick = {
                buttonColorHome =
                    if (buttonColorHome == Color.Yellow) Color.Transparent else Color.Yellow
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColorHome,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = if (buttonColorHome == Color.Yellow) "Favorited" else "Favorite",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal
            )
        }

        Button(
            modifier = Modifier
                .padding(1.dp)
                .width(140.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.small
                )
                .background(
                    buttonColorAway,
                    shape = MaterialTheme.shapes.small
                ),
            onClick = {
                buttonColorAway =
                    if (buttonColorAway == Color.Yellow) Color.Transparent else Color.Yellow
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColorAway,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = if (buttonColorAway == Color.Yellow) "Favorited" else "Favorite",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun GameStorySection(gameStory: GameStoryResponse) {
    Text(
        text = "Game Stats",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        gameStory.summary.teamGameStats.forEach { stat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stat.homeValue.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = when (stat.category) {
                        "sog" -> "Shots"
                        "faceoffWinningPctg" -> "Faceoff %"
                        "powerPlay" -> "Power Play"
                        "powerPlayPctg" -> "Power Play %"
                        "pim" -> "Penalty Minutes"
                        "hits" -> "Hits"
                        "blockedShots" -> "Blocked Shots"
                        "giveaways" -> "Giveaways"
                        "takeaways" -> "Takeaways"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stat.awayValue.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}