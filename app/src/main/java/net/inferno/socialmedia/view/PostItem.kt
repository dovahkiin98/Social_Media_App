package net.inferno.socialmedia.view

import android.graphics.drawable.ColorDrawable
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.utils.toReadableText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(
    currentUserId: String,
    post: Post,
    onImageClick: (Post.PostImage) -> Unit = {},
    onPostLiked: (Post) -> Unit = {},
    onOptionsClicked: (Post) -> Unit = {},
    onPostClick: (Post) -> Unit = {},
    modifier: Modifier,
) {
    var contentExpanded by remember { mutableStateOf(false) }
    val postLiked = post.likes.contains(currentUserId)
    val likes = post.likes

    Card(
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
        ),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    onPostClick(post)
                }
                .padding(
                    start = 8.dp,
                    top = 8.dp,
                    bottom = 8.dp,
                ),
        ) {
            UserImage(
                onClick = {

                },
                modifier = Modifier
                    .size(48.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.publisher.profileImageUrl)
                        .crossfade(true)
                        .placeholder(ColorDrawable(Color.Red.toArgb()))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                )
            }

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
                        post.createdAt.toReadableText(),
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            if(currentUserId == post.publisher.id) {
                IconButton(
                    onClick = {
                        onOptionsClicked(post)
                    }
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = null,
                    )
                }
            }
        }

        if (post.content.isNotBlank()) {
            Text(
                post.content,
                maxLines = if (contentExpanded) Int.MAX_VALUE else 3,
                overflow = if (contentExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth()
                    .clickable {
                        contentExpanded = !contentExpanded
                    }
                    .padding(8.dp)
            )
        }

        if (post.files.isNotEmpty()) {
            HorizontalPager(
                pageCount = post.files.size,
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

        Row(
            modifier = Modifier
                .animateContentSize()
                .clickable {
                    onPostClick(post)
                }
        ) {
            if (likes.isNotEmpty()) {
                Text(
                    pluralStringResource(
                        id = R.plurals.likes,
                        count = likes.size,
                        likes.size
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                )
            }

            if (post.comments.isNotEmpty()) {
                Text(
                    pluralStringResource(
                        id = R.plurals.comments,
                        count = post.comments.size,
                        post.comments.size
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                )
            }
        }

        Divider(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 8.dp)
        )

        Row {
            TextButton(
                onClick = {
                    onPostLiked(post)

                    if (likes.contains(currentUserId)) {
                        likes.remove(currentUserId)
                    } else {
                        likes.add(currentUserId)
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor =
                    if (postLiked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
            ) {
                Icon(
                    painterResource(
                        if (postLiked) R.drawable.ic_thumbs_up
                        else R.drawable.ic_thumbs_up_off
                    ),
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Text(stringResource(id = R.string.like))
            }

            TextButton(
                onClick = {

                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_comment),
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Text(stringResource(id = R.string.comment))
            }
        }
    }
}