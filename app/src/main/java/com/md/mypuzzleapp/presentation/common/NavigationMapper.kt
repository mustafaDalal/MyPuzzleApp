package com.md.mypuzzleapp.presentation.common

import com.md.mypuzzleapp.presentation.home.MenuItem

object NavigationMappers {
    fun MenuItem.mapMenuOptionToRoute() : Screen{

        return when(this){
            MenuItem.SETTINGS -> Screen.Settings
        }
    }

    fun Screen.mapRouteToMenuOption() : MenuItem {
        return when(this){
            Screen.Settings-> MenuItem.SETTINGS
            Screen.Puzzle -> MenuItem.valueOf("puzzle")/*no-op*/
            Screen.Home -> MenuItem.valueOf("home")/*no-op*/
        }
    }
}