package com.example.nhlive.uiElements

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nhlive.GameListViewModel
import com.example.nhlive.dataElements.PlayerDetailsResponse
import com.example.nhlive.ui.theme.NHLiveTheme
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POTWScreen(
    viewModel: GameListViewModel,
    onBackPressed: () -> Unit,
    playerId: Int
) {
    val uiState by viewModel.uiState.observeAsState(GameListViewModel.UiState())
    var playerDetails by remember { mutableStateOf<Result<PlayerDetailsResponse>?>(null) }

    LaunchedEffect(playerId) {
        playerDetails = viewModel.getPlayerDetails(playerId)
    }

    NHLiveTheme(darkTheme = uiState.isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Player of the Week") },
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = onBackPressed) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                            Text("")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                playerDetails?.onSuccess { details ->
                    StarCard(details)
                    HorizontalDivider(modifier = Modifier.padding(top = 15.dp, bottom = 15.dp), thickness = 1.dp)
                    WeeklyStats(details)
                }
            }
        }
    }
}

@Composable
private fun StarCard(playerDetails : PlayerDetailsResponse){
    val today = LocalDate.now()
    val sunday = today.with(DayOfWeek.SUNDAY).minusWeeks(2)
    val saturday = today.with(DayOfWeek.SATURDAY).minusWeeks(1)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "STAR OF THE WEEK",
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 42.sp),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Week of ${sunday.monthValue}/${sunday.dayOfMonth} - ${saturday.monthValue}/${saturday.dayOfMonth}",
            modifier = Modifier
                .padding(top = 2.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        AsyncImage(
            model = playerDetails.headshot,
            contentDescription = "Player Headshot",
            modifier = Modifier
                .padding(top = 5.dp)
                .padding(8.dp)
                .size(300.dp)
                .clip(shape = CircleShape)
                .border(width = 2.dp, color = MaterialTheme.colorScheme.onSurface, shape = CircleShape)
        )
        Text(
            text = "${playerDetails.firstName.default} ${playerDetails.lastName.default}",
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 35.sp),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "#${playerDetails.sweaterNumber} | ${playerDetails.position}",
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 35.sp),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WeeklyStats(playerDetails : PlayerDetailsResponse){
    val today = LocalDate.now()
    val sunday = today.with(DayOfWeek.SUNDAY).minusWeeks(2)
    val saturday = today.with(DayOfWeek.SATURDAY).minusWeeks(1)

    val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val totalGames = playerDetails.last5Games.count {
        val gameDate = LocalDate.parse(it.gameDate, dateFormatter)
        (gameDate.isAfter(sunday) || gameDate.isEqual(sunday)) &&
        (gameDate.isBefore(saturday.plusDays(1)) || gameDate.isEqual(saturday))
    }
    val totalGoals = playerDetails.last5Games.filter {
        val gameDate = LocalDate.parse(it.gameDate, dateFormatter)
        (gameDate.isAfter(sunday) || gameDate.isEqual(sunday)) &&
        (gameDate.isBefore(saturday.plusDays(1)) || gameDate.isEqual(saturday))
    }.sumOf { it.goals }
    val totalAssists = playerDetails.last5Games.filter {
        val gameDate = LocalDate.parse(it.gameDate, dateFormatter)
        (gameDate.isAfter(sunday) || gameDate.isEqual(sunday)) &&
        (gameDate.isBefore(saturday.plusDays(1)) || gameDate.isEqual(saturday))
    }.sumOf { it.assists }
    val totalPoints = playerDetails.last5Games.filter {
        val gameDate = LocalDate.parse(it.gameDate, dateFormatter)
        (gameDate.isAfter(sunday) || gameDate.isEqual(sunday)) &&
        (gameDate.isBefore(saturday.plusDays(1)) || gameDate.isEqual(saturday))
    }.sumOf { it.points }

    Text(
        text = "Weekly Stats:",
        modifier = Modifier
            .padding(start = 10.dp, bottom = 10.dp)
            .fillMaxWidth(),
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 25.sp),
        fontWeight = FontWeight.Bold,
        textDecoration = TextDecoration.Underline,
        textAlign = TextAlign.Start
    )
    Text(
        text = "Games: $totalGames",
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center
    )
    Text(
        text = "Goals: $totalGoals",
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center
    )
    Text(
        text = "Assists: $totalAssists",
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center
    )
    Text(
        text = "Points: $totalPoints",
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center
    )
}