package net.inferno.socialmedia.ui.post.details

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.inferno.socialmedia.model.Comment
import net.inferno.socialmedia.view.UserAvatar

@Composable
fun MiniCommentItem(
    comment: Comment,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(
                start = 8.dp,
                top = 8.dp,
                bottom = 8.dp,
            ),
    ) {
        UserAvatar(
            user = comment.user,
            onClick = {
            },
            modifier = Modifier
                .size(24.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            "${comment.user.firstName} ${comment.user.lastName}",
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.width(8.dp))

        Text(
            comment.content,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
        )
    }
}