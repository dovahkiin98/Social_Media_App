package net.inferno.socialmedia.ui.community.details

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageOptions
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.ui.cropImage.CropImageActivity
import net.inferno.socialmedia.ui.main.Routes
import net.inferno.socialmedia.ui.post.form.PostResult
import net.inferno.socialmedia.utils.CropImageContract
import net.inferno.socialmedia.utils.getFilePathFromUri
import net.inferno.socialmedia.view.BackIconButton
import net.inferno.socialmedia.view.CustomModalBottomSheet
import net.inferno.socialmedia.view.ErrorView
import net.inferno.socialmedia.view.LoadingView
import net.inferno.socialmedia.view.PostAction
import net.inferno.socialmedia.view.PostItem
import java.io.File
import java.time.format.DateTimeFormatter

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
fun CommunityUI(
    navController: NavController,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current

    val cropImageLauncher = rememberLauncherForActivityResult(
        CropImageContract(),
    ) { result ->
        if (result != null) {
            if (result.isSuccessful) {
                val uri = result.uri
                val croppedImage = File(getFilePathFromUri(context, uri))

                viewModel.uploadCoverImage(croppedImage)
            } else {
                val exception = result.error
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) {
        if (it != null) {
            val cropImageOptions = CropImageOptions()

            cropImageOptions.aspectRatioX = 16
            cropImageOptions.aspectRatioY = 9

            val cropImageIntent = Intent(context, CropImageActivity::class.java).apply {
                val bundle = Bundle()
                bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, it)
                bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, cropImageOptions)
                putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, bundle)
            }

            cropImageLauncher.launch(cropImageIntent)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val currentUser by viewModel.currentUser.collectAsState(null)
    val communityState by viewModel.communityData.collectAsState()
    val postsState by viewModel.communityPostsState.collectAsState()
    val coverImageUpload by viewModel.coverImageUploadState.collectAsState()
    val postDeletionState by viewModel.postDeletionState.collectAsState()

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    val snackbarHostState = remember { SnackbarHostState() }

    val coverImageSheetState = rememberModalBottomSheetState()
    var showCoverImageSheet by rememberSaveable { mutableStateOf(false) }

    var selectedPost: Post? by remember { mutableStateOf(null) }
    var showPostDeletionDialog by rememberSaveable { mutableStateOf(false) }

    val showUploadingDialog by remember {
        derivedStateOf {
            coverImageUpload is UIState.Loading
        }
    }

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

    LaunchedEffect(coverImageUpload) {
        if (coverImageUpload is UIState.Success) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Cover Image Uploaded!",
                    withDismissAction = true,
                )
            }
        }

        if (coverImageUpload is UIState.Failure) {
            val error = (coverImageUpload as UIState.Failure).error!!

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Cover Image Upload failed : ${error.message}",
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
                    message = "Post Deleted",
                    withDismissAction = true
                )
            }
        }
    }

    val onBackPressed = {
        println(lazyListState.layoutInfo)
        if (lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0) {
            navController.popBackStack()
        } else {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(0)
            }
        }

        true
    }

    BackHandler {
        onBackPressed()
    }

    val backStackEntry by navController.currentBackStackEntryAsState()

    val postResult = remember(backStackEntry) {
        backStackEntry?.savedStateHandle?.getStateFlow<PostResult?>("postResult", null)
    }?.collectAsState()

    LaunchedEffect(postResult) {
        if (postResult != null) {
            if (postResult.value == PostResult.Added) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Post Added",
                        withDismissAction = true
                    )
                }
            }

            if (postResult.value == PostResult.Updated) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Post Updated",
                        withDismissAction = true
                    )
                }
            }

            if (postResult.value == PostResult.Deleted) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Post Deleted",
                        withDismissAction = true
                    )
                }
            }

            viewModel.getCommunityPosts()

            backStackEntry?.savedStateHandle?.set("postResult", null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (communityState.data != null) {
                        Text(communityState.data!!.name)
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
            SnackbarHost(snackbarHostState)
        },
        floatingActionButton = {
            if (communityState.data != null && currentUser != null) {
                if (communityState.data!!.isMember(currentUser!!)) {
                    ExtendedFloatingActionButton(
                        text = {
                            Text(stringResource(id = R.string.new_post))
                        },
                        icon = {
                            Icon(Icons.Default.Edit, null)
                        },
                        onClick = {
                            navController.navigate(Routes.addPost(null))
                        },
                    )
                }
            }
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (communityState.data != null && currentUser != null) {
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
                    item {
                        Column {
                            BoxWithConstraints {
                                val imageHeight = remember(maxWidth) { maxWidth * 9 / 16 }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(imageHeight)
                                        .clickable {
                                            showCoverImageSheet = true
                                            coroutineScope.launch {
                                                coverImageSheetState.show()
                                            }
                                        }
                                ) {
                                    if (community.coverImageUrl != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(community.coverImageUrl!!)
                                                .crossfade(true)
                                                .placeholder(ColorDrawable(Color.Blue.toArgb()))
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier
                                                .fillMaxSize()
                                        )
                                    }
                                }
                            }

                            Text(
                                community.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(
                                        start = 16.dp,
                                        top = 16.dp,
                                    )
                            )

                            Text(
                                buildString {
                                    append(stringResource(id = R.string.created))
                                    append(" ")
                                    append(
                                        community.createdAt.format(
                                            DateTimeFormatter.ofPattern(
                                                "dd MMM yyyy"
                                            )
                                        )
                                    )
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(
                                        start = 16.dp,
                                        top = 4.dp,
                                        bottom = 4.dp,
                                    )
                            )

                            FlowRow(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    buildAnnotatedString {
                                        pushStyle(
                                            SpanStyle(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        )
                                        append(community.members.size.toString())
                                        pop()

                                        append(" ")
                                        append("Members")
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .then(
                                            if (
                                                community.isAdmin(currentUser!!) ||
                                                community.isManager(currentUser!!)
                                            ) Modifier.clickable {
                                                navController.navigate(
                                                    Routes.communityMembers(
                                                        community
                                                    )
                                                )
                                            } else Modifier
                                        )
                                        .padding(vertical = 8.dp)
                                )

                                if (
                                    community.isAdmin(currentUser!!) ||
                                    community.isManager(currentUser!!)
                                ) {
                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        buildAnnotatedString {
                                            pushStyle(
                                                SpanStyle(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                            )
                                            append(community.pendingMembers.size.toString())
                                            pop()

                                            append(" ")
                                            append("Pending Requests")
                                        },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                navController.navigate(
                                                    Routes.communityRequests(
                                                        community
                                                    )
                                                )
                                            }
                                            .padding(vertical = 8.dp)
                                    )
                                }

                                if (community.isManager(currentUser!!)) {
                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        buildAnnotatedString {
                                            pushStyle(
                                                SpanStyle(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                            )
                                            append(community.admins.size.toString())
                                            pop()

                                            append(" ")
                                            append("Admins")
                                        },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .padding(vertical = 8.dp)
                                    )
                                }
                            }

                            if (community.isMember(currentUser!!)) {
                                OutlinedButton(
                                    onClick = {
//                                        viewModel.toggleFollow(userData)
//                                        followingState = !followingState
                                    },
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp,
                                    ),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(stringResource(id = R.string.leave))
                                }
                            } else {
                                ElevatedButton(
                                    onClick = {
//                                        viewModel.toggleFollow(userData)
//                                        followingState = !followingState
                                    },
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp,
                                    ),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(stringResource(id = R.string.request_join))
                                }
                            }
                        }
                    }

                    if (postsState.data != null) {
                        items(postsState.data!!) {
                            PostItem(
                                currentUserId = currentUser!!.id,
                                post = it.post,
                                onImageClick = { image ->
                                    navController.navigate(Routes.image(image.imageUrl!!))
                                },
                                onLiked = { post ->
//                                    viewModel.likePost(post)
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
                                    }
                                },
                                onClick = { post ->
                                    navController.navigate(Routes.post(post))
                                },
                                onUserClick = { user ->
                                    navController.navigate(Routes.profile(if (currentUser!!.id == user.id) null else user))
                                },
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .animateItemPlacement(),
                            )
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

    if (showCoverImageSheet) {
        CustomModalBottomSheet(
            onDismissRequest = {
                showCoverImageSheet = false
            },
            sheetState = coverImageSheetState,
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(id = R.drawable.ic_image),
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(id = R.string.view_community_cover))
                },
                modifier = Modifier
                    .clickable {
                        coroutineScope.launch {
                            coverImageSheetState.hide()
                            showCoverImageSheet = false
                        }

                        navController.navigate(Routes.image(communityState.data!!.coverImageUrl!!))
                    }
            )
            if (currentUser!!.isManager(communityState.data!!)) {
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(id = R.drawable.ic_upload),
                            contentDescription = null,
                        )
                    },
                    headlineContent = {
                        Text(stringResource(id = R.string.upload_photo))
                    },
                    modifier = Modifier
                        .clickable {
                            coroutineScope.launch {
                                coverImageSheetState.hide()
                                showCoverImageSheet = false
                            }

                            pickImageLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                )
            }
        }
    }

    if (showUploadingDialog) {
        AlertDialog(onDismissRequest = {}) {
            Row {
                CircularProgressIndicator()

                Spacer(Modifier.width(24.dp))

                Text(
                    stringResource(id = R.string.uploading_image) + "...",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (showPostDeletionDialog) {
        AlertDialog(
            onDismissRequest = {
                showPostDeletionDialog = false
                selectedPost = null
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