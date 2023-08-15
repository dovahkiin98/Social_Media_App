package net.inferno.socialmedia.data.di

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.inferno.socialmedia.data.Repository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FirebaseModule {

    @Singleton
    @Provides
    fun firestore(
        @ApplicationContext context: Context,
        preferences: SharedPreferences,
    ): FirebaseFirestore {
        val app = Firebase.initialize(
            context, FirebaseOptions.Builder()
                .setProjectId("social-media")
                .setApiKey("123")
                .setApplicationId("social-media")
                .setGcmSenderId("")
                .build()
        )

        val firestore = FirebaseFirestore.getInstance(app)

        firestore.useEmulator(preferences.getString("ip", Repository.IP)!!, 8080)

        return firestore
    }
}