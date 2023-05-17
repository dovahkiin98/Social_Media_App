package net.inferno.socialmedia.ui.community.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.CommunityDetails
import net.inferno.socialmedia.model.CommunityPost
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.User
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val communityId = savedStateHandle.get<String?>("communityId")!!

    val currentUser = repository.getSavedUserFlow()

    private val _communityDataState = MutableStateFlow<UIState<CommunityDetails>>(UIState.Loading())
    val communityData get() = _communityDataState.asStateFlow()

    private val _communityPostsState =
        MutableStateFlow<UIState<List<CommunityPost>>>(UIState.Loading())
    val communityPostsState get() = _communityPostsState.asStateFlow()

    private val _coverImageUploadState = MutableStateFlow<UIState<Unit>?>(null)
    val coverImageUploadState = _coverImageUploadState.asStateFlow()

    private val _postDeletionState = MutableStateFlow<UIState<Unit>?>(null)
    val postDeletionState = _postDeletionState.asStateFlow()

    init {
        getCommunityDetails()
    }

    fun getCommunityDetails() {
        _communityDataState.value = _communityDataState.value.refresh()

        viewModelScope.launch {
            delay(1_000)

            try {
                val community = withContext(Dispatchers.IO) {
                    repository.getCommunityDetails(communityId)
                }

                _communityDataState.emit(UIState.Success(community))
            } catch (e: HttpException) {
                _communityDataState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _communityDataState.emit(UIState.Failure(e))
            }
        }

        getCommunityPosts()
    }

    fun getCommunityPosts() {
        _communityPostsState.value = _communityPostsState.value.refresh()

        viewModelScope.launch {
            delay(1_000)

            try {
                val posts = withContext(Dispatchers.IO) {
                    repository.getCommunityPosts(communityId)
                }

                _communityPostsState.emit(UIState.Success(posts))
            } catch (e: HttpException) {
                _communityPostsState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _communityPostsState.emit(UIState.Failure(e))
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

    fun uploadCoverImage(croppedImage: File) {
        viewModelScope.launch {
            _coverImageUploadState.value = UIState.Loading()

            try {
                val community = _communityDataState.value.data!!

                withContext(Dispatchers.IO) {
                    repository.uploadCommunityCoverImage(community, croppedImage)
                }

                _communityDataState.emit(UIState.Success(community))
                _coverImageUploadState.emit(UIState.Success(null))
            } catch (e: HttpException) {
                _coverImageUploadState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _coverImageUploadState.emit(UIState.Failure(e))
            }

            delay(200)

            _coverImageUploadState.emit(null)
        }
    }

    fun likePost(post: CommunityPost) {
        viewModelScope.launch {
            val posts = _communityPostsState.value.data!!.toMutableList()
            val index = posts.indexOf(post)

            val newPost = withContext(Dispatchers.IO) {
                repository.likePost(post.post)
            }

//            posts[index] = newPost

            _communityPostsState.emit(UIState.Success(posts))
        }
    }

    fun deletePost(post: Post) {
        _postDeletionState.value = UIState.Loading()

        viewModelScope.launch {
            val posts = _communityPostsState.value.data!!.toMutableList()
//            val index = posts.indexOf(post)

            try {
                withContext(Dispatchers.IO) {
                    repository.deletePost(post)
                }

//                posts.removeAt(index)

                _communityPostsState.emit(UIState.Success(posts))
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
}