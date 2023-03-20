package net.inferno.socialmedia.data.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.inferno.socialmedia.data.remote.SocialMediaService
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
        okHttpClient: OkHttpClient,
        moshi: MoshiConverterFactory,
    ) = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("http://192.168.234.158:1000/api/")
        .addConverterFactory(moshi)
        .build()

    @Singleton
    @Provides
    fun moshi(): MoshiConverterFactory {
        val moshi = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .build()

        return MoshiConverterFactory.create(moshi)
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): SocialMediaService = retrofit.create()
}