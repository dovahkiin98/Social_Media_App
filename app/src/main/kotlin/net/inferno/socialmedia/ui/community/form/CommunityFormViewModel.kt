package net.inferno.socialmedia.ui.community.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.UIState
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class CommunityFormViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _createCommunityState = MutableStateFlow<UIState<Unit>?>(null)
    val createCommunityState = _createCommunityState.asStateFlow()

    fun createCommunity(
        name: String,
        description: String,
        category: String,
    ) {
        viewModelScope.launch {
            _createCommunityState.emit(UIState.Loading())

            try {
                val response = repository.createCommunity(
                    name.trim(),
                    description.trim(),
                    category.takeIf { it.isNotBlank() },
                )

                _createCommunityState.emit(UIState.Success())
            } catch (e: HttpException) {
                _createCommunityState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _createCommunityState.emit(UIState.Failure(e))
            }

            delay(200)

            _createCommunityState.emit(null)
        }
    }
}