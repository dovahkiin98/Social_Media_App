package net.inferno.socialmedia.ui.register

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.UserAddress
import net.inferno.socialmedia.model.UserGender
import net.inferno.socialmedia.model.request.SignupRequest
import net.inferno.socialmedia.theme.SocialMediaTheme
import net.inferno.socialmedia.view.OutlinedExposedDropdownMenu
import net.inferno.socialmedia.view.SelectionDialog
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegisterPhase2(
    isLoading: Boolean,
    signupRequest: SignupRequest,
    onUpdateInput: (SignupRequest) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Calendar.getInstance().apply {
            add(Calendar.YEAR, -1)
        }.timeInMillis
    )

    var isGenderMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCountriesDialog by remember { mutableStateOf(false) }

    val countries = remember {
        mutableListOf<String>()
    }

    LaunchedEffect(Unit) {
        countries.clear()

        countries.addAll(
            Locale.getISOCountries().map {
                val locale = Locale("", it)

                locale.displayCountry
            }.sorted()
        )
    }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.ime)
            .verticalScroll(scrollState)
            .padding(16.dp),
    ) {
        OutlinedTextField(
            value = signupRequest.firstName,
            onValueChange = {
                onUpdateInput(signupRequest.copy(firstName = it.trim()))
            },
            placeholder = {
                Text(stringResource(id = R.string.first_name))
            },
            label = {
                Text(stringResource(id = R.string.first_name))
            },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words,
            ),
            enabled = !isLoading,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = signupRequest.lastName,
            onValueChange = {
                onUpdateInput(signupRequest.copy(lastName = it.trim()))
            },
            placeholder = {
                Text(stringResource(id = R.string.last_name))
            },
            label = {
                Text(stringResource(id = R.string.last_name))
            },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words,
            ),
            enabled = !isLoading,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        TextFieldDefaults.OutlinedTextFieldDecorationBox(
            value = signupRequest.address.country,
            placeholder = {
                Text(stringResource(id = R.string.country))
            },
            label = {
                Text(stringResource(id = R.string.country))
            },
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = null)
            },
            singleLine = true,
            innerTextField = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 24.dp)
                        .pointerInput(Unit) {
                            this.detectTapGestures {
                                showCountriesDialog = true
                            }
                        }
                ) {
                    val value = signupRequest.address.country

                    Text(value)
                }
            },
            interactionSource = remember { MutableInteractionSource() },
            enabled = !isLoading,
            visualTransformation = VisualTransformation.None,
            container = {
                TextFieldDefaults.OutlinedBorderContainerBox(
                    enabled = true,
                    isError = false,
                    remember { MutableInteractionSource() },
                    TextFieldDefaults.outlinedTextFieldColors(),
                )
            }
        )

        Spacer(Modifier.height(24.dp))

        TextFieldDefaults.OutlinedTextFieldDecorationBox(
            value = if (signupRequest.dateOfBirth != null)
                signupRequest.dateOfBirth.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            else "",
            placeholder = {
                Text(stringResource(id = R.string.date_of_birth))
            },
            label = {
                Text(stringResource(id = R.string.date_of_birth))
            },
            leadingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = null)
            },
            singleLine = true,
            innerTextField = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 24.dp)
                        .pointerInput(Unit) {
                            this.detectTapGestures {
                                showDatePicker = true
                            }
                        }
                ) {
                    val value = if (signupRequest.dateOfBirth != null)
                        signupRequest.dateOfBirth.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    else ""

                    Text(value)
                }
            },
            interactionSource = remember { MutableInteractionSource() },
            enabled = !isLoading,
            visualTransformation = VisualTransformation.None,
            container = {
                TextFieldDefaults.OutlinedBorderContainerBox(
                    enabled = true,
                    isError = false,
                    remember { MutableInteractionSource() },
                    TextFieldDefaults.outlinedTextFieldColors(),
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        OutlinedExposedDropdownMenu(
            expanded = isGenderMenuExpanded,
            onExpandedChange = {
                isGenderMenuExpanded = it
            },
            value = signupRequest.gender?.toString()?.replaceFirstChar {
                it.titlecase()
            } ?: "",
            placeholder = {
                Text(stringResource(id = R.string.gender))
            },
            label = {
                Text(stringResource(id = R.string.gender))
            },
            leadingIcon = {
                Icon(Icons.Default.Face, contentDescription = null)
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            UserGender.values().forEach {
                ListItem(
                    headlineContent = {
                        Text(it.toString().replaceFirstChar {
                            it.titlecase()
                        })
                    },
                    modifier = Modifier.clickable {
                        onUpdateInput(
                            signupRequest.copy(
                                gender = it
                            )
                        )
                        isGenderMenuExpanded = false
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator()
            }
        } else {
            ElevatedButton(
                onClick = {
                    onDone()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(id = R.string.register))
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = {
                    showDatePicker = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false

                            if (datePickerState.selectedDateMillis != null) {
                                val selectedDate =
                                    Instant.ofEpochMilli(datePickerState.selectedDateMillis!!)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()

                                val age = ChronoUnit.YEARS.between(
                                    selectedDate.atStartOfDay(),
                                    LocalDate.now().atStartOfDay(),
                                )

                                onUpdateInput(
                                    signupRequest.copy(
                                        dateOfBirth = selectedDate,
                                        age = age.toInt(),
                                    )
                                )
                            }
                        },
                    ) {
                        Text(stringResource(id = R.string.confirm))
                    }
                },
            ) {
                DatePicker(
                    state = datePickerState,
                    dateValidator = {
                        val timeNow = Calendar.getInstance().apply {
                            add(Calendar.YEAR, -1)
                        }

                        it < timeNow.timeInMillis
                    },
                )
            }
        }

        if (showCountriesDialog) {
            SelectionDialog(
                onDismissRequest = {
                    showCountriesDialog = false
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showCountriesDialog = false
                        },
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                },
                modifier = modifier.wrapContentHeight(),
            ) {
                items(countries) {
                    ListItem(
                        headlineContent = { Text(it) },
                        modifier = Modifier.clickable {
                            onUpdateInput(
                                signupRequest.copy(
                                    address = UserAddress(country = it)
                                )
                            )
                            showCountriesDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(
    device = Devices.PIXEL_4,
    showSystemUi = true,
    name = "Light",
)
@Preview(
    device = Devices.PIXEL_4,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark",
)
@Composable
fun RegisterPhase2Preview() {
    SocialMediaTheme {
        Scaffold { paddingValues ->
            RegisterPhase2(
                isLoading = false,
                signupRequest = SignupRequest(
                    firstName = "Ahmad",
                    lastName = "Sattout",
                    address = UserAddress(
                        country = "Syria",
                    ),
                    dateOfBirth = LocalDate.of(
                        1996,
                        Month.NOVEMBER,
                        24,
                    ),
                    gender = UserGender.MALE,
                ),
                onUpdateInput = {

                },
                onDone = {

                },
                modifier = Modifier
                    .padding(paddingValues)
            )
        }
    }
}