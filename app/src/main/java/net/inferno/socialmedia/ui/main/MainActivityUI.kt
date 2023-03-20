package net.inferno.socialmedia.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.inferno.socialmedia.data.PreferencesDataStore
import net.inferno.socialmedia.ui.apps.AppsUI
import net.inferno.socialmedia.ui.home.HomeUI
import net.inferno.socialmedia.ui.login.LoginUI
import net.inferno.socialmedia.ui.register.RegisterUI

@Composable
fun MainActivityUI() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val preferences = remember {
        PreferencesDataStore(context)
    }

    val start = if (preferences.isUserLoggedIn) Routes.HOME else Routes.LOGIN

    NavHost(
        navController,
        start,
    ) {
        composable(Routes.LOGIN) {
            LoginUI(navController = navController)
        }

        composable(Routes.REGISTER) {
            RegisterUI(navController = navController)
        }

        composable(Routes.APPS) {
            AppsUI(navController = navController)
        }

        composable(Routes.HOME) {
            HomeUI(navController = navController)
        }
    }
}

object Routes {
    const val APPS = "apps"

    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "reset_password"
}