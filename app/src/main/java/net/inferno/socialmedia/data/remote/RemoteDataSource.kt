package net.inferno.socialmedia.data.remote

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.UserImage
import net.inferno.socialmedia.model.request.SignupRequest
import net.inferno.socialmedia.model.response.BaseResponse
import okhttp3.MultipartBody
import java.io.File
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class RemoteDataSource @Inject constructor(
    private val remoteService: SocialMediaService,
) : SocialMediaService {
    override suspend fun login(
        email: String,
        password: String,
    ) = remoteService.login(email, password)

    override suspend fun signup(
        request: SignupRequest,
    ) = remoteService.signup(request)

    override suspend fun getUser(
        userId: String,
    ) = remoteService.getUser(userId)

    override suspend fun uploadProfileImage(
        image: MultipartBody.Part,
        style: MultipartBody.Part,
    ) = remoteService.uploadProfileImage(image, style)

    override suspend fun uploadCoverImage(
        image: MultipartBody.Part,
        style: MultipartBody.Part,
    ) = remoteService.uploadCoverImage(image, style)

    override suspend fun getUserPosts(
        userId: String,
    ) = remoteService.getUserPosts(userId)

    override suspend fun getFollowers(
        userId: String,
    ) = remoteService.getFollowers(userId)

    override suspend fun getFollowing(
        userId: String,
    ) = remoteService.getFollowing(userId)

    override suspend fun toggleFollow(
        userId: String,
    ) = remoteService.toggleFollow(userId)

    //region Posts
    override suspend fun getPostDetails(
        postId: String,
    ) = remoteService.getPostDetails(postId)

    override suspend fun likePost(
        postId: String,
    ) = remoteService.likePost(postId)

    override suspend fun deletePost(
        postId: String,
    ) = remoteService.deletePost(postId)

    override suspend fun getPostComments(
        postId: String,
    ) = remoteService.getPostComments(postId)

    override suspend fun likeComment(
        body: Map<String, String,>
    ) = remoteService.likeComment(body)
    //endregion
}