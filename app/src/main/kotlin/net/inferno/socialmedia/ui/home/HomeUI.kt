package net.inferno.socialmedia.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.UserDetails
import net.inferno.socialmedia.ui.main.Routes
import net.inferno.socialmedia.view.UserImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeUI(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val drawerScrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    var showLogoutDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val userState by viewModel.userDataState.collectAsState()

    BackHandler(drawerState.isOpen) {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    val user = userState.data ?: return

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
                        user = user,
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

                    Divider()

                    Text(
                        stringResource(id = R.string.communities),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(
                                vertical = 8.dp,
                                horizontal = 16.dp,
                            )
                    )

                    user.allCommunities.forEach {
                        println(it.coverImageUrl)

                        ListItem(
                            leadingContent = {
                                UserImage(
                                    onClick = {},
                                    modifier = Modifier
                                        .size(48.dp)
                                ) {
                                    if (it.coverImageUrl != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(it.coverImageUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.background(Color.Red)
                                        )
                                    }
                                }
                            },
                            headlineContent = {
                                Text(it.name)
                            },
                            supportingContent = {
                                if (user.isAdmin(it)) {
                                    Text(
                                        stringResource(id = R.string.admin),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }

                                if (user.isManager(it)) {
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
                            UserImage(
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(36.dp)
                            ) {
                                if (user.profileImageUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(user.profileImageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.background(Color.Red)
                                    )
                                }
                            }
                        }
                    },
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            modifier = Modifier
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {},
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
            UserImage(
                onClick = {
                    onClickUserImage()
                },
                modifier = Modifier
                    .size(56.dp)
            ) {
                if (user.profileImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.profileImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                    )
                } else {
                    Box(
                        modifier = Modifier.background(Color.Red)
                    )
                }
            }

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
                    append("Following")
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
                    append("Followers")
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
