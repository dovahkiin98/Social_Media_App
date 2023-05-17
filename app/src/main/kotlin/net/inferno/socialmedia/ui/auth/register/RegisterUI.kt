package net.inferno.socialmedia.ui.auth.register

import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.ui.main.Routes
import net.inferno.socialmedia.view.BackIconButton

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
)
@Composable
fun RegisterUI(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    var signupRequest by viewModel.signupRequest

    val pagerState = rememberPagerState()

    val keyboardController = LocalSoftwareKeyboardController.current

    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val isLoading by remember {
        derivedStateOf { uiState is UIState.Loading || uiState is UIState.Success<*> }
    }

    BackHandler {
        if (pagerState.currentPage != 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(0)
            }
        } else {
            navController.popBackStack()
        }
    }

    val validatePhase1: () -> Boolean = {
        if (signupRequest.email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(signupRequest.email)
                .matches()
        ) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Please enter a valid email address")
            }

            false
        } else if (signupRequest.password.isBlank()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Please enter a password")
            }

            false
        } else if (signupRequest.password != signupRequest.confirmPassword) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Passwords do not match")
            }

            false
        } else {
            true
        }
    }

    val validatePhase2: () -> Boolean = {
        if (
            signupRequest.firstName.isBlank() ||
            signupRequest.lastName.isBlank() ||
            signupRequest.age == -1 ||
            signupRequest.gender == null ||
            signupRequest.address.country.isBlank()
        ) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Please fill all fields")
            }

            false
        } else {
            true
        }
    }

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    val register = {
        keyboardController?.hide()

        snackbarHostState.currentSnackbarData?.dismiss()

        if (validatePhase1() && validatePhase2()) {
            viewModel.signup()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UIState.Success<*>) {
            navController.popBackStack()
            navController.navigate(Routes.HOME)
        }

        if (uiState is UIState.Failure) {
            val error = (uiState as UIState.Failure).error!!

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error.message ?: error.toString(),
                    withDismissAction = true,
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.register),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    BackIconButton {
                        if (pagerState.currentPage != 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(paddingValues),
        ) {
            HorizontalPager(
                pageCount = 2,
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .weight(1f),
            ) { page ->
                if (page == 0) {
                    RegisterPhase1(
                        inputEnabled = !isLoading,
                        signupRequest = signupRequest,
                        onUpdateInput = {
                            signupRequest = it
                        },
                        onDone = {
                            keyboardController?.hide()

                            if (validatePhase1()) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            }
                        },
                    )
                } else if (page == 1) {
                    RegisterPhase2(
                        isLoading = isLoading,
                        signupRequest = signupRequest,
                        onUpdateInput = {
                            signupRequest = it
                        },
                        onDone = {
                            register()
                        },
                    )
                }
            }
        }
    }
}