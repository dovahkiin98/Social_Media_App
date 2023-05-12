package net.inferno.socialmedia.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import net.inferno.socialmedia.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SelectionDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    content: LazyListScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        modifier = modifier.wrapContentHeight(),
    ) {
        Surface(
            modifier = Modifier
                .heightIn(max = 568.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
            ) {
                Spacer(Modifier.height(24.dp))

                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface
                ) {
                    ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                        Box(
                            // Align the title to the center when an icon is present.
                            Modifier
                                .padding(horizontal = 24.dp)
                                .padding(PaddingValues(bottom = 16.dp))
                                .align(Alignment.Start)
                        ) {
                            Text(stringResource(id = R.string.country))
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f),
                    content = content,
                )

                if(dismissButton != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(PaddingValues(bottom = 8.dp, end = 6.dp))
                    ) {
                        CompositionLocalProvider(
                            LocalContentColor provides MaterialTheme.colorScheme.primary
                        ) {
                            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                                FlowRow {
                                    dismissButton()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}