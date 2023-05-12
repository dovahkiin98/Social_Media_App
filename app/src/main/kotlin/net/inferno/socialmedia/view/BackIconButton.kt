package net.inferno.socialmedia.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.inferno.socialmedia.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    PlainTooltipBox(tooltip = {
        Text(stringResource(id = R.string.back))
    }) {
        IconButton(
            onClick = onClick,
            modifier = modifier
                .tooltipTrigger()
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = stringResource(id = R.string.back),
            )
        }
    }
}