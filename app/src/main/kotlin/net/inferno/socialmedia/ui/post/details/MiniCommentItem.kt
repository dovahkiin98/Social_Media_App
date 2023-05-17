package net.inferno.socialmedia.ui.post.details

import android.graphics.drawable.ColorDrawable
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.Comment
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.utils.toReadableText
import net.inferno.socialmedia.view.UserImage

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
        UserImage(
            onClick = {
            },
            modifier = Modifier
                .size(24.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(comment.user.profileImageUrl)
                    .crossfade(true)
                    .placeholder(ColorDrawable(Color.Red.toArgb()))
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
            )
        }

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