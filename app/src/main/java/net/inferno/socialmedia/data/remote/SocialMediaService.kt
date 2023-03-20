package net.inferno.socialmedia.data.remote

import net.inferno.socialmedia.model.request.SignupRequest
import net.inferno.socialmedia.model.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SocialMediaService {
    @GET("user/login")
    suspend fun login(
        @Query("email") email: String,
        @Header("password") password: String,
    ): LoginResponse

    @POST("user/signup")
    suspend fun signup(
        @Body request: SignupRequest,
    ): LoginResponse
}