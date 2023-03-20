package net.inferno.socialmedia.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager

class PreferencesDataStore(
    context: Context,
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

    private val dataStore = context.dataStore

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    val isUserLoggedIn get() = preferences.getString("token", null) != null
}