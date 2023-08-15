package net.inferno.socialmedia.ui.community.form

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.view.LoadingDialog
import net.inferno.socialmedia.view.OutlinedExposedDropdownMenu

val categories = mapOf(
    "None" to "",
    "Sport" to "sport",
    "Business" to "business",
    "Technology" to "tech",
    "Politics" to "politics",
    "Entertainment" to "Entertainment",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFormUI(
    navController: NavController,
    viewModel: CommunityFormViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    val scrollState = rememberScrollState()

    val snackbarHostState = remember { SnackbarHostState() }

    val createCommunityState by viewModel.createCommunityState.collectAsState()

    var nameFieldValue by rememberSaveable {
        mutableStateOf("")
    }
    var descriptionFieldValue by rememberSaveable {
        mutableStateOf("")
    }
    var categoryValue by rememberSaveable {
        mutableStateOf("None")
    }
    var showCategoryMenu by remember {
        mutableStateOf(false)
    }

    var showExitDialog by remember { mutableStateOf(false) }

    val onBackPressed = {
        if (nameFieldValue.isNotBlank() || descriptionFieldValue.isNotBlank()) {
            showExitDialog = true
        } else {
            navController.popBackStack()
        }
    }

    BackHandler {
        onBackPressed()
    }

    LaunchedEffect(createCommunityState) {
        if (createCommunityState is UIState.Failure) {
            val error = (createCommunityState as UIState.Failure<*>).error!!

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error.message ?: error.toString(),
                    withDismissAction = true,
                )
            }
        }

        if (createCommunityState is UIState.Success) {
            val previousHandle = navController.previousBackStackEntry?.savedStateHandle

            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.create_community)
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
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = nameFieldValue,
                onValueChange = {
                    nameFieldValue = it
                },
                label = {
                    Text(stringResource(id = R.string.community_name))
                },
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = descriptionFieldValue,
                onValueChange = {
                    descriptionFieldValue = it
                },
                label = {
                    Text(stringResource(id = R.string.community_description))
                },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedExposedDropdownMenu(
                expanded = showCategoryMenu,
                onExpandedChange = {
                    showCategoryMenu = it
                },
                value = categoryValue,
                label = { Text(stringResource(id = R.string.community_category)) },
                placeholder = { },
                leadingIcon = { },
            ) {
                categories.forEach {
                    ListItem(
                        headlineContent = { Text(it.key) },
                        modifier = Modifier
                            .clickable {
                                showCategoryMenu = false
                                categoryValue = it.key
                            }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            ElevatedButton(
                onClick = {
                    keyboardController?.hide()
                    viewModel.createCommunity(
                        nameFieldValue,
                        descriptionFieldValue,
                        categories[categoryValue] ?: "",
                    )
                },
                enabled = nameFieldValue.isNotBlank() && descriptionFieldValue.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.create_community))
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

        if (createCommunityState is UIState.Loading) {
            LoadingDialog(
                text = {
                    Text(
                        stringResource(R.string.creating_community) + "...",
                    )
                },
            )
        }
    }
}