package net.inferno.socialmedia.ui.chat.conversationsList

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import net.inferno.socialmedia.model.Conversation
import net.inferno.socialmedia.ui.main.Routes
import net.inferno.socialmedia.view.BackIconButton
import net.inferno.socialmedia.view.CustomModalBottomSheet
import net.inferno.socialmedia.view.LoadingView
import net.inferno.socialmedia.view.UserAvatar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConversationsListUI(
    navController: NavController,
    viewModel: ConversationsListViewModel = hiltViewModel(),
) {
    val lifecycle = LocalLifecycleOwner.current

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    val snackbarHostState = remember { SnackbarHostState() }

    val optionsSheetState = rememberModalBottomSheetState()
    var showOptionsSheet by rememberSaveable { mutableStateOf(false) }

    val conversations by viewModel.conversationsFlow.collectAsStateWithLifecycle(
        null,
        lifecycle,
    )
    val currentUser by viewModel.currentUser.collectAsState(null)

    currentUser ?: return

    var selectedConversation by remember { mutableStateOf<Conversation?>(null) }
    val onOptionsClick: (Conversation, ConversationAction) -> Unit = { conversation, action ->
        when (action) {
            ConversationAction.CLOSE -> {
                viewModel.hideConversation(conversation)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.conversations)
                    )
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
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (conversations != null) {
            if (conversations!!.isNotEmpty()) {
                LazyColumn(
                    contentPadding = paddingValues,
                ) {
                    items(conversations!!, key = { it.id }) {
                        val otherUser = it.getOtherUser(currentUser!!.id)

                        ListItem(
                            headlineContent = {
                                Text("${otherUser.firstName} ${otherUser.lastName}")
                            },
                            leadingContent = {
                                UserAvatar(
                                    otherUser,
                                    modifier = Modifier.size(56.dp),
                                )
                            },
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        navController.navigate(Routes.conversation(it.id))
                                    },
                                    onLongClick = {
                                        selectedConversation = it
                                        showOptionsSheet = true
                                    },
                                )
                        )
                    }
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(36.dp)
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    Text(
                        stringResource(id = R.string.empty_conversations),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(36.dp)
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                LoadingView()
            }
        }
    }

    if (showOptionsSheet) {
        CustomModalBottomSheet(
            onDismissRequest = {
                selectedConversation = null
                showOptionsSheet = false
            },
            sheetState = optionsSheetState,
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(id = R.drawable.ic_close),
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(id = R.string.close_conversation))
                },
                modifier = Modifier
                    .clickable {
                        onOptionsClick(selectedConversation!!, ConversationAction.CLOSE)
                        showOptionsSheet = false

                        coroutineScope.launch {
                            optionsSheetState.hide()
                        }
                    }
            )
        }
    }
}

enum class ConversationAction {
    CLOSE,
    ;
}