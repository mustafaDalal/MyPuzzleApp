package com.md.mypuzzleapp.presentation.common

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Puzzle : Screen("puzzle/{puzzleId}") {
        fun createRoute(puzzleId: String): String {
            return "puzzle/$puzzleId"
        }
    }
} 