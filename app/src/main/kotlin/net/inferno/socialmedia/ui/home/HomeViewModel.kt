package net.inferno.socialmedia.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.UserDetails
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _userDataState = MutableStateFlow<UIState<UserDetails>>(UIState.Loading())
    val userDataState get() = _userDataState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSavedUserFlow().collectLatest {
                _userDataState.emit(UIState.Success(it))
            }
        }

        getUser()
    }

    private fun getUser() {
        viewModelScope.launch {
            val user = repository.getUserDetails()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}