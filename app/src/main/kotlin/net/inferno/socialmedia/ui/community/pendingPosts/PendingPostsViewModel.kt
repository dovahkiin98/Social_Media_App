package net.inferno.socialmedia.ui.community.pendingPosts

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
import net.inferno.socialmedia.model.CommunityPost
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UIState
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class PendingPostsViewModel @Inject constructor(
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

    private val _postOperationState = MutableStateFlow<UIState<Unit>?>(null)
    val postOperationState = _postOperationState.asStateFlow()

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
                val posts = repository.getCommunityUnapprovedPosts(communityId)

                _communityPostsState.emit(UIState.Success(posts))
            } catch (e: HttpException) {
                _communityPostsState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _communityPostsState.emit(UIState.Failure(e))
            }
        }
    }

    fun approvePost(post: Post) {
        _postOperationState.value = UIState.Loading()

        viewModelScope.launch {
            val posts = _communityPostsState.value.data!!.toMutableList()
            val index = posts.indexOfFirst { it.post.id == post.id }

            try {
                repository.approvePost(post, communityId)

                posts.removeAt(index)

                _communityPostsState.emit(UIState.Success(posts))
                _postOperationState.emit(UIState.Success(null))
            } catch (e: HttpException) {
                _postOperationState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _postOperationState.emit(UIState.Failure(e))
            }

            delay(200)

            _postOperationState.emit(null)
        }
    }

    fun disapprovePost(post: Post) {
        _postOperationState.value = UIState.Loading()

        viewModelScope.launch {
            val posts = _communityPostsState.value.data!!.toMutableList()
            val index = posts.indexOfFirst { it.post.id == post.id }

            try {
                repository.deletePost(post, communityId)

                posts.removeAt(index)

                _communityPostsState.emit(UIState.Success(posts))
                _postOperationState.emit(UIState.Success(null))
            } catch (e: HttpException) {
                _postOperationState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _postOperationState.emit(UIState.Failure(e))
            }

            delay(200)

            _postOperationState.emit(null)
        }
    }
}