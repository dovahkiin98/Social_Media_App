package net.inferno.socialmedia.ui.auth.register

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.request.SignupRequest
import net.inferno.socialmedia.theme.SocialMediaTheme

@Composable
fun RegisterPhase1(
    inputEnabled: Boolean,
    signupRequest: SignupRequest,
    onUpdateInput: (SignupRequest) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    var showPassword by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.ime)
            .verticalScroll(scrollState)
            .padding(16.dp),
    ) {
        OutlinedTextField(
            value = signupRequest.email,
            onValueChange = {
                onUpdateInput(signupRequest.copy(email = it.trim()))
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
            enabled = inputEnabled,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = signupRequest.password,
            onValueChange = {
                onUpdateInput(signupRequest.copy(password = it))
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
            enabled = inputEnabled,
            singleLine = true,
            visualTransformation = if (!showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = signupRequest.confirmPassword,
            onValueChange = {
                onUpdateInput(signupRequest.copy(confirmPassword = it))
            },
            placeholder = {
                Text(stringResource(id = R.string.confirm_password))
            },
            label = {
                Text(stringResource(id = R.string.confirm_password))
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
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onDone()
                }
            ),
            enabled = inputEnabled,
            singleLine = true,
            visualTransformation = if (!showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        ElevatedButton(
            onClick = {
                onDone()
            },
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(stringResource(id = R.string.next))
        }
    }
}

@Preview(
    device = Devices.PIXEL_4,
    showSystemUi = true,
    name = "Light",
)
@Preview(
    device = Devices.PIXEL_4,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark",
)
@Composable
fun RegisterPhase1Preview() {
    SocialMediaTheme {
        Scaffold { paddingValues ->
            RegisterPhase1(
                inputEnabled = true,
                signupRequest = SignupRequest(
                    email = "ahmad.sattout.ee@gmail.com",
                    password = "12345678",
                    confirmPassword = "12345678",
                ),
                onUpdateInput = {

                },
                onDone = {

                },
                modifier = Modifier
                    .padding(paddingValues)
            )
        }
    }
}