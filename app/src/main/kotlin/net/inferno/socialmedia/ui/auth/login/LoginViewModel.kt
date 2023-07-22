package net.inferno.socialmedia.ui.auth.login

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.inferno.socialmedia.App
import net.inferno.socialmedia.BuildConfig
import net.inferno.socialmedia.R
import net.inferno.socialmedia.data.Repository
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.ui.main.MainActivity
import net.inferno.socialmedia.ui.main.Routes
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
        preferences.getString("ip", Repository.IP)!!
    )

    private val _uiState = MutableStateFlow<UIState<Unit>?>(null)
    val uiState = _uiState.asStateFlow()

    val emailValue = mutableStateOf(
        if (BuildConfig.DEBUG) "admin@email.com"
        else preferences.getString("email", "")!!
    )
    val passwordValue = mutableStateOf(if (BuildConfig.DEBUG) "12345678" else "")

    fun login() {
        _uiState.value = UIState.Loading()

        viewModelScope.launch(Dispatchers.Main.immediate) {
            try {
                repository.login(
                    email = emailValue.value.trim(),
                    password = passwordValue.value.trim(),
                )

                createShortcuts()

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

    fun updateIP(ip: String) {
        viewModelScope.launch {
            repository.updateIP(ip)

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

    private fun createShortcuts() {
        val conversationsShortcut = ShortcutInfoCompat.Builder(getApplication(), "id_conversations")
            .setShortLabel("Conversations")
            .setLongLabel("Open Conversations List")
            .setIcon(
                IconCompat.createWithResource(
                    getApplication(),
                    R.drawable.ic_message,
                )
            )
            .setIntent(
                Intent(
                    getApplication(), MainActivity::class.java,
                ).setAction(Intent.ACTION_VIEW)
                    .putExtra("start", Routes.CONVERSATIONS)
            )
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(getApplication(), conversationsShortcut)

        val profileShortcut = ShortcutInfoCompat.Builder(getApplication(), "id_profile")
            .setShortLabel("Profile")
            .setLongLabel("Open Profile")
            .setIcon(
                IconCompat.createWithResource(
                    getApplication(),
                    R.drawable.ic_person,
                )
            )
            .setIntent(
                Intent(
                    getApplication(), MainActivity::class.java,
                ).setAction(Intent.ACTION_VIEW)
                    .putExtra("start", Routes.USER_PROFILE)
            )
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(getApplication(), profileShortcut)

        val newPostShortcut = ShortcutInfoCompat.Builder(getApplication(), "id_new_post")
            .setShortLabel("Create Post")
            .setLongLabel("Create a new post")
            .setIcon(
                IconCompat.createWithResource(
                    getApplication(),
                    R.drawable.ic_add,
                )
            )
            .setIntent(
                Intent(
                    getApplication(), MainActivity::class.java,
                ).setAction(Intent.ACTION_VIEW)
                    .putExtra("start", Routes.ADD_POST)
            )
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(getApplication(), newPostShortcut)
    }
}