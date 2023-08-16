package net.inferno.socialmedia.ui.chat.conversation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.Message
import net.inferno.socialmedia.view.BackIconButton
import net.inferno.socialmedia.view.ErrorView
import net.inferno.socialmedia.view.LoadingView
import net.inferno.socialmedia.view.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationUI(
    navController: NavController,
    viewModel: ConversationViewModel = hiltViewModel(),
) {
    val lifecycle = LocalLifecycleOwner.current

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var messageContent by remember { mutableStateOf("") }

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    val snackbarHostState = remember { SnackbarHostState() }

    val conversationOptionSheetState = rememberModalBottomSheetState()
    var showConversationOptionsSheet by rememberSaveable { mutableStateOf(false) }

    val conversationState by viewModel.conversationDataState.collectAsState()
    val messages by viewModel.messagesFlow.collectAsStateWithLifecycle(
        null,
        lifecycle,
    )
    LaunchedEffect(messages) {
        coroutineScope.launch {
            lazyListState.animateScrollToItem(0)
        }
    }

    val currentUser by viewModel.currentUser.collectAsState(null)

    currentUser ?: return

    val onConversationOptionsClick: (ConversationAction) -> Unit = { action ->
        when (action) {
            ConversationAction.CLOSE -> {
                viewModel.hideConversation()
            }
        }
    }

    val onMessageOptionsClick: (Message, MessageAction) -> Unit = { message, action ->
        when (action) {
            MessageAction.DELETE -> {
                viewModel.deleteMessage(message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (conversationState.data != null) {
                        val conversation = conversationState.data!!
                        val otherUser = conversation.getOtherUser(currentUser!!.id)

                        Row {
                            UserAvatar(
                                otherUser,
                                modifier = Modifier.size(36.dp),
                            )

                            Spacer(Modifier.width(16.dp))

                            Text(
                                "${otherUser.firstName} ${otherUser.lastName}",
                                modifier = Modifier
                                    .weight(1f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.hideConversation()
                            navController.popBackStack()
                        },
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_close),
                            null
                        )
                    }
                },
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
        contentWindowInsets = WindowInsets(0),
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (conversationState.data != null && messages != null) {
                if (messages!!.isNotEmpty()) {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 8.dp + paddingValues.calculateTopPadding(),
                            bottom = 8.dp,
                        ),
                        reverseLayout = true,
                        state = lazyListState,
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        items(
                            messages!!,
                            key = { it.id },
                        ) {
                            MessageItem(
                                it,
                                onOptionsClick =
                                if (it.sender.id == currentUser!!.id) onMessageOptionsClick
                                else null,
                            )
                        }
                    }
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(36.dp)
                            .padding(paddingValues)
                            .weight(1f)
                    ) {
                        Text(
                            stringResource(id = R.string.empty_conversation),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            } else if (conversationState.error != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(36.dp)
                        .padding(paddingValues)
                        .weight(1f)
                ) {
                    ErrorView(conversationState.error!!) {
                        viewModel.getConversationDetails()
                    }
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(36.dp)
                        .padding(paddingValues)
                        .weight(1f)
                ) {
                    LoadingView()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Row {
                    BasicTextField(
                        messageContent,
                        onValueChange = {
                            messageContent = it
                        },
                        decorationBox = {
                            if (messageContent.isBlank()) {
                                Text(
                                    stringResource(R.string.send_message),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            it()
                        },
                        textStyle = LocalTextStyle.current.copy(
                            color = LocalContentColor.current,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f)
                    )

                    IconButton(
                        onClick = {
                            viewModel.sendMessage(messageContent)
                            messageContent = ""
                        },
                        enabled = messageContent.isNotBlank()
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_send),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}

enum class ConversationAction {
    CLOSE,
    ;
}

enum class MessageAction {
    DELETE,
    ;
}