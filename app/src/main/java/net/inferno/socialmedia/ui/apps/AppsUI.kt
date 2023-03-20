package net.inferno.socialmedia.ui.apps

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.UserApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsUI(
    navController: NavController,
    viewModel: AppsViewModel = hiltViewModel(),
) {
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "User Apps",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                scrollBehavior = topAppBarScrollBehavior,
                navigationIcon = {
                    PlainTooltipBox(tooltip = {
                        Text(stringResource(id = R.string.back))
                    }) {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .tooltipAnchor()
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(id = R.string.back),
                            )
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
        when (uiState) {
            is UIState.Failure -> {
                val error = (uiState as UIState.Failure).error!!

                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                    ) {
                        Text(
                            error.message ?: error.toString(),
                            fontSize = 20.sp,
                        )

                        Spacer(Modifier.height(16.dp))

                        ElevatedButton(
                            onClick = {
                                viewModel.getUserApps()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            is UIState.Loading -> {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is UIState.Success -> {
                val apps = (uiState as UIState.Success<List<UserApp>>).data!!

                LazyColumn(
                    contentPadding = paddingValues,
                    state = lazyListState,
                ) {
                    items(apps) {
                        ListItem(
                            headlineContent = {
                                Text(it.name)
                            },
                            supportingContent = {
                                Text(it.packageName)
                            },
                            leadingContent = {
                                Image(
                                    bitmap = it.drawable.toBitmap().asImageBitmap(),
                                    contentDescription = it.name,
                                    modifier = Modifier
                                        .size(56.dp)
                                )
                            },
                            modifier = Modifier
                                .clickable {
                                    val launchIntent = viewModel.intent(it)

                                    try {
                                        context.startActivity(launchIntent!!)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Unable to launch intent ${e.message}",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                }
                        )
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                )
            }
        }
    }
}