package net.inferno.socialmedia.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import coil.compose.AsyncImage
import coil.request.ImageRequest
import net.inferno.socialmedia.model.Community
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.utils.colorFromCommunity
import net.inferno.socialmedia.utils.colorFromUser

@Composable
fun CommunityAvatar(
    community: Community,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                role = Role.Button,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (community.coverImageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(community.coverImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .background(colorFromCommunity(community))
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    community.name.first().toString(),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
fun UserAvatar(
    user: User,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .then(
                if (onClick != null) Modifier.clickable(
                    onClick = onClick,
                    role = Role.Button,
                )
                else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (user.profileImageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.profileImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .background(colorFromUser(user))
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("${user.firstName.first()}${user.lastName.first()}")
            }
        }
    }
}