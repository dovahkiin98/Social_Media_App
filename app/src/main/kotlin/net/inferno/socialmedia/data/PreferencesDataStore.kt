package net.inferno.socialmedia.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

    private val dataStore = context.dataStore

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    val isUserLoggedIn get() = preferences.getString("token", null) != null

    val userId get() = preferences.getString("userId", "")

    val savedUser
        get() = dataStore.data.map {
            it[SAVED_USER] ?: ""
        }

    suspend fun saveUser(user: String) = dataStore.edit {
        it[SAVED_USER] = user
    }

    companion object {
        private val SAVED_USER = stringPreferencesKey("user_data")
    }
}