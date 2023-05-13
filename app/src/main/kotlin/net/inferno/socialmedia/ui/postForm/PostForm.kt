package net.inferno.socialmedia.ui.postForm

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.view.ErrorView
import net.inferno.socialmedia.view.LoadingView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
)
@Composable
fun PostForm(
    navController: NavController,
    viewModel: PostFormViewModel = hiltViewModel(),
) {
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    val snackbarHostState = remember { SnackbarHostState() }

    val scrollState = rememberScrollState()

//    val currentUser by viewModel.currentUser.collectAsState(null)
    val postState by viewModel.postDataState.collectAsState()

    val postAddState by viewModel.postAddState.collectAsState()
    var image by remember { mutableStateOf<File?>(null) }

    val isEditPost = remember { viewModel.postId != null }

    val textFieldState = rememberSaveable(postState) {
        mutableStateOf(
            if (isEditPost) postState.data?.content ?: "" else ""
        )
    }
    val textFieldValue by textFieldState

    var showExitDialog by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) {
        if (it != null) {
            val resolver = context.contentResolver

            val stream = resolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(stream)

            try {
                val outputFile = File.createTempFile(
                    "cropped", ".jpg",
                    context.cacheDir,
                )

                val outputStream = FileOutputStream(outputFile)

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

                image = outputFile
            } catch (e: IOException) {
                throw RuntimeException("Failed to create temp file for output image", e)
            }
        }
    }

    val onBackPressed = {
        if (textFieldValue.isNotBlank()) {
            showExitDialog = true
        } else {
            navController.popBackStack()
        }
    }

    BackHandler {
        onBackPressed()
    }

    LaunchedEffect(postAddState) {
        if (postAddState is UIState.Failure) {
            val error = (postAddState as UIState.Failure<*>).error!!

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error.message ?: error.toString(),
                    withDismissAction = true,
                )
            }
        }

        if (postAddState is UIState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            when {
                                isEditPost -> R.string.edit_post
                                else -> R.string.create_post
                            }
                        )
                    )
                },
                navigationIcon = {
                    PlainTooltipBox({
                        Text(stringResource(id = R.string.discard))
                    }) {
                        IconButton(
                            onClick = {
                                onBackPressed()
                            },
                            modifier = Modifier
                                .tooltipTrigger()
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                            )
                        }
                    }
                },
                actions = {
                    if (isEditPost) {
                        TextButton(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.updatePost(textFieldValue)
                            },
                            enabled = textFieldValue.isNotBlank(),
                        ) {
                            Text(stringResource(id = R.string.save_comment))
                        }
                    } else {
                        TextButton(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.createPost(textFieldValue, image)
                            },
                            enabled = textFieldValue.isNotBlank(),
                        ) {
                            Text(stringResource(id = R.string.post_comment))
                        }
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        bottomBar = {
            if (!isEditPost) {
                BottomAppBar {
                    IconButton(onClick = {
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }) {
                        Icon(
                            painterResource(id = R.drawable.ic_image),
                            contentDescription = null,
                        )
                    }
                }
            }
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (viewModel.postId == null) {
            PostInputForm(
                textFieldState = textFieldState,
                image = image,
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.ime)
            )
        } else if (postState.data != null) {
            val post = postState.data!!

            PostInputForm(
                textFieldState = textFieldState,
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.ime)
            )
        } else if (postState.error != null) {
            ErrorView(
                error = postState.error!!,
                modifier = Modifier
                    .padding(paddingValues)
            ) {
                viewModel.getData()
            }
        } else {
            LoadingView(Modifier.padding(paddingValues))
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = {
                showExitDialog = false
            },
            title = {
                Text(
                    stringResource(id = R.string.discard_post_title)
                )
            },
            text = {
                Text(
                    stringResource(
                        if (isEditPost) R.string.discard_post_changes_message
                        else R.string.discard_post_message
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text(
                        stringResource(id = R.string.discard)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                    }
                ) {
                    Text(
                        stringResource(id = R.string.no)
                    )
                }
            },
        )
    }
}

@Composable
fun PostInputForm(
    modifier: Modifier = Modifier,
    textFieldState: MutableState<String>,
    image: File? = null,
) {
    var textFieldValue by remember { textFieldState }
    println(image)

    Column(
        modifier = modifier,
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
            },
            textStyle = LocalTextStyle.current.copy(
                color = LocalContentColor.current,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            if (textFieldValue.isEmpty()) {
                Text(
                    stringResource(R.string.post_text),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            it()
        }

        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaddingValues.withIme(): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        top = calculateTopPadding(),
        start = calculateStartPadding(layoutDirection),
        bottom = if (WindowInsets.isImeVisible) WindowInsets.ime
            .asPaddingValues()
            .calculateBottomPadding()
        else calculateBottomPadding(),
        end = calculateEndPadding(layoutDirection),
    )
}