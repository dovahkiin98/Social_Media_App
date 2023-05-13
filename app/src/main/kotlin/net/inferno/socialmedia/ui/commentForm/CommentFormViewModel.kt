package net.inferno.socialmedia.ui.commentForm

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
import net.inferno.socialmedia.model.Comment
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UIState
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class CommentFormViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val postId = savedStateHandle.get<String>("postId")
    val commentId = savedStateHandle.get<String>("commentId")

    private val _postDataState = MutableStateFlow<UIState<Post>>(UIState.Loading())
    val postDataState = _postDataState.asStateFlow()

    private val _commentDataState = MutableStateFlow<UIState<Comment>>(UIState.Loading())
    val commentDataState = _commentDataState.asStateFlow()

    private val _commentAddState = MutableStateFlow<UIState<Unit>?>(null)
    val commentAddState = _commentAddState.asStateFlow()

    val currentUser = repository.getSavedUserFlow()

    init {
        getData()
    }

    fun getData() {
        if(postId != null) {
            getPostDetails()
        } else {
            getCommentDetails()
        }
    }

    private fun getPostDetails() {
        _postDataState.value = UIState.Loading()

        viewModelScope.launch {
            try {
                val post = withContext(Dispatchers.IO) {
                    repository.getPostDetails(postId!!)
                }

                _postDataState.emit(UIState.Success(post))
            } catch (e: HttpException) {
                _postDataState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _postDataState.emit(UIState.Failure(e))
            }
        }

        if (commentId != null) {
            getCommentDetails()
        }
    }

    private fun getCommentDetails() {
        _commentDataState.value = UIState.Loading()

        viewModelScope.launch {
            try {
                val comment = withContext(Dispatchers.IO) {
                    repository.getCommentDetails(commentId!!)
                }

                _commentDataState.emit(UIState.Success(comment))
            } catch (e: HttpException) {
                _commentDataState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _commentDataState.emit(UIState.Failure(e))
            }
        }
    }

    fun postComment(
        content: String,
    ) {
        _commentAddState.value = UIState.Loading()

        viewModelScope.launch {
            try {
                val comment = withContext(Dispatchers.IO) {
                    repository.createComment(
                        postId!!,
                        content,
                        commentId,
                    )
                }

                _commentAddState.value = UIState.Success(null)
            } catch (e: HttpException) {
                _commentAddState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _commentAddState.emit(UIState.Failure(e))
            }

            delay(200)

            _commentAddState.emit(null)
        }
    }

    fun saveComment(
        content: String,
    ) {
        _commentAddState.value = UIState.Loading()

        viewModelScope.launch {
            val comment = commentDataState.value.data!!.copy(content = content)

            try {
                val newComment = withContext(Dispatchers.IO) {
                    repository.updateComment(comment)
                }

                _commentAddState.value = UIState.Success(null)
            } catch (e: HttpException) {
                _commentAddState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _commentAddState.emit(UIState.Failure(e))
            }

            delay(200)

            _commentAddState.emit(null)
        }
    }
}
