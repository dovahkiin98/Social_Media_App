package net.inferno.socialmedia.ui.profile

import android.graphics.drawable.ColorDrawable
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.ui.main.Routes
import net.inferno.socialmedia.utils.CropImageContract
import net.inferno.socialmedia.utils.getFilePathFromUri
import net.inferno.socialmedia.view.BackIconButton
import net.inferno.socialmedia.view.ErrorView
import net.inferno.socialmedia.view.LoadingView
import net.inferno.socialmedia.view.PostItem
import net.inferno.socialmedia.view.UserImage
import java.io.File

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class,
)
@Composable
fun UserProfileUI(
    navController: NavController,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val cropImageLauncher = rememberLauncherForActivityResult(
        CropImageContract(),
    ) { result ->
        if (result != null) {
            if (result.isSuccessful) {
                val uri = result.uri
                val croppedImage = File(getFilePathFromUri(context, uri))

                if (viewModel.imageType == "cover") {
                    viewModel.uploadCoverImage(croppedImage)
                } else {
                    viewModel.uploadProfileImage(croppedImage)
                }
            } else {
                val exception = result.error
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) {
        if (it != null) {
            val cropImageIntent = CropImage.activity(it)

            if (viewModel.imageType == "cover") {
                cropImageIntent.setAspectRatio(16, 9)
            } else {
                cropImageIntent.setAspectRatio(1, 1)
            }

            cropImageLauncher.launch(cropImageIntent)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val currentUser by viewModel.currentUser.collectAsState(null)
    val user by viewModel.userDataState.collectAsState()
    val posts by viewModel.userPostsState.collectAsState()
    val coverImageUpload by viewModel.coverImageUploadState.collectAsState()
    val profileImageUpload by viewModel.profileImageUploadState.collectAsState()

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    val snackbarHostState = remember { SnackbarHostState() }

    val coverImageSheetState = rememberModalBottomSheetState()
    var showCoverImageSheet by rememberSaveable { mutableStateOf(false) }

    val profileImageSheetState = rememberModalBottomSheetState()
    var showProfileImageSheet by rememberSaveable { mutableStateOf(false) }

    var selectedPost: Post? by remember { mutableStateOf(null) }
    val postSheetState = rememberModalBottomSheetState()
    var showPostSheet by rememberSaveable { mutableStateOf(false) }
    var showPostDeletionDialog by rememberSaveable { mutableStateOf(false) }

    val showUploadingDialog by remember {
        derivedStateOf {
            coverImageUpload is UIState.Loading || profileImageUpload is UIState.Loading
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.getUserDetails()
        },
    )

    LaunchedEffect(posts) {
        if (posts is UIState.Success) {
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

    LaunchedEffect(profileImageUpload) {
        if (profileImageUpload is UIState.Success) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Profile Image Uploaded!",
                    withDismissAction = true,
                )
            }
        }

        if (profileImageUpload is UIState.Failure) {
            val error = (profileImageUpload as UIState.Failure).error!!

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Profile Image Upload failed : ${error.message}",
                    withDismissAction = true,
                )
            }
        }
    }

    val onBackPressed = {
        if (lazyListState.layoutInfo.viewportStartOffset == 0) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (user.data != null) {
                        Text("${user.data!!.firstName} ${user.data!!.lastName}")
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
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (user.data != null && currentUser != null) {
            val userData = user.data!!
            var followingState = rememberSaveable(userData) {
                currentUser!!.followes.contains(viewModel.userId)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = paddingValues,

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
                                    if (userData.coverImageUrl != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(userData.coverImageUrl!!)
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

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .padding(
                                            top = imageHeight - 136.dp + 36.dp,
                                            start = 16.dp,
                                        )
                                        .size(136.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface),
                                ) {
                                    UserImage(
                                        onClick = {
                                            showProfileImageSheet = true
                                        },
                                        modifier = Modifier
                                            .size(128.dp)
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(userData.profileImageUrl)
                                                .crossfade(true)
                                                .placeholder(ColorDrawable(Color.Red.toArgb()))
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.FillWidth,
                                        )
                                    }
                                }
                            }

                            Text(
                                "${userData.firstName} ${userData.lastName}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(start = 16.dp)
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
                                        append(userData.followes.size.toString())
                                        pop()

                                        append(" ")
                                        append("Following")
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            navController.navigate(Routes.followings(userData))
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
                                        append(userData.followers.size.toString())
                                        pop()

                                        append(" ")
                                        append("Followers")
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            navController.navigate(Routes.followers(userData))
                                        }
                                        .padding(vertical = 8.dp)
                                )
                            }

                            if (viewModel.userId != null) {
                                if (followingState) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.toggleFollow(userData)
                                            followingState = !followingState
                                        },
                                        contentPadding = PaddingValues(
                                            horizontal = 12.dp,
                                        ),
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(stringResource(id = R.string.unfollow))
                                    }
                                } else {
                                    ElevatedButton(
                                        onClick = {
                                            viewModel.toggleFollow(userData)
                                            followingState = !followingState
                                        },
                                        contentPadding = PaddingValues(
                                            horizontal = 12.dp,
                                        ),
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(stringResource(id = R.string.follow))
                                    }
                                }
                            } else {
                                ElevatedButton(
                                    onClick = {
                                        navController.navigate(Routes.EDIT_PROFILE)
                                    },
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp,
                                    ),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(stringResource(id = R.string.edit_profile))
                                }
                            }
                        }
                    }

                    if (posts.data != null) {
                        items(posts.data!!) {
                            PostItem(
                                currentUserId = currentUser!!.id,
                                post = it,
                                onImageClick = { image ->
                                    navController.navigate(Routes.image(image.imageUrl!!))
                                },
                                onPostLiked = { post ->
                                    viewModel.likePost(post)
                                },
                                onOptionsClicked = { post ->
                                    selectedPost = post
                                    showPostSheet = true
                                },
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .animateItemPlacement(),
                            )
                        }
                    } else if (posts.error != null) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(36.dp)
                            ) {
                                ErrorView(
                                    error = posts.error!!,
                                    onRetry = {
                                        viewModel.getUserPosts()
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
        } else if (user.error != null) {
            ErrorView(
                error = posts.error!!,
                onRetry = {
                    viewModel.getUserDetails()
                }
            )
        } else {
            LoadingView()
        }
    }


    if (showProfileImageSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showProfileImageSheet = false
            },
            sheetState = profileImageSheetState,
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(id = R.drawable.ic_image),
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(id = R.string.view_profile_image))
                },
                modifier = Modifier
                    .clickable {
                        coroutineScope.launch {
                            profileImageSheetState.hide()
                            showProfileImageSheet = false
                        }

                        navController.navigate(Routes.image(user.data!!.profileImageUrl!!))
                    }
            )
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
                            profileImageSheetState.hide()
                            showProfileImageSheet = false
                        }

                        viewModel.imageType = "profile"
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            )
        }
    }

    if (showCoverImageSheet) {
        ModalBottomSheet(
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
                    Text(stringResource(id = R.string.view_profile_cover))
                },
                modifier = Modifier
                    .clickable {
                        coroutineScope.launch {
                            coverImageSheetState.hide()
                            showCoverImageSheet = false
                        }

                        navController.navigate(Routes.image(user.data!!.coverImageUrl!!))
                    }
            )
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

                        viewModel.imageType = "cover"
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            )
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
                modifier = Modifier
                    .clickable {
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
                modifier = Modifier
                    .clickable {
                        coroutineScope.launch {
                            postSheetState.hide()
                            showPostDeletionDialog = true
                            showPostSheet = false
                        }
                    }
            )
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

    println(selectedPost)
}