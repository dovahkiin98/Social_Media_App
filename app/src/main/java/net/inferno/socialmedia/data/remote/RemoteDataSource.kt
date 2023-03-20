package net.inferno.socialmedia.data.remote

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.inferno.socialmedia.model.request.SignupRequest
import net.inferno.socialmedia.model.response.LoginResponse
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class RemoteDataSource @Inject constructor(
    private val remoteService: SocialMediaService,
) : SocialMediaService {
    override suspend fun login(
        email: String,
        password: String,
    ): LoginResponse {
        return remoteService.login(email, password)
    }

    override suspend fun signup(
        request: SignupRequest,
    ): LoginResponse {
        return remoteService.signup(request)
    }
}