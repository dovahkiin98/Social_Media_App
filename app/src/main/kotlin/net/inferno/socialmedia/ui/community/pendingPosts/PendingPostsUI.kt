package net.inferno.socialmedia.ui.community.pendingPosts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.ui.main.Routes
import net.inferno.socialmedia.ui.post.PostAction
import net.inferno.socialmedia.ui.post.PostItem
import net.inferno.socialmedia.view.BackIconButton
import net.inferno.socialmedia.view.ErrorView
import net.inferno.socialmedia.view.LoadingView

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
fun PendingPostsUI(
    navController: NavController,
    viewModel: PendingPostsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val currentUserState by viewModel.currentUser.collectAsState(null)
    val communityState by viewModel.communityData.collectAsState()
    val postsState by viewModel.communityPostsState.collectAsState()
    val postOperationState by viewModel.postOperationState.collectAsState()

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    val snackbarHostState = remember { SnackbarHostState() }

    var selectedPost: Post? by remember { mutableStateOf(null) }
    var showPostDiapproveDialog by rememberSaveable { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.getCommunityDetails()
        },
    )

    LaunchedEffect(postsState) {
        if (postsState is UIState.Success) {
            isRefreshing = false
        }
    }

    LaunchedEffect(postOperationState) {
        if (postOperationState is UIState.Failure) {
            val error = (postOperationState as UIState.Failure).error!!

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error.message ?: error.toString(),
                    withDismissAction = true,
                )
            }
        }

        if (postOperationState is UIState.Success) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Post Updated",
                    withDismissAction = true
                )
            }
        }
    }

    val currentUser = currentUserState ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(id = R.string.pending_posts),
                            fontWeight = FontWeight.SemiBold,
                        )

                        if (communityState.data != null) {
                            Text(
                                communityState.data!!.name,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                            )
                        }
                    }
                },
                navigationIcon = {
                    BackIconButton {
                        navController.popBackStack()
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (communityState.data != null) {
            val community = communityState.data!!

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = paddingValues.calculateEndPadding(layoutDirection),
                        bottom = paddingValues.calculateBottomPadding() + 72.dp,
                    ),
                ) {
                    if (postsState.data != null) {
                        val posts = postsState.data!!

                        if(posts.isNotEmpty()) {
                            items(posts) {
                                PostItem(
                                    currentUser = currentUser,
                                    isApproved = false,
                                    post = it.post,
                                    onImageClick = { image ->
                                        navController.navigate(Routes.image(image.imageUrl!!))
                                    },
                                    onOptionsClick = if (
                                        currentUser.isAdmin(community) ||
                                        currentUser.isManager(community) ||
                                        currentUser.id == it.post.publisher.id
                                    ) { post, action ->
                                        selectedPost = post

                                        when (action) {
                                            PostAction.Disapprove -> {
                                                showPostDiapproveDialog = true
                                            }

                                            PostAction.Approve -> {
                                                viewModel.approvePost(post)
                                            }

                                            else -> {}
                                        }
                                    } else null,
                                    onUserClick = { user ->
                                        navController.navigate(Routes.profile(if (currentUser.id == user.id) null else user))
                                    },
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .animateItemPlacement(),
                                )
                            }
                        } else {
                            item {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .padding(36.dp)
                                ) {
                                    Text(
                                        "There are no pending posts",
                                        style = MaterialTheme.typography.titleLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp),
                                    )
                                }
                            }
                        }
                    } else if (postsState.error != null) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(36.dp)
                            ) {
                                ErrorView(
                                    error = postsState.error!!,
                                    onRetry = {
                                        viewModel.getCommunityPosts()
                                    }
                                )
                            }
                        }
                    } else {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(36.dp)
                            ) {
                                LoadingView()
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    isRefreshing,
                    pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = paddingValues.calculateTopPadding()),
                )
            }
        } else if (communityState.error != null) {
            ErrorView(
                error = communityState.error!!,
                onRetry = {
                    viewModel.getCommunityDetails()
                }
            )
        } else {
            LoadingView()
        }
    }

    if (showPostDiapproveDialog) {
        AlertDialog(
            onDismissRequest = {
                showPostDiapproveDialog = false
                selectedPost = null
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.disapprovePost(selectedPost!!)
                    showPostDiapproveDialog = false
                }) {
                    Text(stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPostDiapproveDialog = false
                }) {
                    Text(stringResource(id = R.string.no))
                }
            },
            title = {
                Text(stringResource(id = R.string.disapprove_post))
            },
            text = {
                Text(stringResource(id = R.string.disapprove_post_message))
            }
        )
    }
}