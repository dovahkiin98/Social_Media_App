package net.inferno.socialmedia.ui.chat.conversation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.Message
import net.inferno.socialmedia.view.CustomModalBottomSheet
import net.inferno.socialmedia.view.UserAvatar
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MessageItem(
    message: Message,
    modifier: Modifier = Modifier,
    onOptionsClick: ((Message, MessageAction) -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()

    val sender = message.sender
    var messageHidden by remember { mutableStateOf(message.isNegative) }

    val messageOptionsSheetState = rememberModalBottomSheetState()
    var showMessageOptionsSheet by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    if (onOptionsClick != null) {
                        showMessageOptionsSheet = true
                    }
                },
            )
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        UserAvatar(
            sender,
            modifier = Modifier.size(48.dp),
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${sender.firstName} ${sender.lastName}",
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f, fill = false)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    message.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                message.content,
                modifier = Modifier.then(
                    if (messageHidden) Modifier
                        .background(Color.Black)
                        .alpha(0f)
                        .pointerInput(Unit) {
                            messageHidden = false
                        }
                    else if (!messageHidden && message.isNegative) Modifier.background(
                        if (isSystemInDarkTheme()) Color.DarkGray
                        else Color.LightGray
                    )
                    else Modifier
                ),
            )
        }
    }

    if (showMessageOptionsSheet) {
        CustomModalBottomSheet(
            onDismissRequest = {
                showMessageOptionsSheet = false
            },
            sheetState = messageOptionsSheetState,
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(id = R.drawable.ic_delete),
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(id = R.string.delete_message))
                },
                modifier = Modifier
                    .clickable {
                        onOptionsClick!!(message, MessageAction.DELETE)
                        showMessageOptionsSheet = false

                        coroutineScope.launch {
                            messageOptionsSheetState.hide()
                        }
                    }
            )
        }
    }
}