package net.inferno.socialmedia.ui.chat.conversationsList

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.Conversation
import javax.inject.Inject

@HiltViewModel
class ConversationsListViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val currentUser = repository.getSavedUserFlow()
    val conversationsFlow = repository.getConversationsFlow()

    fun hideConversation(conversation: Conversation) {
        viewModelScope.launch {
            repository.hideConversation(conversation.id)
        }
    }
}