package net.inferno.socialmedia.ui.post.form

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
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UIState
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PostFormViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val postId = savedStateHandle.get<String>("postId")

    private val _postDataState = MutableStateFlow<UIState<Post>>(UIState.Loading())
    val postDataState = _postDataState.asStateFlow()

    private val _postAddState = MutableStateFlow<UIState<Unit>?>(null)
    val postAddState = _postAddState.asStateFlow()

    val currentUser = repository.getSavedUserFlow()

    init {
        getData()
    }

    fun getData() {
        if (postId != null) {
            getPostDetails()
        }
    }

    private fun getPostDetails() {
        _postDataState.value = UIState.Loading()

        viewModelScope.launch {
            try {
                val post = repository.getPostDetails(postId!!)

                _postDataState.emit(UIState.Success(post))
            } catch (e: HttpException) {
                _postDataState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _postDataState.emit(UIState.Failure(e))
            }
        }
    }

    fun createPost(
        content: String,
        image: File?,
    ) {
        _postAddState.value = UIState.Loading()

        viewModelScope.launch {
            try {
                val post = repository.createPost(content, image)

                _postAddState.value = UIState.Success(null)
            } catch (e: HttpException) {
                _postAddState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _postAddState.emit(UIState.Failure(e))
            }

            delay(200)

            _postAddState.emit(null)
        }
    }

    fun updatePost(
        content: String,
    ) {
        _postAddState.value = UIState.Loading()

        viewModelScope.launch {
            val comment = postDataState.value.data!!.copy(content = content)

            try {
                val newPost = repository.updatePost(comment)

                _postAddState.value = UIState.Success(null)
            } catch (e: HttpException) {
                _postAddState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _postAddState.emit(UIState.Failure(e))
            }

            delay(200)

            _postAddState.emit(null)
        }
    }
}
