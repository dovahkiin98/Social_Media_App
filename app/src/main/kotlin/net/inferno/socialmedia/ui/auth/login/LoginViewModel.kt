package net.inferno.socialmedia.ui.auth.login

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import net.inferno.socialmedia.App
import net.inferno.socialmedia.BuildConfig
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.UIState
import retrofit2.HttpException
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: Repository,
    private val savedState: SavedStateHandle,
    preferences: SharedPreferences,
    application: Application,
) : AndroidViewModel(application) {
    val urlValue = mutableStateOf(
        preferences.getString("url", "http://192.168.234.158:1000/api/")!!
    )

    private val _uiState = MutableStateFlow<UIState<Unit>?>(null)
    val uiState = _uiState.asStateFlow()

    val emailValue = mutableStateOf(
        if (BuildConfig.DEBUG) "ahmad.sattout.ee@gmail.com"
        else preferences.getString("email", "")!!
    )
    val passwordValue = mutableStateOf(if (BuildConfig.DEBUG) "12345678" else "")

    fun login() {
        _uiState.value = UIState.Loading()

        viewModelScope.launch(Dispatchers.Main.immediate) {
            try {
                withTimeout(Repository.TIMEOUT) {
                    withContext(Dispatchers.IO) {
                        repository.login(
                            email = emailValue.value.trim(),
                            password = passwordValue.value.trim(),
                        )
                    }
                }

                _uiState.emit(UIState.Success(null))
            } catch (e: HttpException) {
                _uiState.emit(UIState.Failure(Exception(e.message())))
            } catch (e: Exception) {
                _uiState.emit(UIState.Failure(e))
            }

            delay(200)

            _uiState.emit(null)
        }
    }

    fun updateUrl(url: String) {
        viewModelScope.launch {
            repository.updateUrl(url)

            delay(1_000)

            val context = getApplication<App>().applicationContext

            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
            val componentName = intent!!.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            context.startActivity(mainIntent)

            Runtime.getRuntime().exit(0)
        }
    }
}