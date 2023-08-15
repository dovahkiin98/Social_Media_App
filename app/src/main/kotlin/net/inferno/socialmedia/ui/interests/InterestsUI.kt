package net.inferno.socialmedia.ui.interests

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import net.inferno.socialmedia.model.DummyUser
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.UserAction
import net.inferno.socialmedia.model.UserDetails
import net.inferno.socialmedia.theme.SocialMediaTheme
import net.inferno.socialmedia.utils.CustomPreview
import net.inferno.socialmedia.view.BackIconButton
import net.inferno.socialmedia.view.ErrorView
import net.inferno.socialmedia.view.LoadingView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.random.Random

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InterestsUI(
    navController: NavController,
    viewModel: UserInterestsViewModel = hiltViewModel(),
) {
    val lazyScrollState = rememberLazyListState()

    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(null)

    val coroutineScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = {
            viewModel.getUserInterests()
        },
    )

    InterestsUI(
        uiState = uiState,
        currentUser = currentUser,
        snackbarHostState = snackbarHostState,
        lazyScrollState = lazyScrollState,
        pullRefreshState = pullRefreshState,
        onBackPressed = {
            navController.popBackStack()
        },
        onRetry = {
            viewModel.getUserInterests()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun InterestsUI(
    uiState: UIState<List<UserAction>>,
    currentUser: UserDetails?,
    onBackPressed: () -> Unit = {},
    lazyScrollState: LazyListState = rememberLazyListState(),
    pullRefreshState: PullRefreshState = rememberPullRefreshState(false, {}),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRetry: () -> Unit = {},
) {
    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Interests",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    BackIconButton {
                        onBackPressed()
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (uiState.data != null && currentUser != null) {
            val followers = uiState.data

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                LazyColumn(
                    state = lazyScrollState,
                    contentPadding = paddingValues,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(followers, key = { it.id }) {
                        ListItem(
                            headlineContent = {
                                Text(it.action)
                            },
                            trailingContent = {
                                Text(it.actionScore.toStringSigned())
                            },
                            overlineContent = {
                                Text(it.category)
                            },
                            supportingContent = {
                                Text(it.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")))
                            },
                        )
                    }
                }

                PullRefreshIndicator(
                    uiState.isRefreshing,
                    pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = paddingValues.calculateTopPadding()),
                )
            }
        } else if (
            uiState is UIState.Failure) {
            ErrorView(uiState.error!!) {
                onRetry()
            }
        } else {
            LoadingView()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@CustomPreview
@Composable
fun UserFollowesUIPreview() {
    SocialMediaTheme {
        InterestsUI(
            uiState = UIState.Success(buildList {
                repeat(20) {
                    this += UserAction(
                        id = "",
                        score = Random.nextInt(-10, 50),
                        action = "addPost",
                        actionScore = Random.nextInt(-10, 50),
                        category = "Business",
                        date = LocalDateTime.now(),
                    )
                }
            }),
            currentUser = DummyUser,
        )
    }
}

fun Int.toStringSigned() = buildString {
    if (this@toStringSigned > 0) {
        append("6")
    } else {
        append("-")
    }

    append(abs(this@toStringSigned).toString())
}