package com.example.nhlive.uiElements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nhlive.GameListViewModel
import com.example.nhlive.ui.theme.NHLiveTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POTWScreen(
    viewModel: GameListViewModel,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.observeAsState(GameListViewModel.UiState())
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
                            Text("Star Of The Week", textAlign = TextAlign.Center)
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
            Text(
                text = "Player of the Week Content Goes Here",
                modifier = Modifier.padding(paddingValues).padding(16.dp)
            )
        }
    }
}
