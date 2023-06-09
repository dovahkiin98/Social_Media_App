package net.inferno.socialmedia.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.inferno.socialmedia.data.PreferencesDataStore
import net.inferno.socialmedia.model.Community
import net.inferno.socialmedia.ui.apps.AppsUI
import net.inferno.socialmedia.ui.auth.login.LoginUI
import net.inferno.socialmedia.ui.auth.register.RegisterUI
import net.inferno.socialmedia.ui.comment.form.CommentForm
import net.inferno.socialmedia.ui.community.details.CommunityUI
import net.inferno.socialmedia.ui.community.members.CommunityMembersUI
import net.inferno.socialmedia.ui.community.requests.CommunityRequestsUI
import net.inferno.socialmedia.ui.home.HomeUI
import net.inferno.socialmedia.ui.image.ImageUI
import net.inferno.socialmedia.ui.post.details.PostDetailsUI
import net.inferno.socialmedia.ui.post.form.PostForm
import net.inferno.socialmedia.ui.user.editProfile.EditProfileUI
import net.inferno.socialmedia.ui.user.followers.UserFollowersUI
import net.inferno.socialmedia.ui.user.followes.UserFollowesUI
import net.inferno.socialmedia.ui.user.profile.UserProfileUI

@Composable
fun MainActivityUI(
    mainViewModel: MainViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    preferences: PreferencesDataStore,
) {
//    val start = Routes.LOGIN

    val start = if (preferences.isUserLoggedIn) Routes.HOME else Routes.LOGIN

    var navigated by rememberSaveable { mutableStateOf(false) }
    if (preferences.isUserLoggedIn && !navigated) {
        navigated = true
        LaunchedEffect(Unit) {
            navController.navigate(Routes.profile(null))
        }
    }
//    if (preferences.isUserLoggedIn && !navigated) {
//        navigated = true
//        LaunchedEffect(Unit) {
//            navController.navigate(
//                Routes.post(
//                    Post(
//                        id = "64577377aed9af62b96ec8e1",
//                        content = "",
//                        publisher = DummyUser,
//                    )
//                )
//            )
//        }
//    }
//    if (preferences.isUserLoggedIn && !navigated) {
//        navigated = true
//        LaunchedEffect(Unit) {
//            navController.navigate(
//                Routes.community(
//                    Community(
//                        id = "6463530421e45374a1a4eb82",
//                        name = "abc",
//                        coverImageName = null,
//                    )
//                )
//            )
//        }
//    }

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
            "${Routes.POST}?postId={postId}",
            arguments = listOf(navArgument("postId") {
                type = NavType.StringType
            }),
        ) {
            PostDetailsUI(navController = navController)
        }

        composable(
            "${Routes.ADD_POST}?postId={postId}",
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        ) {
            PostForm(navController = navController)
        }

        composable(
            "${Routes.ADD_COMMENT}?postId={postId}&commentId={commentId}",
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("commentId") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        ) {
            CommentForm(navController = navController)
        }

        composable(
            "${Routes.COMMUNITY}?communityId={communityId}",
            arguments = listOf(
                navArgument("communityId") {
                    type = NavType.StringType
                },
            ),
        ) {
            CommunityUI(navController = navController)
        }

        composable(
            "${Routes.COMMUNITY_MEMBERS}?communityId={communityId}",
            arguments = listOf(
                navArgument("communityId") {
                    type = NavType.StringType
                },
            ),
        ) {
            CommunityMembersUI(navController = navController)
        }

        composable(
            "${Routes.COMMUNITY_REQUESTS}?communityId={communityId}",
            arguments = listOf(
                navArgument("communityId") {
                    type = NavType.StringType
                },
            ),
        ) {
            CommunityRequestsUI(navController = navController)
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