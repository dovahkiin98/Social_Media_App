package net.inferno.socialmedia.ui.post.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.Comment
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UIState
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val postId = savedStateHandle.get<String>("postId")!!
    val currentUser = repository.getSavedUserFlow()

    private val _postDataState = MutableStateFlow<UIState<Post>>(UIState.Loading())
    val postDataState = _postDataState.asStateFlow()

    private val _postCommentsState = MutableStateFlow<UIState<List<Comment>>>(UIState.Loading())
    val postCommentsState = _postCommentsState.asStateFlow()

    private val _postDeletionState = MutableStateFlow<UIState<Unit>?>(null)
    val postDeletionState = _postDeletionState.asStateFlow()

    var selectedComment: Comment? = null

    private val _commentDeletionState = MutableStateFlow<UIState<Unit>?>(null)
    val commentDeletionState = _commentDeletionState.asStateFlow()

    init {
        getPostDetails()
    }

    fun getPostDetails() {
        _postDataState.value = _postDataState.value.refresh()

        viewModelScope.launch {
            delay(1_000)

            try {
                val post = repository.getPostDetails(postId)

                _postDataState.emit(UIState.Success(post))
            } catch (e: HttpException) {
                _postDataState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _postDataState.emit(UIState.Failure(e))
            }
        }

        getPostComments()
    }

    fun getPostComments() {
        _postCommentsState.value = _postCommentsState.value.refresh()

        viewModelScope.launch {
            delay(1_000)

            try {
                val comments = repository.getPostComments(postId)

                _postCommentsState.emit(UIState.Success(comments))
            } catch (e: HttpException) {
                _postCommentsState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _postCommentsState.emit(UIState.Failure(e))
            }
        }
    }

    fun likePost() {
        viewModelScope.launch {
            val post = _postDataState.value.data!!

            val newPost = repository.likePost(post)

            _postDataState.emit(UIState.Success(newPost))
        }
    }

    fun deletePost() {
        _postDeletionState.value = UIState.Loading()

        viewModelScope.launch {
            val post = _postDataState.value.data!!

            try {
                val newPost = repository.deletePost(post)

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

    fun likeComment(comment: Comment) {
        viewModelScope.launch {
            val comments = _postCommentsState.value.data!!.toMutableList()

            val newComment = repository.likeComment(comment)

            getPostComments()

//            if (newComment.repliedTo != null) {
//                val originalComment = findOriginalComment(comments, comment)!!
//                val index = comments.indexOf(originalComment)
//
//                val updatedComment = updateReplies(originalComment, newComment)
//                comments[index] = updatedComment
//
//                _postCommentsState.emit(UIState.Success(comments))
//            } else {
//                val index = comments.indexOf(comment)
//
//                comments[index] = newComment
//
//                _postCommentsState.emit(UIState.Success(comments))
//            }
        }
    }

    fun deleteComment(
        comment: Comment,
    ) {
        _commentDeletionState.value = UIState.Loading()

        viewModelScope.launch {
            try {
                val comments = postCommentsState.value.data!!

                repository.deleteComment(comment)

//                val originComment = findOriginalComment(comments, comment)

                getPostComments()

                _commentDeletionState.emit(UIState.Success(null))
            } catch (e: HttpException) {
                _commentDeletionState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _commentDeletionState.emit(UIState.Failure(e))
            }

            delay(200)

            _commentDeletionState.emit(null)
        }
    }

    private fun updateReplies(
        originalComment: Comment,
        newComment: Comment,
    ): Comment {
        val replies = originalComment.replies.toMutableList()
        val index = replies.indexOf(newComment)

        if (index >= 0) {
            replies[index] = newComment
        } else {
            replies.forEachIndexed { i, comment ->
                replies[i] = updateReplies(comment, newComment)
            }
        }

        return originalComment.copy(replies = replies)
    }

    private fun findOriginalComment(comments: List<Comment>, comment: Comment): Comment? {
        val originalComment = comments.find {
            it.id == comment.id || findOriginalComment(it.replies, comment) != null
        }

        return originalComment
    }
}