package net.inferno.socialmedia.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.view.UserAvatar

@Composable
fun UserItem(
    user: User,
    onClick: ((User) -> Unit)?,
    onFollowToggled: ((User) -> Unit)?,
    modifier: Modifier = Modifier,
    following: Boolean = true,
) {
    var followingState by rememberSaveable { mutableStateOf(following) }

    val toggleFollow = {
        followingState = !followingState
        onFollowToggled!!(user)
    }

    ListItem(
        headlineContent = {
            Text(
                buildString {
                    append(user.firstName)
                    append(" ")
                    append(user.lastName)
                }
            )
        },
        leadingContent = {
            UserAvatar(
                user = user,
                modifier = Modifier
                    .size(48.dp)
            )
        },
        trailingContent = {
            if (onFollowToggled != null) {
                if (followingState) {
                    OutlinedButton(
                        onClick = toggleFollow,
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                        ),
                    ) {
                        Text(stringResource(id = R.string.unfollow))
                    }
                } else {
                    ElevatedButton(
                        onClick = toggleFollow,
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                        ),
                    ) {
                        Text(stringResource(id = R.string.follow))
                    }
                }
            }
        },
        modifier = modifier
            .clickable(enabled = onClick != null) {
                onClick!!(user)
            },
    )
}