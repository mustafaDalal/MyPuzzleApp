package com.md.mypuzzleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.md.mypuzzleapp.presentation.common.Screen
import com.md.mypuzzleapp.presentation.home.HomeScreen
import com.md.mypuzzleapp.presentation.puzzle.PuzzleScreen
import com.md.mypuzzleapp.presentation.settings.SettingsScreen
import com.md.mypuzzleapp.ui.theme.MyPuzzleAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyPuzzleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(route = Screen.Home.route) {
                            HomeScreen(
                                onNavigate = { navEvent ->
                                    navController.navigate(navEvent.route)
                                }
                            )
                        }
                        
                        composable(
                            route = Screen.Puzzle.route,
                            arguments = listOf(
                                navArgument("puzzleId") {
                                    type = NavType.StringType
                                }
                            )
                        ) {
                            // PuzzleScreen will be implemented later
                            // For now, we'll just navigate back when a puzzle is selected
                            PuzzleScreen(onNavigateBack = { navController.navigate(Screen.Home.route)})


                        }

                        composable(route = Screen.Settings.route) {
                            SettingsScreen(
                                navController = navController,
                            )
                        }
                    }
                }
            }
        }
    }
}