package net.inferno.socialmedia.ui.post.details

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.ui.comment.form.CommentResult
import net.inferno.socialmedia.ui.main.Routes
import net.inferno.socialmedia.ui.post.form.PostResult
import net.inferno.socialmedia.utils.toReadableText
import net.inferno.socialmedia.view.BackIconButton
import net.inferno.socialmedia.view.CustomModalBottomSheet
import net.inferno.socialmedia.view.ErrorView
import net.inferno.socialmedia.view.LoadingView
import net.inferno.socialmedia.view.MDDocument
import net.inferno.socialmedia.view.UserAvatar

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class, ExperimentalLayoutApi::class,
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

    val postDeletionState by viewModel.postDeletionState.collectAsState()
    val commentDeletionState by viewModel.commentDeletionState.collectAsState()

    var contentExpanded by remember { mutableStateOf(false) }

    val postSheetState = rememberModalBottomSheetState()
    var showPostSheet by rememberSaveable { mutableStateOf(false) }
    var showPostDeletionDialog by rememberSaveable { mutableStateOf(false) }
    var showCommentDeletionDialog by rememberSaveable { mutableStateOf(false) }

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

    LaunchedEffect(commentDeletionState) {
        if (commentDeletionState is UIState.Failure) {
            val error = (commentDeletionState as UIState.Failure).error!!

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error.message ?: error.toString(),
                    withDismissAction = true,
                )
            }
        }

        if (commentDeletionState is UIState.Success) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Comment Deleted",
                    withDismissAction = true,
                )
            }
        }
    }

    LaunchedEffect(postDeletionState) {
        if (postDeletionState is UIState.Failure) {
            val error = (postDeletionState as UIState.Failure).error!!

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error.message ?: error.toString(),
                    withDismissAction = true,
                )
            }
        }

        if (postDeletionState is UIState.Success) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Post Deleted",
                    withDismissAction = true,
                )
            }

            val previousHandle = navController.previousBackStackEntry?.savedStateHandle

            previousHandle?.set("postResult", PostResult.Deleted)

            navController.popBackStack()
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()

    val commentResult = remember(backStackEntry) {
        backStackEntry?.savedStateHandle?.getStateFlow<CommentResult?>("commentResult", null)
    }?.collectAsState()

    LaunchedEffect(commentResult) {
        if (commentResult?.value != null) {
            if (commentResult.value == CommentResult.Added) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Comment Added",
                        withDismissAction = true
                    )
                }
            }

            if (commentResult.value == CommentResult.Updated) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Comment Updated",
                        withDismissAction = true
                    )
                }
            }

            viewModel.getPostComments()

            backStackEntry?.savedStateHandle?.set("commentResult", null)
        }
    }

    val postResult = remember(backStackEntry) {
        backStackEntry?.savedStateHandle?.getStateFlow<PostResult?>("postResult", null)
    }?.collectAsState()

    LaunchedEffect(postResult) {
        if (postResult?.value != null) {
            if (postResult.value == PostResult.Updated) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Post Updated",
                        withDismissAction = true
                    )
                }
            }

            viewModel.getPostDetails()

            backStackEntry?.savedStateHandle?.set("postResult", null)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
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

            val likes by remember(postState) {
                derivedStateOf {
                    mutableStateListOf(*post.likes.toTypedArray())
                }
            }

            val dislikes by remember(postState) {
                derivedStateOf {
                    mutableStateListOf(*post.dislikes.toTypedArray())
                }
            }

            val postLiked by remember(postState) {
                derivedStateOf { likes.contains(currentUserId) }
            }
            val postDisliked by remember(postState) {
                derivedStateOf { dislikes.contains(currentUserId) }
            }

            val likesScore by remember(postState) {
                derivedStateOf { likes.size - dislikes.size }
            }
            val isBadPost by remember(postState) {
                derivedStateOf {
                    currentUserId != post.publisher.id && (likesScore <= -10 || post.hasBadComments)
                }
            }

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
                        if (isBadPost) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Red)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        stringResource(id = R.string.post_has_bad_comments),
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .background(
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
                                        UserAvatar(
                                            user = post.publisher,
                                            onClick = {
                                                navController.navigate(Routes.profile(post.publisher))
                                            },
                                            modifier = Modifier.size(48.dp)
                                        )

                                        Spacer(Modifier.width(8.dp))

                                        Column(
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            if (post.community != null) {
                                                FlowRow(
                                                    verticalArrangement = Arrangement.Center,
                                                ) {
                                                    Text(
                                                        post.community.community!!.name,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier
                                                            .clickable {
                                                                navController.navigate(
                                                                    Routes.community(post.community.community)
                                                                )
                                                            }
                                                    )

                                                    Text(" ")

                                                    Text(
                                                        buildAnnotatedString {
                                                            append("by")
                                                            append(" ")

                                                            append("${post.publisher.firstName} ${post.publisher.lastName}")
                                                        },
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 14.sp,
                                                        modifier = Modifier
                                                            .align(Alignment.CenterVertically)
                                                            .clickable {
                                                                navController.navigate(
                                                                    Routes.profile(
                                                                        if (currentUser!!.id == post.publisher.id) null
                                                                        else post.publisher
                                                                    )
                                                                )
                                                            }
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    "${post.publisher.firstName} ${post.publisher.lastName}",
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
                                            }

                                            if (post.createdAt != null) {
                                                Text(
                                                    post.createdAt.toReadableText(),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 14.sp,
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
                                        MDDocument(
                                            post.content,
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

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Row {
                                        IconButton(
                                            onClick = {
                                                if (likes.contains(currentUserId)) {
                                                    likes.remove(currentUserId)
                                                } else {
                                                    if (dislikes.contains(currentUserId)) {
                                                        dislikes.remove(currentUserId)
                                                    }
                                                    likes.add(currentUserId)
                                                }

                                                viewModel.likePost()
                                            },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                contentColor =
                                                if (postLiked) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                        ) {
                                            Icon(
                                                painterResource(
                                                    if (postLiked) R.drawable.ic_thumbs_up
                                                    else R.drawable.ic_thumbs_up_off
                                                ),
                                                contentDescription = null
                                            )
                                        }

                                        Text(
                                            likesScore.toString(),
                                            color = when {
                                                likesScore > 0 -> MaterialTheme.colorScheme.primary
                                                likesScore < 0 -> MaterialTheme.colorScheme.secondary
                                                else -> Color.Unspecified
                                            },
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .sizeIn(minWidth = 36.dp)
                                                .align(Alignment.CenterVertically)
                                        )

                                        IconButton(
                                            onClick = {
                                                if (dislikes.contains(currentUserId)) {
                                                    dislikes.remove(currentUserId)
                                                } else {
                                                    if (likes.contains(currentUserId)) {
                                                        likes.remove(currentUserId)
                                                    }
                                                    dislikes.add(currentUserId)
                                                }

                                                viewModel.dislikePost()
                                            },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                contentColor =
                                                if (postDisliked) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                        ) {
                                            Icon(
                                                painterResource(
                                                    if (postDisliked) R.drawable.ic_thumb_down
                                                    else R.drawable.ic_thumb_down_off
                                                ),
                                                contentDescription = null
                                            )
                                        }

                                        Spacer(Modifier.width(24.dp))

                                        TextButton(
                                            onClick = {
                                                navController.navigate(Routes.addComment(post))
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                        ) {
                                            Icon(
                                                painterResource(id = R.drawable.ic_comment),
                                                contentDescription = null
                                            )

                                            Spacer(Modifier.width(8.dp))

                                            Text(stringResource(id = R.string.comment) + if (post.comments.isEmpty()) "" else " (${post.comments.size})")
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                        }

                        if (commentsState.data != null) {
                            items(commentsState.data!!, key = { it.id }) { item ->
                                CommentItem(
                                    comment = item,
                                    currentUserId = currentUserId,
                                    opId = post.publisher.id,
                                    onUserClick = { user ->
                                        navController.navigate(Routes.profile(if (currentUser!!.id == user.id) null else user))
                                    },
                                    onLiked = { comment ->
                                        viewModel.likeComment(comment)
                                    },
                                    onDisliked = { comment ->
                                        viewModel.dislikeComment(comment)
                                    },
                                    onReply = { comment ->
                                        navController.navigate(Routes.addComment(comment = comment))
                                    },
                                    onOptionsClick = { comment, action ->
                                        when (action) {
                                            CommentAction.Edit -> {
                                                navController.navigate(Routes.editComment(comment = comment))
                                            }

                                            CommentAction.Delete -> {
                                                showCommentDeletionDialog = true
                                                viewModel.selectedComment = comment
                                            }
                                        }
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
                                        error = commentsState.error!!,
                                        onRetry = {
                                            viewModel.getPostComments()
                                        },
                                        modifier = Modifier.padding(16.dp)
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
                            MaterialTheme.colorScheme.surfaceVariant,
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
                                        navController.navigate(Routes.addComment(post))
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
        CustomModalBottomSheet(
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
                    showPostSheet = false
                    navController.navigate(Routes.addPost(postState.data!!))

                    coroutineScope.launch {
                        postSheetState.hide()
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
                    showPostSheet = false
                    showPostDeletionDialog = true

                    coroutineScope.launch {
                        postSheetState.hide()
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

    if (showCommentDeletionDialog) {
        AlertDialog(
            onDismissRequest = {
                showCommentDeletionDialog = false
                viewModel.selectedComment = null
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteComment(viewModel.selectedComment!!)
                    showCommentDeletionDialog = false
                }) {
                    Text(stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCommentDeletionDialog = false
                }) {
                    Text(stringResource(id = R.string.no))
                }
            },
            title = {
                Text(stringResource(id = R.string.delete_comment))
            },
            text = {
                Text(stringResource(id = R.string.delete_comment_message))
            }
        )
    }
}