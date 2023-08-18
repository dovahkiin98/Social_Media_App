package net.inferno.socialmedia.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.inferno.socialmedia.BuildConfig
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.UserDetails
import net.inferno.socialmedia.ui.main.Routes
import net.inferno.socialmedia.ui.post.PostAction
import net.inferno.socialmedia.ui.post.PostItem
import net.inferno.socialmedia.view.CommunityAvatar
import net.inferno.socialmedia.view.ErrorView
import net.inferno.socialmedia.view.LoadingView
import net.inferno.socialmedia.view.UserAvatar

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class,
)
@Composable
fun HomeUI(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
//    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val drawerScrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    var selectedPost: Post? by remember { mutableStateOf(null) }
    var showPostDeletionDialog by rememberSaveable { mutableStateOf(false) }

    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    val userState by viewModel.userDataState.collectAsState()
    val postsState by viewModel.postsDataState.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.getNewsFeed()
        },
    )

    LaunchedEffect(postsState) {
        if (postsState is UIState.Success) {
            isRefreshing = false
        }
    }

    BackHandler(drawerState.isOpen) {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    val currentUser = userState.data ?: return

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(drawerScrollState)
                ) {
                    Spacer(Modifier.height(16.dp))

                    DrawerHeader(
                        user = currentUser,
                        onClickUserImage = {
                            navController.navigate(Routes.profile(null))
                        },
                        onClickLogout = {
                            showLogoutDialog = true
                        },
                        onClickFollowees = {
                            navController.navigate(Routes.followings(null))
                        },
                        onClickFollowers = {
                            navController.navigate(Routes.followers(null))
                        },
                    )

                    Spacer(Modifier.height(16.dp))

                    HorizontalDivider()

                    ListItem(
                        headlineContent = {
                            Text(stringResource(id = R.string.conversations))
                        },
                        modifier = Modifier
                            .clickable {
                                navController.navigate(Routes.CONVERSATIONS)
                            }
                    )

                    HorizontalDivider()

                    Text(
                        stringResource(id = R.string.communities),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(
                                vertical = 16.dp,
                                horizontal = 16.dp,
                            )
                    )

                    currentUser.allCommunities.forEach {
                        ListItem(
                            leadingContent = {
                                CommunityAvatar(
                                    community = it,
                                    onClick = {},
                                    modifier = Modifier
                                        .size(48.dp)
                                )
                            },
                            headlineContent = {
                                Text(it.name)
                            },
                            supportingContent = {
                                if (currentUser.isAdmin(it)) {
                                    Text(
                                        stringResource(id = R.string.admin),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }

                                if (currentUser.isManager(it)) {
                                    Text(
                                        stringResource(id = R.string.manager),
                                        color = MaterialTheme.colorScheme.tertiary,
                                    )
                                }
                            },
                            modifier = Modifier
                                .clickable {
                                    navController.navigate(Routes.community(it))
                                }
                        )
                    }

                    TextButton(
                        onClick = {
                            navController.navigate(Routes.NEW_COMMUNITY)
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            stringResource(id = R.string.create_community),
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f))
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(id = R.string.home),
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    scrollBehavior = topAppBarScrollBehavior,
                    navigationIcon = {
                        PlainTooltipBox(
                            tooltip = {
                                Text(stringResource(id = R.string.nav_menu))
                            },
                        ) {
                            UserAvatar(
                                user = currentUser,
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(36.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .then(
                            if (BuildConfig.DEBUG) Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        navController.navigate(Routes.INTERESTS)
                                    },
                                )
                            }
                            else Modifier
                        )
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            modifier = Modifier
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
        ) { paddingValues ->
            if (postsState.data != null) {
                val posts = postsState.data!!

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    LazyColumn(
                        contentPadding = paddingValues,
                    ) {
                        items(posts, key = { it.id }) {
                            PostItem(
                                currentUser = currentUser,
                                post = it,
                                onImageClick = { image ->
                                    navController.navigate(Routes.image(image.imageUrl!!))
                                },
                                onLiked = { post ->
                                    viewModel.likePost(post)
                                },
                                onDisliked = { post ->
                                    viewModel.dislikePost(post)
                                },
                                onOptionsClick = { post, action ->
                                    selectedPost = post

                                    when (action) {
                                        PostAction.Delete -> {
                                            showPostDeletionDialog = true
                                        }

                                        PostAction.Edit -> {
                                            navController.navigate(Routes.addPost(post))
                                        }

                                        else -> {}
                                    }
                                },
                                onClick = { post ->
                                    navController.navigate(Routes.post(post))
                                },
                                onUserClick = { user ->
                                    navController.navigate(Routes.profile(if (user.id == currentUser.id) null else user))
                                },
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .animateItemPlacement(),
                            )
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
            } else if (postsState.error != null) {
                val error = postsState.error!!

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(36.dp)
                        .padding(paddingValues)
                ) {
                    ErrorView(
                        error = error,
                        onRetry = {
                            viewModel.getNewsFeed()
                        }
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(36.dp)
                        .padding(paddingValues)
                ) {
                    LoadingView()
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logout()

                    navController.popBackStack()
                    navController.navigate(Routes.LOGIN)
                }) {
                    Text(stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                }) {
                    Text(stringResource(id = R.string.no))
                }
            },
            title = {
                Text(stringResource(id = R.string.logout))
            },
            text = {
                Text(stringResource(id = R.string.logout_message))
            }
        )
    }

    if (showPostDeletionDialog) {
        AlertDialog(
            onDismissRequest = {
                showPostDeletionDialog = false
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePost(selectedPost!!)
                    showPostDeletionDialog = false
                }) {
                    Text(stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPostDeletionDialog = false
                }) {
                    Text(stringResource(id = R.string.no))
                }
            },
            title = {
                Text(stringResource(id = R.string.delete_post))
            },
            text = {
                Text(stringResource(id = R.string.delete_post_message))
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DrawerHeader(
    user: UserDetails,
    onClickUserImage: () -> Unit = {},
    onClickLogout: () -> Unit = {},
    onClickFollowees: () -> Unit = {},
    onClickFollowers: () -> Unit = {},
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row {
            UserAvatar(
                user = user,
                onClick = {
                    onClickUserImage()
                },
                modifier = Modifier
                    .size(56.dp)
            )
            Box(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    onClickLogout()
                },
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_logout),
                    contentDescription = null,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "${user.firstName} ${user.lastName}",
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier
        ) {
            Text(
                buildAnnotatedString {
                    pushStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    )
                    append(user.followes.size.toString())
                    pop()

                    append(" ")
                    append(stringResource(id = R.string.following))
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onClickFollowees()
                    }
                    .padding(vertical = 8.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                buildAnnotatedString {
                    pushStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    )
                    append(user.followers.size.toString())
                    pop()

                    append(" ")
                    append(stringResource(id = R.string.followers))
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onClickFollowers()
                    }
                    .padding(vertical = 8.dp)
            )
        }
    }
}
