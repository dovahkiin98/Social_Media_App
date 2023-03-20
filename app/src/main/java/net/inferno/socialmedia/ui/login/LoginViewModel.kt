package net.inferno.socialmedia.ui.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import net.inferno.socialmedia.BuildConfig
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.UIState
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: Repository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UIState<Unit>?>(null)

    val uiState = _uiState.asStateFlow()

    val emailValue = mutableStateOf(if (BuildConfig.DEBUG) "ahmad.sattout.ee@gmail.com" else "")
    val passwordValue = mutableStateOf(if (BuildConfig.DEBUG) "12345678" else "")

    fun login() {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            _uiState.emit(UIState.Loading())

            try {
                withTimeout(Repository.TIMEOUT) {
                    withContext(Dispatchers.IO) {
                        repository.login(
                            email = emailValue.value.trim(),
                            password = passwordValue.value.trim(),
                        )
                    }
                }

                _uiState.emit(UIState.Success(null))
            } catch (e: HttpException) {
                _uiState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _uiState.emit(UIState.Failure(e))
            }

            delay(200)

            _uiState.emit(null)
        }
    }
}