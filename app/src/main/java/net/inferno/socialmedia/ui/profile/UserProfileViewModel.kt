package net.inferno.socialmedia.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.model.UserDetails
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var imageType: String? = null

    val userId = savedStateHandle.get<String?>("userId")

    val currentUser = repository.getSavedUserFlow()

    private val _userDataState = MutableStateFlow<UIState<UserDetails>>(UIState.Loading())
    val userDataState get() = _userDataState.asStateFlow()

    private val _userPostsState = MutableStateFlow<UIState<List<Post>>>(UIState.Loading())
    val userPostsState get() = _userPostsState.asStateFlow()

    private val _coverImageUploadState = MutableStateFlow<UIState<Unit>?>(null)
    val coverImageUploadState = _coverImageUploadState.asStateFlow()

    private val _profileImageUploadState = MutableStateFlow<UIState<Unit>?>(null)
    val profileImageUploadState = _profileImageUploadState.asStateFlow()

    init {
        if (userId == null) viewModelScope.launch {
            repository.getSavedUserFlow().collectLatest {
                _userDataState.emit(UIState.Success(it))
            }
        }

        getUserDetails()
    }

    fun getUserDetails() {
        _userDataState.value = _userDataState.value.refresh()

        viewModelScope.launch {
            delay(1_000)

            try {
                val user = withContext(Dispatchers.IO) {
                    repository.getUserDetails(userId)
                }

                if (userId != null) _userDataState.emit(UIState.Success(user))
            } catch (e: HttpException) {
                _userDataState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _userDataState.emit(UIState.Failure(e))
            }
        }

        getUserPosts()
    }

    fun getUserPosts() {
        _userPostsState.value = _userPostsState.value.refresh()

        viewModelScope.launch {
            delay(1_000)

            try {
                val posts = withContext(Dispatchers.IO) {
                    repository.getUserPosts(userId)
                }

                _userPostsState.emit(UIState.Success(posts))
            } catch (e: HttpException) {
                _userPostsState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _userPostsState.emit(UIState.Failure(e))
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

    fun uploadProfileImage(croppedImage: File) {
        viewModelScope.launch {
            _profileImageUploadState.value = UIState.Loading()

            try {
                val user = withContext(Dispatchers.IO) {
                    repository.uploadProfileImage(croppedImage)
                }

                _userDataState.emit(UIState.Success(user))
                _profileImageUploadState.emit(UIState.Success(null))
            } catch (e: HttpException) {
                _profileImageUploadState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _profileImageUploadState.emit(UIState.Failure(e))
            }

            delay(200)

            _profileImageUploadState.emit(null)
            imageType = null
        }
    }

    fun uploadCoverImage(croppedImage: File) {
        viewModelScope.launch {
            _coverImageUploadState.value = UIState.Loading()

            try {
                val user = withContext(Dispatchers.IO) {
                    repository.uploadCoverImage(croppedImage)
                }

                _userDataState.emit(UIState.Success(user))
                _coverImageUploadState.emit(UIState.Success(null))
            } catch (e: HttpException) {
                _coverImageUploadState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _coverImageUploadState.emit(UIState.Failure(e))
            }

            delay(200)

            _coverImageUploadState.emit(null)
            imageType = null
        }
    }

    fun likePost(post: Post) {
        viewModelScope.launch {
            val posts = _userPostsState.value.data!!.toMutableList()
            val index = posts.indexOf(post)

            val newPost = withContext(Dispatchers.IO) {
                repository.likePost(post)
            }

            posts[index] = newPost

            _userPostsState.emit(UIState.Success(posts))
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            val posts = _userPostsState.value.data!!.toMutableList()
            val index = posts.indexOf(post)

            withContext(Dispatchers.IO) {
                repository.deletePost(post)
            }

            posts.removeAt(index)

            _userPostsState.emit(UIState.Success(posts))
        }
    }
}