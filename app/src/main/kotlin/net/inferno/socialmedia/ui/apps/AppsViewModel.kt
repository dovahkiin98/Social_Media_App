package net.inferno.socialmedia.ui.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.inferno.socialmedia.model.UIState
import net.inferno.socialmedia.model.UserApp
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {
    private val packageManager = context.packageManager

    private val _uiState = MutableStateFlow<UIState<List<UserApp>>?>(null)

    val uiState = _uiState.asStateFlow()

    init {
        getUserApps()
    }

    fun getUserApps() {
        viewModelScope.launch {
            _uiState.emit(UIState.Loading())

            try {
                val packages = withContext(Dispatchers.IO) {
                    packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                }

                val userPackages = mutableListOf<UserApp>()

                for (packageInfo in packages) {
                    if (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                        userPackages += UserApp(
                            name = packageInfo.loadLabel(packageManager).toString(),
                            packageName = packageInfo.packageName,
                            drawable = packageInfo.loadIcon(packageManager),
                        )
                    }
                }

                _uiState.emit(UIState.Success(userPackages))
            } catch(e: Exception) {
                _uiState.emit(UIState.Failure(e))
            }
        }
    }

    fun intent(userApp: UserApp) = packageManager.getLaunchIntentForPackage(userApp.packageName)
}