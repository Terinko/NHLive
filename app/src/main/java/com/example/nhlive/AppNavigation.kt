import android.R.attr.type
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nhlive.GameListViewModel
import com.example.nhlive.uiElements.GameDetailScreen
import com.example.nhlive.uiElements.GameListScreenWithRefresh
import com.example.nhlive.uiElements.POTWScreen

object AppRoutes {
    const val GAME_LIST = "gameList"
    const val GAME_DETAIL = "gameDetail/{gameId}"
    const val POTW = "potw"

    fun gameDetail(gameId: Int) = "gameDetail/$gameId"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: GameListViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.GAME_LIST
    ) {
        composable(AppRoutes.GAME_LIST) {
            GameListScreenWithRefresh(
                viewModel = viewModel,
                onGameClick = { gameId ->
                    navController.navigate(AppRoutes.gameDetail(gameId))
                },
                onPOTWClick = {
                    navController.navigate(AppRoutes.POTW)
                }
            )
        }

        composable(
            route = AppRoutes.GAME_DETAIL,
            arguments = listOf(
                navArgument("gameId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getInt("gameId") ?: 0
            GameDetailScreen(
                gameId = gameId,
                viewModel = viewModel,
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.POTW) {
            POTWScreen(
                viewModel = viewModel,
                onBackPressed = { navController.popBackStack() },
                playerId = 8482116
            )
        }
    }
}
