package net.inferno.socialmedia.ui.post

import android.graphics.drawable.ColorDrawable
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.ui.main.Routes
import net.inferno.socialmedia.utils.toReadableText
import net.inferno.socialmedia.view.BackIconButton
import net.inferno.socialmedia.view.ErrorView
import net.inferno.socialmedia.view.LoadingView
import net.inferno.socialmedia.view.UserImage

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class,
)
@Composable
fun PostDetailsUI(
    navController: NavController,
    viewModel: PostDetailsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val postState by viewModel.postDataState.collectAsState()
    val commentsState by viewModel.postCommentsState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(null)

    var contentExpanded by remember { mutableStateOf(false) }

    val postSheetState = rememberModalBottomSheetState()
    var showPostSheet by rememberSaveable { mutableStateOf(false) }
    var showPostDeletionDialog by rememberSaveable { mutableStateOf(false) }

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    val snackbarHostState = remember { SnackbarHostState() }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.getPostDetails()
        },
    )

    LaunchedEffect(commentsState) {
        if (commentsState is UIState.Success) {
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackIconButton {
                        navController.popBackStack()
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures {
                            coroutineScope.launch {
                                lazyListState.animateScrollToItem(0)
                            }
                        }
                    }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (postState.data != null && currentUser != null) {
            val post = postState.data!!
            val currentUserId = currentUser!!.id

            val likes = remember { mutableStateListOf(*post.likes.toTypedArray()) }
            val postLiked = likes.contains(currentUserId)

            Column {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .pullRefresh(pullRefreshState)
                ) {
                    LazyColumn(
                        state = lazyListState,
                        contentPadding = PaddingValues(
                            start = paddingValues.calculateStartPadding(layoutDirection),
                            end = paddingValues.calculateEndPadding(layoutDirection),
                            top = paddingValues.calculateTopPadding(),
                        ),
                    ) {
                        item {
                            Box(
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                                )
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(
                                            start = 8.dp,
                                            top = 8.dp,
                                            bottom = 8.dp,
                                        ),
                                    ) {
                                        UserImage(
                                            onClick = {

                                            }, modifier = Modifier.size(48.dp)
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(post.publisher.profileImageUrl)
                                                    .crossfade(true)
                                                    .placeholder(ColorDrawable(Color.Red.toArgb()))
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.FillWidth,
                                            )
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        Column(
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Text("${post.publisher.firstName} ${post.publisher.lastName}",
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clickable {
                                                        navController.navigate(
                                                            Routes.profile(
                                                                if (currentUser!!.id == post.publisher.id) null
                                                                else post.publisher
                                                            )
                                                        )
                                                    }
                                            )

                                            if (post.createdAt != null) {
                                                Text(
                                                    post.createdAt.toReadableText(),
                                                )
                                            }
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        if (currentUserId == post.publisher.id) {
                                            IconButton(onClick = {
                                                showPostSheet = true
                                            }) {
                                                Icon(
                                                    Icons.Default.MoreVert,
                                                    contentDescription = null,
                                                )
                                            }
                                        }
                                    }

                                    if (post.content.isNotBlank()) {
                                        Text(
                                            post.content,
                                            maxLines = if (contentExpanded) Int.MAX_VALUE else 3,
                                            overflow = if (contentExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .animateContentSize()
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        contentExpanded = !contentExpanded
                                                    },
                                                    onLongClick = {
                                                        clipboardManager.setText(
                                                            AnnotatedString(
                                                                post.content
                                                            )
                                                        )
                                                        Toast
                                                            .makeText(
                                                                context,
                                                                R.string.post_copied,
                                                                Toast.LENGTH_SHORT
                                                            )
                                                            .show()
                                                    }
                                                )
                                                .padding(8.dp)
                                        )
                                    }

                                    if (post.files.isNotEmpty()) {
                                        HorizontalPager(
                                            state = rememberPagerState { post.files.size },
                                            modifier = Modifier
                                                .requiredHeightIn(max = 500.dp)
                                                .fillMaxWidth()
                                        ) {
                                            val image = post.files[it]

                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(image.imageUrl!!)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.FillWidth,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        navController.navigate(Routes.image(image.imageUrl!!))
                                                    }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.animateContentSize()
                                    ) {
                                        if (post.likes.isNotEmpty()) {
                                            Text(
                                                pluralStringResource(
                                                    id = R.plurals.likes,
                                                    count = post.likes.size,
                                                    post.likes.size,
                                                ),
                                                style = MaterialTheme.typography.bodySmall,
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(8.dp)
                                            )
                                        }

                                        if (post.comments.isNotEmpty()) {
                                            Text(
                                                pluralStringResource(
                                                    id = R.plurals.comments,
                                                    count = post.comments.size,
                                                    post.comments.size
                                                ),
                                                style = MaterialTheme.typography.bodySmall,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(8.dp)
                                            )
                                        }
                                    }

                                    Divider(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Row {
                                        TextButton(
                                            onClick = {
                                                if (likes.contains(currentUserId)) {
                                                    likes.remove(currentUserId)
                                                } else {
                                                    likes.add(currentUserId)
                                                }

                                                viewModel.likePost()
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = if (postLiked) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                painterResource(
                                                    if (postLiked) R.drawable.ic_thumbs_up
                                                    else R.drawable.ic_thumbs_up_off
                                                ),
                                                contentDescription = null,
                                            )

                                            Spacer(Modifier.width(8.dp))

                                            Text(stringResource(id = R.string.like))
                                        }

                                        TextButton(
                                            onClick = {

                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                painterResource(id = R.drawable.ic_comment),
                                                contentDescription = null
                                            )

                                            Spacer(Modifier.width(8.dp))

                                            Text(stringResource(id = R.string.comment))
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                        }

                        if (commentsState.data != null) {
                            items(commentsState.data!!, key = { it.id }) { comment ->
                                CommentItem(
                                    comment = comment,
                                    currentUserId = currentUserId,
                                    onUserClick = { user ->
                                        navController.navigate(Routes.profile(if (currentUser!!.id == user.id) null else user))
                                    },
                                    onLiked = {
                                        viewModel.likeComment(it)
                                    },
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                                        )
                                )
                            }
                        } else {
                            item {
                                if (commentsState.error != null) {
                                    ErrorView(
                                        error = postState.error!!, onRetry = {
                                            viewModel.getPostComments()
                                        }, modifier = Modifier.padding(16.dp)
                                    )
                                } else {
                                    LoadingView(
                                        modifier = Modifier.padding(16.dp)
                                    )
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        )
                        .padding(
                            bottom = paddingValues.calculateBottomPadding(),
                        )
                ) {
                    Row {
                        Text(
                            stringResource(id = R.string.add_comment),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures {

                                    }
                                }
                                .padding(16.dp)
                                .weight(1f),
                        )

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (lazyListState.firstVisibleItemIndex + 1 < lazyListState.layoutInfo.totalItemsCount) {
                                        lazyListState.animateScrollToItem(lazyListState.firstVisibleItemIndex + 1)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        } else if (postState.error != null) {
            ErrorView(error = postState.error!!, onRetry = {
                viewModel.getPostDetails()
            })
        } else {
            LoadingView()
        }
    }

    if (showPostSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showPostSheet = false
            },
            sheetState = postSheetState,
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(id = R.drawable.ic_edit),
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(id = R.string.edit_post))
                },
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        postSheetState.hide()
                        showPostSheet = false
                    }
                }

            )
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(id = R.drawable.ic_delete),
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(id = R.string.delete_post))
                },
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        postSheetState.hide()
                        showPostSheet = false
                    }
                }
            )
        }
    }

    if (showPostDeletionDialog) {
        AlertDialog(
            onDismissRequest = {
                showPostDeletionDialog = false
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePost()
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