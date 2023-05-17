package net.inferno.socialmedia.data.remote

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.inferno.socialmedia.model.Comment
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

    override suspend fun getCommentDetails(
        commentId: String,
    ) = remoteService.getCommentDetails(commentId)

    override suspend fun likePost(
        postId: String,
    ) = remoteService.likePost(postId)

    override suspend fun deletePost(
        postId: String,
    ) = remoteService.deletePost(postId)

    override suspend  fun createPost(
        content: MultipartBody.Part,
        image: MultipartBody.Part?,
    ) = remoteService.createPost(content, image)

    override suspend  fun updatePost(
        postId: String,
        content: String,
    ) = remoteService.updatePost(postId, content)
    //endregion

    //region Comments
    override suspend fun getPostComments(
        postId: String,
    ) = remoteService.getPostComments(postId)

    override suspend fun deleteComment(
        commentId: String,
    ) = remoteService.deleteComment(commentId)

    override suspend fun likeComment(
        body: Map<String, String>
    ) = remoteService.likeComment(body)

    override suspend  fun createComment(
        body: Map<String, String?>
    ) = remoteService.createComment(body)

    override suspend  fun updateComment(
        commentId: String,
        content: String,
    ) = remoteService.updateComment(commentId, content)
    //endregion

    //region Community
    override suspend fun getCommunityDetails(
        communityId: String,
    ) = remoteService.getCommunityDetails(communityId)

    override suspend fun getCommunityPosts(
        communityId: String,
    ) = remoteService.getCommunityPosts(communityId)

    override suspend fun uploadCommunityCoverImage(
        image: MultipartBody.Part,
        communityId: MultipartBody.Part,
    ) = remoteService.uploadCommunityCoverImage(image, communityId)
    //endregion
}