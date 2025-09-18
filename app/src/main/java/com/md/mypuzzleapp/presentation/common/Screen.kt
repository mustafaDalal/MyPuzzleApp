package com.md.mypuzzleapp.presentation.common

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Puzzle : Screen("puzzle/{puzzleId}") {
        fun createRoute(puzzleId: String): String {
            return "puzzle/$puzzleId"
        }
    }

    data object Settings : Screen("settings")
} 