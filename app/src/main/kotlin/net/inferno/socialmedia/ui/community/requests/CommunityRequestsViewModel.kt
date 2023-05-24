package net.inferno.socialmedia.ui.community.requests

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
import net.inferno.socialmedia.model.CommunityDetails
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.User
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class CommunityRequestsViewModel @Inject constructor(
    private val repository: Repository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val communityId = savedState.get<String>("communityId")!!

    private val _uiState = MutableStateFlow<UIState<CommunityDetails>>(UIState.Loading())
    val uiState get() = _uiState.asStateFlow()

    val currentUser = repository.getSavedUserFlow()

    init {
        getCommunityDetails()
    }

    fun getCommunityDetails(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            _uiState.emit(if (isRefreshing) _uiState.value.refresh() else UIState.Loading())

            try {
                val community = repository.getCommunityDetails(communityId)

                _uiState.emit(UIState.Success(community))
            } catch (e: HttpException) {
                _uiState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _uiState.emit(UIState.Failure(e))
            }
        }
    }

    fun acceptUser(user: User) {
        viewModelScope.launch {
        }
    }

    fun denyUser(user: User) {
        viewModelScope.launch {
        }
    }
}