package net.inferno.socialmedia.ui.community.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.CommunityDetails
import net.inferno.socialmedia.model.CommunityPost
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UIState
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
        _communityDataState.value = _communityDataState.value.loading()

        viewModelScope.launch {
            delay(1_000)

            try {
                val community = repository.getCommunityDetails(communityId)

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
        _communityPostsState.value = _communityPostsState.value.loading()

        viewModelScope.launch {
            delay(1_000)

            try {
                val posts = repository.getCommunityPosts(communityId)

                _communityPostsState.emit(UIState.Success(posts))
            } catch (e: HttpException) {
                _communityPostsState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _communityPostsState.emit(UIState.Failure(e))
            }
        }
    }

    fun sendJoinRequest() {
        viewModelScope.launch {
            try {
                val community = repository.sendJoinRequest(communityId)

                _communityDataState.emit(UIState.Success(community))
            } catch (e: Exception) {

            }
        }
    }

    fun cancelJoinRequest() {
        viewModelScope.launch {
            try {
                val community = repository.cancelJoinRequest(communityId)

                _communityDataState.emit(UIState.Success(community))
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    fun leaveCommunity() {
        viewModelScope.launch {
            try {
                val community = repository.leaveCommunity(communityId)

                _communityDataState.emit(UIState.Success(community))
            } catch (e: Exception) {

            }
        }
    }

    fun uploadCoverImage(croppedImage: File) {
        viewModelScope.launch {
            _coverImageUploadState.value = UIState.Loading()

            try {
                val community = _communityDataState.value.data!!

                repository.uploadCommunityCoverImage(community, croppedImage)

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

    fun likePost(post: Post) {
        viewModelScope.launch {
            val posts = _communityPostsState.value.data!!.toMutableList()
            val index = posts.indexOfFirst { it.post.id == post.id }
            val communityPost = posts[index]

            val newPost = repository.likePost(post)
            val newCommunityPost = CommunityPost(
                id = communityPost.id,
                approved = communityPost.approved,
                post = newPost,
            )

            posts[index] = newCommunityPost

            _communityPostsState.emit(UIState.Success(posts))
        }
    }

    fun dislikePost(post: Post) {
        viewModelScope.launch {
            val posts = _communityPostsState.value.data!!.toMutableList()
            val index = posts.indexOfFirst { it.post.id == post.id }
            val communityPost = posts[index]

            val newPost = repository.dislikePost(post)
            val newCommunityPost = CommunityPost(
                id = communityPost.id,
                approved = communityPost.approved,
                post = newPost,
            )

            posts[index] = newCommunityPost

            _communityPostsState.emit(UIState.Success(posts))
        }
    }

    fun deletePost(post: Post) {
        _postDeletionState.value = UIState.Loading()

        viewModelScope.launch {
            val currentUser = currentUser.first()!!
            val posts = _communityPostsState.value.data!!.toMutableList()
            val index = posts.indexOfFirst { it.post.id == post.id }

            try {
                if(currentUser.id != post.id) {
                    repository.deletePost(post, communityId)
                } else {
                    repository.deletePost(post)
                }

                posts.removeAt(index)

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

    fun convertPublicity() {
        viewModelScope.launch {
            try {
                repository.convertPublicity(communityId)
                getCommunityDetails()
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}