package net.inferno.socialmedia.ui.community.members

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.CommunityDetails
import net.inferno.socialmedia.model.DummyCommunity
import net.inferno.socialmedia.model.DummyUser
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.theme.SocialMediaTheme
import net.inferno.socialmedia.ui.main.Routes
import net.inferno.socialmedia.utils.CustomPreview
import net.inferno.socialmedia.view.BackIconButton
import net.inferno.socialmedia.view.ErrorView
import net.inferno.socialmedia.view.LoadingView

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CommunityMembersUI(
    navController: NavController,
    viewModel: CommunityMembersViewModel = hiltViewModel(),
) {
    val lazyScrollState = rememberLazyListState()

    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(null)

    val coroutineScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState is UIState.Refreshing,
        onRefresh = {
            viewModel.getCommunityDetails(true)
        },
    )

    CommunityMembersUI(
        uiState = uiState,
        currentUser = currentUser,
        snackbarHostState = snackbarHostState,
        lazyScrollState = lazyScrollState,
        pullRefreshState = pullRefreshState,
        onBackPressed = {
            navController.popBackStack()
        },
        onRetry = {
            viewModel.getCommunityDetails()
        },
        onPromoteUser = {
            viewModel.promoteUser(it)
        },
        onDemoteUser = {
            viewModel.demoteUser(it)
        },
        onKickUser = {
            viewModel.kickUser(it)
        },
        onItemUserClick = {
            navController.navigate(Routes.profile(it))
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CommunityMembersUI(
    uiState: UIState<CommunityDetails>,
    currentUser: User?,
    onBackPressed: () -> Unit = {},
    lazyScrollState: LazyListState = rememberLazyListState(),
    pullRefreshState: PullRefreshState = rememberPullRefreshState(false, {}),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRetry: () -> Unit = {},
    onPromoteUser: (User) -> Unit = {},
    onDemoteUser: (User) -> Unit = {},
    onKickUser: (User) -> Unit = {},
    onItemUserClick: (User) -> Unit = {},
) {
    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(id = R.string.members),
                            fontWeight = FontWeight.SemiBold,
                        )

                        if (uiState.data != null) {
                            Text(
                                uiState.data.name,
                                fontSize = 12.sp,
                            )
                        }
                    }
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
            val community = uiState.data

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
                    items(community.members) {
                        CommunityMemberItem(
                            it,
                            community = community,
                            onPromote = if (
                                community.isManager(currentUser) &&
                                currentUser != it.user &&
                                !community.isAdmin(it.user)
                            ) { user ->
                                onPromoteUser(user)
                            } else null,
                            onDemote = if (
                                community.isManager(currentUser) &&
                                currentUser != it.user &&
                                community.isAdmin(it.user)
                            ) { user ->
                                onDemoteUser(user)
                            } else null,
                            onKick = if (
                                community.isManager(currentUser) &&
                                currentUser != it.user
                            ) { user ->
                                onKickUser(user)
                            } else null,
                            onClick = if (currentUser.id != it.id) { user ->
                                onItemUserClick(user)
                            } else null,
                        )
                    }
                }

                PullRefreshIndicator(
                    uiState is UIState.Refreshing,
                    pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = paddingValues.calculateTopPadding()),
                )
            }
        } else if (uiState is UIState.Failure) {
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
fun CommunityMembersUIPreview() {
    SocialMediaTheme {
        CommunityMembersUI(
            uiState = UIState.Success(DummyCommunity),
            currentUser = DummyUser,
        )
    }
}
