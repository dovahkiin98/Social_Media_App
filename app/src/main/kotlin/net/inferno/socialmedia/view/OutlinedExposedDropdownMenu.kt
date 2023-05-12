package net.inferno.socialmedia.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedExposedDropdownMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    value: String,
    placeholder: @Composable () -> Unit,
    label: @Composable () -> Unit,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            onExpandedChange(it)
        },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            placeholder = placeholder,
            label = label,
            leadingIcon = leadingIcon,
            trailingIcon = {
                IconButton(
                    onClick = {
                        onExpandedChange(true)
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier
                            .rotate(
                                if (expanded) 180f
                                else 0f
                            )
                    )
                }
            },
            enabled = enabled,
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            },
            content = content,
        )
    }
}