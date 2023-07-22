package net.inferno.socialmedia.ui.community.requests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.PendingMember
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.view.UserAvatar
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CommunityRequestItem(
    member: PendingMember,
    onClick: (User) -> Unit,
    onAccept: (User) -> Unit,
    onDeny: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    val acceptRequest by rememberUpdatedState(onAccept)
    val denyRequest by rememberUpdatedState(onDeny)
    val user by remember { derivedStateOf { member.user } }

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
        supportingContent = {
            Text(
                buildAnnotatedString {
                    append("Sent")
                    append(" ")

                    append(DateTimeFormatter.ofPattern("dd MMM yyyy").format(member.date))
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
            FlowRow {
                PlainTooltipBox(tooltip = {
                    Text(stringResource(id = R.string.deny))
                }) {
                    IconButton(
                        onClick = {
                            denyRequest(user)
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

                PlainTooltipBox(tooltip = {
                    Text(stringResource(id = R.string.accept))
                }) {
                    IconButton(
                        onClick = {
                            acceptRequest(user)
                        },
                        modifier = Modifier
                            .tooltipTrigger()
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_accept_user),
                            contentDescription = null,
                        )
                    }
                }
            }
        },
        modifier = modifier
            .clickable {
                onClick(user)
            },
    )
}