package net.inferno.socialmedia.ui.followes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.User
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class UserFollowesViewModel @Inject constructor(
    private val repository: Repository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val userId = savedState.get<String>("userId")

    private val _uiState = MutableStateFlow<UIState<List<User>>>(UIState.Loading())
    val uiState get() = _uiState.asStateFlow()

    val currentUser = repository.getSavedUserFlow()

    init {
        getUserFollowings()
    }

    fun getUserFollowings(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            _uiState.emit(if (isRefreshing) _uiState.value.refresh() else UIState.Loading())

            try {
                val followers = withContext(Dispatchers.IO) {
                    repository.getUserFollowings(userId)
                }

                _uiState.emit(UIState.Success(followers))
            } catch (e: HttpException) {
                _uiState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _uiState.emit(UIState.Failure(e))
            }
        }
    }

    fun toggleFollow(user: User) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.toggleFollow(user)
            }
        }
    }
}