package net.inferno.socialmedia.ui.interests

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.model.UserAction
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class UserInterestsViewModel @Inject constructor(
    private val repository: Repository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UIState<List<UserAction>>>(UIState.Loading())
    val uiState get() = _uiState.asStateFlow()

    val currentUser = repository.getSavedUserFlow()

    init {
        getUserInterests()
    }

    fun getUserInterests() {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.loading())

            try {
                val interests = repository.getUserInterests()

                _uiState.emit(UIState.Success(interests))
            } catch (e: HttpException) {
                _uiState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _uiState.emit(UIState.Failure(e))
            }
        }
    }
}