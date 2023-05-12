package net.inferno.socialmedia.data.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SharedPreferencesModule {

    @Singleton
    @Provides
    fun sharedPreferences(
        @ApplicationContext context: Context,
    ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
}