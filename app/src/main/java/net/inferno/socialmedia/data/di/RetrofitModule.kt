package net.inferno.socialmedia.data.di

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.inferno.socialmedia.data.remote.SocialMediaService
import net.inferno.socialmedia.utils.LocalDateAsStringAdapter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.util.Date
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {

    @Singleton
    @Provides
    fun retrofit(
        preferences: SharedPreferences,
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(preferences.getString("url", "http://192.168.234.158:1000/api/")!!)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Singleton
    @Provides
    fun moshi(): Moshi = Moshi.Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .add(LocalDateAsStringAdapter())
        .build()

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): SocialMediaService = retrofit.create()
}