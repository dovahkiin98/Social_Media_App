package net.inferno.socialmedia.ui.login

import android.util.Patterns
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.inferno.socialmedia.BuildConfig
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.theme.SocialMediaTheme
import net.inferno.socialmedia.ui.main.Routes
import net.inferno.socialmedia.utils.CustomPreview
import java.util.UUID

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginUI(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val scrollState = rememberScrollState()

    val keyboardController = LocalSoftwareKeyboardController.current

    val snackbarHostState = remember { SnackbarHostState() }

    val emailState = viewModel.emailValue
    val passwordState = viewModel.passwordValue

    val emailValue by viewModel.emailValue
    val passwordValue by viewModel.passwordValue

    val uiState by viewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val validateInput: () -> Boolean = {
        if (emailValue.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Please enter a valid email address")
            }

            false
        } else if (passwordValue.isBlank()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Please enter a password")
            }

            false
        } else {
            true
        }
    }

    val login = {
        keyboardController?.hide()

        snackbarHostState.currentSnackbarData?.dismiss()

        if (validateInput()) {
            viewModel.login()
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

    var showUrlDialog by remember { mutableStateOf(false) }

    if (showUrlDialog) {
        var url by viewModel.urlValue

        AlertDialog(
            onDismissRequest = {
                showUrlDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateUrl(url)
                        showUrlDialog = false
                    },
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUrlDialog = false
                    },
                ) {
                    Text("Cancel")
                }
            },
            text = {
                TextField(
                    value = url,
                    onValueChange = {
                        url = it
                    },
                    singleLine = true,
                )
            },
        )
    }

    LoginUI(
        uiState = uiState,
        emailState = emailState,
        passwordState = passwordState,
        snackbarHostState = snackbarHostState,
        scrollState = scrollState,
        onLogin = login,
        onResetPassword = {
            navController.navigate(Routes.RESET_PASSWORD)
        },
        onRegister = {
            navController.navigate(Routes.REGISTER)
        },
        topAppBarModifier = Modifier
            .then(
                if (BuildConfig.DEBUG) Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            navController.navigate(Routes.APPS)
                        },
                        onLongPress = {
                            showUrlDialog = true
                        }
                    )
                }
                else Modifier
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginUI(
    uiState: UIState<Unit>?,
    emailState: MutableState<String> = remember { mutableStateOf("") },
    passwordState: MutableState<String> = remember { mutableStateOf("") },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scrollState: ScrollState = rememberScrollState(),
    onLogin: () -> Unit = {},
    onResetPassword: () -> Unit = {},
    onRegister: () -> Unit = {},
    topAppBarModifier: Modifier = Modifier,
) {
    var emailValue by emailState
    var passwordValue by passwordState

    val isLoading by remember(uiState) {
        derivedStateOf { uiState is UIState.Loading || uiState is UIState.Success<*> }
    }
    var showPassword by rememberSaveable {
        mutableStateOf(false)
    }

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.login),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                scrollBehavior = topAppBarScrollBehavior,
                modifier = topAppBarModifier
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
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.ime)
                .verticalScroll(scrollState)
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = emailValue,
                onValueChange = {
                    emailValue = it.trim()
                },
                placeholder = {
                    Text(stringResource(id = R.string.email))
                },
                label = {
                    Text(stringResource(id = R.string.email))
                },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                enabled = !isLoading,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = passwordValue,
                onValueChange = {
                    passwordValue = it
                },
                placeholder = {
                    Text(stringResource(id = R.string.password))
                },
                label = {
                    Text(stringResource(id = R.string.password))
                },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            showPassword = !showPassword
                        }
                    ) {
                        Icon(
                            painterResource(
                                if (showPassword) R.drawable.ic_password_shown
                                else R.drawable.ic_password_hidden
                            ),
                            contentDescription = null,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onLogin()
                    }
                ),
                enabled = !isLoading,
                singleLine = true,
                visualTransformation = if (!showPassword) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = Modifier
                    .fillMaxWidth()
            )

            if (isLoading) {
                Spacer(Modifier.height(32.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = onResetPassword,
                ) {
                    Text(stringResource(id = R.string.forgot_password))
                }

                Spacer(Modifier.height(8.dp))

                ElevatedButton(
                    onClick = {
                        onLogin()
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(stringResource(id = R.string.login))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Divider(
                        modifier = Modifier
                            .weight(1f)
                    )

                    Text(
                        stringResource(id = R.string.or).uppercase(),
                        modifier = Modifier
                            .padding(16.dp)
                    )

                    Divider(
                        modifier = Modifier
                            .weight(1f)
                    )
                }

                OutlinedButton(
                    onClick = onRegister,
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(stringResource(id = R.string.register))
                }
            }
        }
    }
}

@CustomPreview
@Composable
fun LoginUIPreviewIdle() {
    SocialMediaTheme {
        LoginUI(
            uiState = null,
        )
    }
}

@CustomPreview
@Composable
fun LoginUIPreviewLoading() {
    SocialMediaTheme {
        LoginUI(
            uiState = UIState.Loading(),
        )
    }
}