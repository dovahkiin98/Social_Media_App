package net.inferno.socialmedia.ui.editProfile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.UserDetails
import net.inferno.socialmedia.model.UserGender
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _updatingState = MutableStateFlow<UIState<Unit>?>(null)
    val updatingState = _updatingState.asStateFlow()

    val firstNameValue = mutableStateOf("")
    val lastNameValue = mutableStateOf("")
    val genderValue = mutableStateOf(UserGender.MALE)
    val addressValue = mutableStateOf("")
//    val firstNameValue = mutableStateOf("")

    init {
        viewModelScope.launch {
            val userData = repository.getSavedUserFlow().first()!!

            firstNameValue.value = userData.firstName
            lastNameValue.value = userData.lastName
        }
    }

    fun updateUserDetails() {

    }
}