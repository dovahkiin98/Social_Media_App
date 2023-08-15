package net.inferno.socialmedia.ui.post

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.Post
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
fun PostItem(
    currentUserId: String,
    post: Post,
    communityId: String? = null,
    isApproved: Boolean = true,
    onImageClick: (Post.PostImage) -> Unit = {},
    onLiked: (Post) -> Unit = {},
    onDisliked: (Post) -> Unit = {},
    onOptionsClick: ((Post, PostAction) -> Unit)? = null,
    onUserClick: (User) -> Unit = {},
    onClick: (Post) -> Unit = {},
    modifier: Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val likes = remember { mutableStateListOf(*post.likes.toTypedArray()) }
    val dislikes = remember { mutableStateListOf(*post.dislikes.toTypedArray()) }

    val postLiked = likes.contains(currentUserId)
    val postDisliked = dislikes.contains(currentUserId)

    val likesScore by remember { derivedStateOf { likes.size - dislikes.size } }
    val isBadPost by remember {
        derivedStateOf {
            currentUserId != post.publisher.id && (likesScore <= -10 || post.hasBadComments)
        }
    }

    val postSheetState = rememberModalBottomSheetState()
    var showPostSheet by rememberSaveable { mutableStateOf(false) }

    Card(
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
        ),
        modifier = modifier
            .then(
                if (isBadPost) Modifier.alpha(0.5f)
                else Modifier
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    onClick(post)
                }
                .padding(
                    start = 8.dp,
                    top = 8.dp,
                    bottom = 8.dp,
                ),
        ) {
            UserAvatar(
                user = post.publisher,
                onClick = {
                    onUserClick(post.publisher)
                },
                modifier = Modifier
                    .size(48.dp)
            )

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    "${post.publisher.firstName} ${post.publisher.lastName}",
                    fontWeight = FontWeight.Bold,
                )

                if (post.createdAt != null) {
                    Text(
                        buildString {
                            append(post.createdAt.toReadableText())
                            if (post.category != null) {
                                append(" (${post.category})")
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                }

                if(communityId == null && post.community != null) {
                    Text(
                        post.community.community!!.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            if (onOptionsClick != null) {
                IconButton(
                    onClick = {
                        showPostSheet = true
                    }
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = null,
                    )
                }
            }
        }

        if (post.content.isNotBlank() && !isBadPost) {
            MDDocument(
                post.content,
                clip = true,
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            onClick(post)
                        },
                        onLongClick = {
                            clipboardManager.setText(
                                AnnotatedString(
                                    post.content
                                )
                            )
                            Toast
                                .makeText(
                                    context,
                                    R.string.post_copied,
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    )
                    .padding(8.dp)
            )
        }

        if (post.files.isNotEmpty()) {
            HorizontalPager(
                state = rememberPagerState { post.files.size },
                modifier = Modifier
                    .requiredHeightIn(max = 500.dp)
                    .fillMaxWidth()
            ) {
                val image = post.files[it]

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image.imageUrl!!)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onImageClick(image)
                        }
                )
            }
        }

        if(isApproved) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            )

            Row {
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

                        onLiked(post)
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor =
                        if (postLiked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                ) {
                    Icon(
                        painterResource(
                            if (postLiked) R.drawable.ic_thumbs_up
                            else R.drawable.ic_thumbs_up_off
                        ),
                        contentDescription = null
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

                        onDisliked(post)
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor =
                        if (postDisliked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                ) {
                    Icon(
                        painterResource(
                            if (postDisliked) R.drawable.ic_thumb_down
                            else R.drawable.ic_thumb_down_off
                        ),
                        contentDescription = null
                    )
                }

                Spacer(Modifier.width(24.dp))

                TextButton(
                    onClick = {
                        onClick(post)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_comment),
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(stringResource(id = R.string.comment) + if (post.comments.isEmpty()) "" else " (${post.comments.size})")
                }
            }
        }
    }

    if (showPostSheet) {
        CustomModalBottomSheet(
            onDismissRequest = {
                showPostSheet = false
            },
            sheetState = postSheetState,
        ) {
            if(currentUserId == post.publisher.id && isApproved) {
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(id = R.drawable.ic_edit),
                            contentDescription = null,
                        )
                    },
                    headlineContent = {
                        Text(stringResource(id = R.string.edit_post))
                    },
                    modifier = Modifier
                        .clickable {
                            onOptionsClick!!(post, PostAction.Edit)
                            showPostSheet = false

                            coroutineScope.launch {
                                postSheetState.hide()
                            }
                        }
                )
            }

            if(isApproved) {
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(id = R.drawable.ic_delete),
                            contentDescription = null,
                        )
                    },
                    headlineContent = {
                        Text(stringResource(id = R.string.delete_post))
                    },
                    modifier = Modifier
                        .clickable {
                            onOptionsClick!!(post, PostAction.Delete)
                            showPostSheet = false

                            coroutineScope.launch {
                                postSheetState.hide()
                            }
                        }
                )
            } else {
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(id = R.drawable.ic_thumbs_up),
                            contentDescription = null,
                        )
                    },
                    headlineContent = {
                        Text(stringResource(id = R.string.approve_post))
                    },
                    modifier = Modifier
                        .clickable {
                            onOptionsClick!!(post, PostAction.Approve)
                            showPostSheet = false

                            coroutineScope.launch {
                                postSheetState.hide()
                            }
                        }
                )
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(id = R.drawable.ic_thumb_down),
                            contentDescription = null,
                        )
                    },
                    headlineContent = {
                        Text(stringResource(id = R.string.disapprove_post))
                    },
                    modifier = Modifier
                        .clickable {
                            onOptionsClick!!(post, PostAction.Disapprove)
                            showPostSheet = false

                            coroutineScope.launch {
                                postSheetState.hide()
                            }
                        }
                )
            }
        }
    }
}

enum class PostAction {
    Delete,
    Edit,
    Approve,
    Disapprove,
    ;
}