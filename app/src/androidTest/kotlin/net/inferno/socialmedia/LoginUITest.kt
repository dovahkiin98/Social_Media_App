package net.inferno.socialmedia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.ui.auth.login.LoginUI
import net.inferno.socialmedia.ui.main.MainActivity
import org.junit.Rule
import org.junit.Test

class LoginUITest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val loginText get() = rule.activity.getString(R.string.login)

    @Test
    fun testLoginButton() {
        rule.setContent {
            val coroutineScope = rememberCoroutineScope()
            var uiState by remember { mutableStateOf<UIState<Unit>?>(null) }

            LoginUI(
                uiState = uiState,
                onLogin = {
                    coroutineScope.launch {
                        uiState = UIState.Loading()

                        delay(1000)

                        uiState = null
                    }
                },
            )
        }

        rule.onNodeWithTag("Loading Indicator").assertDoesNotExist()

        rule.onNode(
            hasText(loginText)
                    and
                    hasClickAction()
        ).performClick()

        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithTag("Loading Indicator").assertExists()

        rule.mainClock.advanceTimeBy(1000)

        rule.onNodeWithTag("Loading Indicator").assertDoesNotExist()
    }

    @Test
    fun testInvalidInput() {
        rule.setContent {
            val coroutineScope = rememberCoroutineScope()
            var uiState by remember { mutableStateOf<UIState<Unit>?>(null) }

            val emailState = remember { mutableStateOf("ezio1497@gmail") }
            val passwordState = remember { mutableStateOf("") }

            LoginUI(
                uiState = uiState,
                onLogin = {
                    coroutineScope.launch {
                        uiState = UIState.Loading()

                        delay(1000)

                        uiState = null
                    }
                },
                emailState = emailState,
                passwordState = passwordState,
            )
        }

        rule.onNodeWithTag("Loading Indicator").assertDoesNotExist()

        rule.onNode(
            hasText(loginText)
                    and
                    hasClickAction()
        ).performClick()

        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithTag("Loading Indicator").assertExists()

        rule.mainClock.advanceTimeBy(1000)

        rule.onNodeWithTag("Loading Indicator").assertDoesNotExist()
    }
}