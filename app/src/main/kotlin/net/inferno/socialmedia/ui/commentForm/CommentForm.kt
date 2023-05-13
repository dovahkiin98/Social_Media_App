package net.inferno.socialmedia.ui.commentForm

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.utils.parseAsHtml
import net.inferno.socialmedia.view.ErrorView
import net.inferno.socialmedia.view.LoadingView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CommentForm(
    navController: NavController,
    viewModel: CommentFormViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    val snackbarHostState = remember { SnackbarHostState() }

//    val currentUser by viewModel.currentUser.collectAsState(null)
    val postState by viewModel.postDataState.collectAsState()
    val commentState by viewModel.commentDataState.collectAsState()

    val commentAddState by viewModel.commentAddState.collectAsState()

    val dataState = if (viewModel.commentId != null) commentState else postState
    val isComment = remember { viewModel.commentId != null }
    val isEditComment = remember { viewModel.postId == null }

    var textFieldValue by rememberSaveable(commentState) {
        mutableStateOf(
            if (isEditComment) commentState.data?.content ?: "" else ""
        )
    }

    var showExitDialog by remember { mutableStateOf(false) }

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

    LaunchedEffect(commentAddState) {
        if (commentAddState is UIState.Failure) {
            val error = (commentAddState as UIState.Failure<*>).error!!

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error.message ?: error.toString(),
                    withDismissAction = true,
                )
            }
        }

        if (commentAddState is UIState.Success) {
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
                                isEditComment -> R.string.edit_comment
                                isComment -> R.string.add_reply
                                else -> R.string.add_comment
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
                    if (isEditComment) {
                        TextButton(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.saveComment(textFieldValue)
                            },
                            enabled = textFieldValue.isNotBlank(),
                        ) {
                            Text(stringResource(id = R.string.save_comment))
                        }
                    } else {
                        TextButton(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.postComment(textFieldValue)
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
//        bottomBar = {
//            BottomAppBar {
//
//            }
//        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (dataState.data != null) {
            val post = postState.data
            val comment = commentState.data

            val content = if (isComment) commentState.data!!.content else postState.data!!.content

            Column(
                modifier = Modifier
                    .padding(paddingValues)
            ) {
                if (post != null) {
                    val op = post.publisher
                    val commentUser = if (isComment) commentState.data!!.user else null

                    if (commentUser != null) {
                        Text(
                            "${commentUser.firstName} ${commentUser.lastName}",
                            color =
                            if (commentUser.id == op.id) MaterialTheme.colorScheme.primary
                            else Color.Unspecified,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 8.dp,
                                )
                        )
                    }

                    Text(
                        content.parseAsHtml(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(12.dp)
                    )

                    Divider()
                }

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
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (textFieldValue.isEmpty()) {
                        Text(
                            stringResource(
                                if (isComment) R.string.your_comment
                                else R.string.your_reply
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    it()
                }
            }
        } else if (dataState.error != null) {
            ErrorView(
                error = dataState.error,
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
                    stringResource(id = R.string.discard_comment_title)
                )
            },
            text = {
                Text(
                    stringResource(id = R.string.discard_comment_message)
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