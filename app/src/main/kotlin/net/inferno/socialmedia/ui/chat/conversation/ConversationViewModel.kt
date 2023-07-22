package net.inferno.socialmedia.ui.chat.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.Conversation
import net.inferno.socialmedia.model.Message
import net.inferno.socialmedia.model.UIState
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val conversationId = savedStateHandle.get<String>("conversationId")!!

    private val _conversationDataState = MutableStateFlow<UIState<Conversation>>(UIState.Loading())
    val conversationDataState = _conversationDataState.asStateFlow()

    val currentUser = repository.getSavedUserFlow()
    val messagesFlow = repository.getMessagesFlow(conversationId)

    init {
        getConversationDetails()
    }

    fun getConversationDetails() {
        viewModelScope.launch {
            try {
                val conversation = repository.getConversation(conversationId)

                _conversationDataState.emit(UIState.Success(conversation))
            } catch (e: HttpException) {
                _conversationDataState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _conversationDataState.emit(UIState.Failure(e))
            }
        }
    }

    fun hideConversation() {
        viewModelScope.launch {
            try {
                repository.hideConversation(conversationId)
            } catch (e: Exception) {

            }
        }
    }

    fun sendMessage(
        message: String,
    ) {
        viewModelScope.launch {
            try {
                repository.sendMessage(conversationId, message.trim())
            } catch (e: Exception) {

            }
        }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            try {
                repository.deleteMessage(conversationId, message.id)
            } catch (e: Exception) {

            }
        }
    }
}