package com.charlesmuchogo.livestream

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.charlesmuchogo.livestream.navigation.Navigation
import com.charlesmuchogo.livestream.ui.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        Navigation(navController = rememberNavController())
    }
}
