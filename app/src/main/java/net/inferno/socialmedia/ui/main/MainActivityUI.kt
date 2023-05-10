package net.inferno.socialmedia.ui.main

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.inferno.socialmedia.data.PreferencesDataStore
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.ui.apps.AppsUI
import net.inferno.socialmedia.ui.editProfile.EditProfileUI
import net.inferno.socialmedia.ui.followers.UserFollowersUI
import net.inferno.socialmedia.ui.followes.UserFollowesUI
import net.inferno.socialmedia.ui.home.HomeUI
import net.inferno.socialmedia.ui.image.ImageUI
import net.inferno.socialmedia.ui.login.LoginUI
import net.inferno.socialmedia.ui.profile.UserProfileUI
import net.inferno.socialmedia.ui.register.RegisterUI

@Composable
fun MainActivityUI() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val preferences = remember {
        PreferencesDataStore(context)
    }

//    val start = Routes.LOGIN

    val start = if (preferences.isUserLoggedIn) Routes.HOME else Routes.LOGIN

    var navigated by rememberSaveable { mutableStateOf(false) }
    if (preferences.isUserLoggedIn && !navigated) {
        navigated = true
        LaunchedEffect(Unit) {
            navController.navigate(Routes.profile(null))
        }
    }

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

        composable(
            "${Routes.USER_FOLLOWERS}?userId={userId}",
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
                nullable = true
            }),
        ) {
            UserFollowersUI(navController = navController)
        }

        composable(
            "${Routes.USER_FOLLOWINGS}?userId={userId}",
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
                nullable = true
            }),
        ) {
            UserFollowesUI(navController = navController)
        }

        composable(
            "${Routes.USER_PROFILE}?userId={userId}",
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
                nullable = true
            }),
        ) {
            UserProfileUI(navController = navController)
        }

        composable(
            Routes.EDIT_PROFILE,
        ) {
            EditProfileUI(navController = navController)
        }

        composable(
            "${Routes.IMAGE}?imageUrl={imageUrl}",
            arguments = listOf(navArgument("imageUrl") {
                type = NavType.StringType
            }),
        ) {
            val imageUrl = it.arguments!!.getString("imageUrl")!!

            ImageUI(
                navController = navController,
                imageUrl = imageUrl,
            )
        }
    }
}

object Routes {
    const val APPS = "apps"

    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "reset_password"

    const val USER_PROFILE = "user/profile"
    const val EDIT_PROFILE = "user/profile/edit"
    const val USER_FOLLOWERS = "user/followers"
    const val USER_FOLLOWINGS = "user/followings"

    const val IMAGE = "image"

    fun profile(user: User?): String {
        val uri = Uri.Builder()
            .path(USER_PROFILE)

        if (user != null) {
            uri.appendQueryParameter("userId", user.id)
        }

        return uri.toString()
    }

    fun followers(user: User?): String {
        val uri = Uri.Builder()
            .path(USER_FOLLOWERS)

        if (user != null) {
            uri.appendQueryParameter("userId", user.id)
        }

        return uri.toString()
    }

    fun followings(user: User?): String {
        val uri = Uri.Builder()
            .path(USER_FOLLOWINGS)

        if (user != null) {
            uri.appendQueryParameter("userId", user.id)
        }

        return uri.toString()
    }

    fun image(imageUrl: String) = Uri.Builder()
        .path(IMAGE)
        .appendQueryParameter("imageUrl", imageUrl)
        .toString()
}