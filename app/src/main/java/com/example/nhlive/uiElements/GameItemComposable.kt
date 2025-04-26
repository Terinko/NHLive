package com.example.nhlive.uiElements

import android.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.example.nhlive.dataElements.AppDatabase
import com.example.nhlive.dataElements.FavoriteRepository
import com.example.nhlive.dataElements.Game
import com.example.nhlive.dataElements.GameDetailsResponse
import com.example.nhlive.dataElements.TeamStats

@Composable
fun GameItemComposable(
    game: Game,
    homeTeamStats: TeamStats?,
    awayTeamStats: TeamStats?,
    gameDetailsResponse: GameDetailsResponse?,
    onGameClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val repo    = remember { FavoriteRepository(AppDatabase.getInstance(context).userDao()) }

    val favorites by repo.allFavorites.collectAsState(initial = emptyList())

    val homeName = game.homeTeam.commonName.default
    val awayName = game.awayTeam.commonName.default

    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            add(SvgDecoder.Factory())
        }
        .build()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onGameClick(game.id) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (game.gameState) {
                        "FUT" -> "Upcoming"
                        "PRE" -> "Pregame"
                        "LIVE" -> "LIVE"
                        "FINAL" -> "Final"
                        "CRIT" -> "LIVE"
                        "OFF" -> "Official Score"
                        else -> game.gameState
                    },
                    color = when (game.gameState) {
                        "LIVE" -> Color.Red
                        "FINAL" -> Color.Gray
                        "CRIT" -> Color.hsl(23f, 1f, 0.5f)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = when (game.gameState) {
                        "LIVE" -> FontWeight.ExtraBold
                        else -> FontWeight.Normal
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Text(
                    text = if (game.gameState == "FUT" || game.gameState == "PRE") {
                        game.formattedDateTime
                    } else if (game.gameState == "FINAL" || game.gameState == "OFF") {
                        " "
                    } else if (gameDetailsResponse != null) {
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

            //Home row
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
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Inside
                    )
                    Column(modifier = Modifier.padding(start = 5.dp)) {
                        Row() {
                            Text(
                                text = "${game.homeTeam.placeName.default} ${game.homeTeam.commonName.default}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (favorites.any { it.teamName == homeName }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Favorite",
                                    tint = Color.Yellow,
                                    modifier = Modifier.padding(start = 5.dp)
                                )
                            }
                        }
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

            //Away row
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
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Inside
                    )
                    Column(modifier = Modifier.padding(start = 5.dp)) {
                        Row() {
                            Text(
                                text = "${game.awayTeam.placeName.default} ${game.awayTeam.commonName.default}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (favorites.any { it.teamName == awayName }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Favorite",
                                    tint = Color.Yellow,
                                    modifier = Modifier.padding(start = 5.dp)
                                )
                            }
                        }
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