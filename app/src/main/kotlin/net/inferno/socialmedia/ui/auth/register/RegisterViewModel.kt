package net.inferno.socialmedia.ui.auth.register

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
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.UserAddress
import net.inferno.socialmedia.model.UserGender
import net.inferno.socialmedia.model.request.SignupRequest
import retrofit2.HttpException
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: Repository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UIState<Unit>?>(null)

    val uiState = _uiState.asStateFlow()

    val signupRequest = mutableStateOf(
        SignupRequest(
            firstName = "Ahmad",
            lastName = "Sattout",
            email = "ahmad.sattout.ee@gmail.com",
            password = "12345678",
            confirmPassword = "12345678",
            gender = UserGender.MALE,
            address = UserAddress(country = "Syria"),
            dateOfBirth = LocalDate.of(1996, 11, 24),
            age = 26,
        )
    )

    fun signup() {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            _uiState.emit(UIState.Loading())

            try {
                repository.signup(signupRequest.value)

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