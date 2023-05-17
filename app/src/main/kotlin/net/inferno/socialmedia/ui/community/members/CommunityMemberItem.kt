package net.inferno.socialmedia.ui.community.members

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.CommunityDetails
import net.inferno.socialmedia.model.CommunityMember
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.view.UserImage
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CommunityMemberItem(
    member: CommunityMember,
    community: CommunityDetails,
    onClick: ((User) -> Unit)?,
    onPromote: ((User) -> Unit)?,
    onDemote: ((User) -> Unit)?,
    onKick: ((User) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val promoteUser by rememberUpdatedState(onPromote)
    val demoteUser by rememberUpdatedState(onDemote)
    val kickUser by rememberUpdatedState(onKick)
    val user by remember { derivedStateOf { member.user } }

    ListItem(
        headlineContent = {
            Text(
                buildString {
                    append(user.firstName)
                    append(" ")
                    append(user.lastName)
                },
                color = when {
                    community.isManager(user) -> MaterialTheme.colorScheme.primary
                    community.isAdmin(user) -> MaterialTheme.colorScheme.tertiary
                    else -> Color.Unspecified
                }
            )
        },
        supportingContent = {
            Text(
                buildAnnotatedString {
                    append("Joined")
                    append(" ")

                    append(member.joinedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                },
            )
        },
        leadingContent = {
            UserImage(
                onClick = {
                },
                modifier = Modifier
                    .size(48.dp)
            ) {
                if (user.profileImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.profileImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color.Red)
                            .fillMaxSize()
                    )
                }
            }
        },
        trailingContent = {
            FlowRow {
                if (kickUser != null) {
                    PlainTooltipBox(tooltip = {
                        Text(stringResource(id = R.string.kick))
                    }) {
                        IconButton(
                            onClick = {
                                kickUser!!(user)
                            },
                            modifier = Modifier
                                .tooltipTrigger()
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_kick_user),
                                contentDescription = null,
                            )
                        }
                    }
                }

                if (promoteUser != null) {
                    PlainTooltipBox(tooltip = {
                        Text(stringResource(id = R.string.promote))
                    }) {
                        IconButton(
                            onClick = {
                                promoteUser!!(user)
                            },
                            modifier = Modifier
                                .tooltipTrigger()
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_promote_user),
                                contentDescription = null,
                            )
                        }
                    }
                }

                if (demoteUser != null) {
                    PlainTooltipBox(tooltip = {
                        Text(stringResource(id = R.string.demote))
                    }) {
                        IconButton(
                            onClick = {
                                demoteUser!!(user)
                            },
                            modifier = Modifier
                                .tooltipTrigger()
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_demote_user),
                                contentDescription = null,
                            )
                        }
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