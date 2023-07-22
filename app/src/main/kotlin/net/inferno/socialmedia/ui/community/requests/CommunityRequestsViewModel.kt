package net.inferno.socialmedia.ui.community.requests

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    private val _errorState = MutableStateFlow<Throwable?>(null)
    val errorState get() = _errorState.asStateFlow()

    val currentUser = repository.getSavedUserFlow()

    init {
        getCommunityDetails()
    }

    fun getCommunityDetails() {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.loading())

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
            try {
                repository.approveJoinRequest(communityId, user.id)
                getCommunityDetails()
            } catch (e: Exception) {
                _errorState.emit(e)

                delay(200)

                _errorState.emit(null)
            }
        }
    }

    fun denyUser(user: User) {
        viewModelScope.launch {
            try {
                repository.denyJoinRequest(communityId, user.id)
                getCommunityDetails()
            } catch (e: Exception) {
                _errorState.emit(e)

                delay(200)

                _errorState.emit(null)
            }
        }
    }
}