package net.inferno.socialmedia.ui.post.details

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.Comment
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.utils.toReadableText
import net.inferno.socialmedia.view.CustomModalBottomSheet
import net.inferno.socialmedia.view.MDDocument
import net.inferno.socialmedia.view.UserAvatar

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String,
    opId: String,
    modifier: Modifier = Modifier,
    level: Int = 0,
    onLiked: (Comment) -> Unit = {},
    onDisliked: (Comment) -> Unit = {},
    onReply: (Comment) -> Unit = {},
    onUserClick: (User) -> Unit = {},
    onOptionsClick: (Comment, CommentAction) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val isReply = remember(level) { level > 0 }

    val likes = remember { mutableStateListOf(*comment.likes.toTypedArray()) }
    val commentLiked = likes.contains(currentUserId)

    val dislikes = remember { mutableStateListOf(*comment.dislikes.toTypedArray()) }
    val commentDisliked = dislikes.contains(currentUserId)

    val likesScore by remember {
        derivedStateOf {
            likes.size - dislikes.size
        }
    }
    val isBadComment by remember {
        derivedStateOf {
            currentUserId != comment.user.id && (likesScore <= -1 || comment.isBadComment)
        }
    }
    var contentHidden by remember { mutableStateOf(isBadComment) }

    val commentSheetState = rememberModalBottomSheetState()
    var showCommentSheet by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min),
    ) {
        if (isReply) {
            Box(
                modifier = Modifier
                    .padding(
                        start = (8 * level).dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 8.dp,
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.onSurface,
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                    )
                    .width(1.dp)
                    .fillMaxHeight()
            )
        }

        Column(
            modifier = modifier,
        ) {
            Box(
                modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            contentHidden = false
                        },
                        onLongClick = {
                            showCommentSheet = true
                        },
                    )
                    .then(
                        if (isBadComment) Modifier.alpha(0.5f)
                        else Modifier
                    )
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            start = 8.dp,
                            top = 8.dp,
                            bottom = 8.dp,
                        ),
                    ) {
                        UserAvatar(
                            user = comment.user, onClick = {
                                onUserClick(comment.user)
                            }, modifier = Modifier.size(48.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("${comment.user.firstName} ${comment.user.lastName}",
                                color = if (opId == comment.user.id) MaterialTheme.colorScheme.primary
                                else Color.Unspecified,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    onUserClick(comment.user)
                                }
                            )

                            if (comment.createdAt != null) {
                                Text(
                                    comment.createdAt.toReadableText(),
                                )
                            }
                        }
                    }

                    MDDocument(
                        comment.content,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .then(
                                if (contentHidden) Modifier
                                    .background(Color.Black)
                                    .alpha(0f)
                                else if (isBadComment) Modifier
                                    .background(
                                        if (isSystemInDarkTheme()) Color.DarkGray
                                        else Color.LightGray
                                    )
                                else Modifier
                            )
                    )

                    Row(
                        horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                if (likes.contains(currentUserId)) {
                                    likes.remove(currentUserId)
                                } else {
                                    if (dislikes.contains(currentUserId)) {
                                        dislikes.remove(currentUserId)
                                    }
                                    likes.add(currentUserId)
                                }

                                onLiked(comment)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (commentLiked) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                        ) {
                            Icon(
                                painterResource(
                                    if (commentLiked) R.drawable.ic_thumbs_up
                                    else R.drawable.ic_thumbs_up_off
                                ), contentDescription = null
                            )
                        }

                        Text(
                            likesScore.toString(),
                            color = when {
                                likesScore > 0 -> MaterialTheme.colorScheme.primary
                                likesScore < 0 -> MaterialTheme.colorScheme.secondary
                                else -> Color.Unspecified
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .sizeIn(minWidth = 36.dp)
                                .align(Alignment.CenterVertically)
                        )

                        IconButton(
                            onClick = {
                                if (dislikes.contains(currentUserId)) {
                                    dislikes.remove(currentUserId)
                                } else {
                                    if (likes.contains(currentUserId)) {
                                        likes.remove(currentUserId)
                                    }
                                    dislikes.add(currentUserId)
                                }

                                onDisliked(comment)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (commentDisliked) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                        ) {
                            Icon(
                                painterResource(
                                    if (commentDisliked) R.drawable.ic_thumb_down
                                    else R.drawable.ic_thumb_down_off
                                ), contentDescription = null
                            )
                        }

                        TextButton(
                            onClick = {
                                onReply(comment)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_reply), contentDescription = null
                            )

                            if (!isReply) {
                                Spacer(Modifier.width(8.dp))

                                Text(stringResource(id = R.string.reply) + if (comment.replies.isEmpty()) "" else " (${comment.replies.size})")
                            }
                        }
                    }
                }
            }

            if (!contentHidden) {
                comment.replies.forEach { reply ->
                    CommentItem(
                        reply,
                        currentUserId = currentUserId,
                        opId = opId,
                        level = level + 1,
                        onUserClick = onUserClick,
                        onLiked = {
                            onLiked(it)
                        },
                        onDisliked = {
                            onDisliked(it)
                        },
                        onReply = {
                            onReply(it)
                        },
                        onOptionsClick = { it, action ->
                            onOptionsClick(it, action)
                        },
                    )
                }
            }
        }
    }

    if (showCommentSheet) {
        CustomModalBottomSheet(
            onDismissRequest = {
                showCommentSheet = false
            },
            sheetState = commentSheetState,
        ) {
            if (currentUserId == comment.user.id) {
                ListItem(leadingContent = {
                    Icon(
                        painterResource(id = R.drawable.ic_edit),
                        contentDescription = null,
                    )
                }, headlineContent = {
                    Text(stringResource(id = R.string.edit_comment))
                }, modifier = Modifier.clickable {
                    showCommentSheet = false

                    coroutineScope.launch {
                        commentSheetState.hide()
                    }

                    onOptionsClick(comment, CommentAction.Edit)
                })
            }
            ListItem(leadingContent = {
                Icon(
                    painterResource(id = R.drawable.ic_copy),
                    contentDescription = null,
                )
            }, headlineContent = {
                Text(stringResource(id = R.string.copy_comment))
            }, modifier = Modifier.clickable {
                coroutineScope.launch {
                    clipboardManager.setText(AnnotatedString(comment.content))
                    Toast.makeText(context, R.string.comment_copied, Toast.LENGTH_SHORT).show()

                    commentSheetState.hide()
                    showCommentSheet = false
                }
            })
            if (currentUserId == comment.user.id) {
                ListItem(leadingContent = {
                    Icon(
                        painterResource(id = R.drawable.ic_delete),
                        contentDescription = null,
                    )
                }, headlineContent = {
                    Text(stringResource(id = R.string.delete_comment))
                }, modifier = Modifier.clickable {
                    coroutineScope.launch {
                        showCommentSheet = false
                        commentSheetState.hide()
                    }

                    onOptionsClick(comment, CommentAction.Delete)
                })
            }
        }
    }
}

enum class CommentAction {
    Edit, Delete, ;
}