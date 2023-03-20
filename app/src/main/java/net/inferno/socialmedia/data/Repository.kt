package net.inferno.socialmedia.data

import android.content.SharedPreferences
import androidx.core.content.edit
import net.inferno.socialmedia.data.remote.RemoteDataSource
import net.inferno.socialmedia.model.request.SignupRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val preferences: SharedPreferences,
) {
    suspend fun login(
        email: String,
        password: String,
    ) {
        val response = remoteDataSource.login(email, password)

        if(response.success) {
            val token = response.token!!

            preferences.edit {
                putString("token", token)
            }
        } else {
            throw Exception(response.error)
        }
    }

    suspend fun signup(request: SignupRequest) {
        val response = remoteDataSource.signup(request)

        if(response.success) {
            val token = response.token!!

            preferences.edit {
                putString("token", token)
            }
        } else {
            throw Exception(response.error)
        }
    }

    fun logout() {
        preferences.edit {
            remove("token")
        }
    }

    companion object {
        const val TIMEOUT = 8_000L
    }
}