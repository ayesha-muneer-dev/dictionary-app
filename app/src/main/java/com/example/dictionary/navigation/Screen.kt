package com.example.dictionary.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Dictionary : Screen("dictionary_screen")
}
