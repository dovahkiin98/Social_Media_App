package net.inferno.socialmedia.ui.editProfile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.inferno.socialmedia.R
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.view.BackIconButton
import net.inferno.socialmedia.view.SelectionDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileUI(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    val snackbarHostState = remember { SnackbarHostState() }

    val scrollState = rememberScrollState()

    val updatingState by viewModel.updatingState.collectAsState()
    val isLoading by remember {
        derivedStateOf {
            updatingState is UIState.Loading || updatingState is UIState.Success
        }
    }

    var firstNameValue by viewModel.firstNameValue
    var lastNameValue by viewModel.lastNameValue
    var addressValue by viewModel.addressValue

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Calendar.getInstance().apply {
            add(Calendar.YEAR, -1)
        }.timeInMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val timeNow = Calendar.getInstance().apply {
                    add(Calendar.YEAR, -1)
                }

                return utcTimeMillis < timeNow.timeInMillis
            }
        },
    )

    var isGenderMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCountriesDialog by remember { mutableStateOf(false) }

    val countries = remember { mutableListOf<String>() }

    LaunchedEffect(Unit) {
        countries.clear()

        countries.addAll(
            Locale.getISOCountries().map {
                val locale = Locale("", it)

                locale.displayCountry
            }.sorted()
        )
    }

    LaunchedEffect(updatingState) {
        if (updatingState is UIState.Failure) {
            val error = (updatingState as UIState.Failure).error!!

            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error : ${error.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.edit_profile))
                },
                navigationIcon = {
                    BackIconButton {
                        navController.popBackStack()
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                value = firstNameValue,
                onValueChange = {
                    firstNameValue = it.trim()
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
                value = lastNameValue,
                onValueChange = {
                    lastNameValue = it.trim()
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

            OutlinedTextFieldDefaults.DecorationBox(
                value = addressValue,
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
                        Text(addressValue)
                    }
                },
                interactionSource = remember { MutableInteractionSource() },
                enabled = !isLoading,
                visualTransformation = VisualTransformation.None,
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = true,
                        isError = false,
                        remember { MutableInteractionSource() },
                        OutlinedTextFieldDefaults.colors(),
                    )
                }
            )
//
//            Spacer(Modifier.height(24.dp))
//
//            OutlinedTextFieldDefaults.DecorationBox(
//                value = if (signupRequest.dateOfBirth != null)
//                    signupRequest.dateOfBirth.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
//                else "",
//                placeholder = {
//                    Text(stringResource(id = R.string.date_of_birth))
//                },
//                label = {
//                    Text(stringResource(id = R.string.date_of_birth))
//                },
//                leadingIcon = {
//                    Icon(Icons.Default.DateRange, contentDescription = null)
//                },
//                singleLine = true,
//                innerTextField = {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .heightIn(min = 24.dp)
//                            .pointerInput(Unit) {
//                                this.detectTapGestures {
//                                    showDatePicker = true
//                                }
//                            }
//                    ) {
//                        val value = if (signupRequest.dateOfBirth != null)
//                            signupRequest.dateOfBirth.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
//                        else ""
//
//                        Text(value)
//                    }
//                },
//                interactionSource = remember { MutableInteractionSource() },
//                enabled = !isLoading,
//                visualTransformation = VisualTransformation.None,
//                container = {
//                    OutlinedTextFieldDefaults.ContainerBox(
//                        enabled = true,
//                        isError = false,
//                        remember { MutableInteractionSource() },
//                        OutlinedTextFieldDefaults.colors(),
//                    )
//                }
//            )
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
                        viewModel.updateUserDetails()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(id = R.string.save_changes))
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

                                    //TODO add age change
                                }
                            },
                        ) {
                            Text(stringResource(id = R.string.confirm))
                        }
                    },
                ) {
                    DatePicker(
                        state = datePickerState,
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
                ) {
                    items(countries) {
                        ListItem(
                            headlineContent = { Text(it) },
                            modifier = Modifier.clickable {
                                //TODO Add address change

                                showCountriesDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}