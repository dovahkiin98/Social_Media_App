package net.inferno.socialmedia.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.UserDetails
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _userDataState = MutableStateFlow<UIState<UserDetails>>(UIState.Loading())
    val userDataState get() = _userDataState.asStateFlow()

    private val _postsDataState = MutableStateFlow<UIState<List<Post>>>(UIState.Loading())
    val postsDataState get() = _postsDataState.asStateFlow()

    private val _postDeletionState = MutableStateFlow<UIState<Unit>?>(null)
    val postDeletionState = _postDeletionState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSavedUserFlow().collectLatest {
                _userDataState.emit(UIState.Success(it))
            }
        }

        getUser()
        getNewsFeed()
    }

    private fun getUser() {
        viewModelScope.launch {
            val user = repository.getUserDetails()
        }
    }

    fun getNewsFeed() {
        viewModelScope.launch {
            _postsDataState.value = UIState.Loading()

            try {
                val posts = repository.getNewsFeed()

                _postsDataState.emit(UIState.Success(posts))
            } catch (e: HttpException) {
                _postsDataState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _postsDataState.emit(UIState.Failure(e))
            }
        }
    }

    fun likePost(post: Post) {
        viewModelScope.launch {
            val posts = _postsDataState.value.data!!.toMutableList()
            val index = posts.indexOf(post)

            val newPost = repository.likePost(post)

//            posts[index] = newPost

            _postsDataState.emit(UIState.Success(posts))
        }
    }

    fun deletePost(post: Post) {
        _postDeletionState.value = UIState.Loading()

        viewModelScope.launch {
            val posts = _postsDataState.value.data!!.toMutableList()
            val index = posts.indexOf(post)

            try {
                repository.deletePost(post)

                posts.removeAt(index)

                _postsDataState.emit(UIState.Success(posts))
                _postDeletionState.emit(UIState.Success(null))
            } catch (e: HttpException) {
                _postDeletionState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _postDeletionState.emit(UIState.Failure(e))
            }

            delay(200)

            _postDeletionState.emit(null)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}