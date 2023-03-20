package net.inferno.socialmedia.data.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.inferno.socialmedia.BuildConfig
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class OkHttpModule {

    @Singleton
    @Provides
    fun okhttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        requestInterceptor: Interceptor,
    ) = OkHttpClient.Builder().run {
        // sets the timeout for the entire request. default is 0 ( no timeout )
        callTimeout(0, TimeUnit.SECONDS)
        // sets the timeout for each request stage. default is 10 seconds
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)

        cache(cache)

        addInterceptor(requestInterceptor)

        // logs requests and responses
        if (BuildConfig.DEBUG) {
            addInterceptor(loggingInterceptor)
        }

        build()
    }

    @Singleton
    @Provides
    fun cache(
        @ApplicationContext context: Context,
    ): Cache {
        val cacheDir = File(context.cacheDir, "cache")
        val cacheSize = 10L * 1024 * 1024

        return Cache(cacheDir, cacheSize)
    }

    @Singleton
    @Provides
    fun loggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Singleton
    @Provides
    fun requestInterceptor(
        preferences: SharedPreferences,
    ) = Interceptor {
        val token = preferences.getString("token", null)

        val request = it.request()

        val requestBuilder = it.request().newBuilder().apply {
            if (token != null) {
                addHeader("Authorization", "Bearer $token")
            }

            addHeader("Accept", "application/json; charset=utf-8")
            if (request.method != "GET") addHeader("Content-Type", "application/json; charset=utf-8")
        }

        val response = it.proceed(requestBuilder.build())

        if (response.isSuccessful) {
            response
        } else {
            val body = response.peekBody(Long.MAX_VALUE).string()

            if (BuildConfig.DEBUG) {
                println(body)
            }

            val error = try {
                if (response.headers["Content-Type"] == "application/json; charset=utf-8") {
                    JSONObject(body).getString("err")
                } else {
                    response.message
                }
            } catch (e: Exception) {
                response.message
            }

            response.newBuilder()
                .message(error)
                .build()
        }
    }
}