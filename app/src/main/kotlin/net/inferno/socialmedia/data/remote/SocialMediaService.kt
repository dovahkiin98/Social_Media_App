package net.inferno.socialmedia.data.remote

import net.inferno.socialmedia.model.Comment
import net.inferno.socialmedia.model.CommunityDetails
import net.inferno.socialmedia.model.CommunityPost
import net.inferno.socialmedia.model.Conversation
import net.inferno.socialmedia.model.Message
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.model.UserAction
import net.inferno.socialmedia.model.UserDetails
import net.inferno.socialmedia.model.UserImage
import net.inferno.socialmedia.model.UserNotification
import net.inferno.socialmedia.model.request.SignupRequest
import net.inferno.socialmedia.model.response.BaseResponse
import net.inferno.socialmedia.model.response.LoginResponse
import net.inferno.socialmedia.model.response.NewsFeedResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

@JvmSuppressWildcards
interface SocialMediaService {
    //region user
    @GET("user/login")
    suspend fun login(
        @Query("email") email: String,
        @Header("password") password: String,
    ): LoginResponse

    @POST("user/signup")
    suspend fun signup(
        @Body request: SignupRequest,
    ): LoginResponse

    @GET("get-posts")
    suspend fun getNewsFeed(
        @Query("limit") pageSize: Int,
    ): BaseResponse<NewsFeedResponse>

    @GET("user/notifications")
    suspend fun getNotifications(
    ): BaseResponse<List<UserNotification>>

    @GET("user/{userId}")
    suspend fun getUser(
        @Path("userId") userId: String,
    ): BaseResponse<UserDetails>

    @GET("user/interests")
    suspend fun getUserInterests(
    ): BaseResponse<Map<String, List<UserAction>>>

    @Multipart
    @PATCH("user/profile-image")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part,
        @Part style: MultipartBody.Part,
    ): BaseResponse<UserImage>

    @Multipart
    @PATCH("user/cover-image")
    suspend fun uploadCoverImage(
        @Part image: MultipartBody.Part,
        @Part style: MultipartBody.Part,
    ): BaseResponse<UserImage>

    @GET("posts/get-user-posts/{userId}")
    suspend fun getUserPosts(
        @Path("userId") userId: String,
    ): BaseResponse<List<Post>>

    @GET("followers")
    suspend fun getFollowers(
        @Query("userId") userId: String,
    ): BaseResponse<List<User>>

    @GET("followes")
    suspend fun getFollowing(
        @Query("userId") userId: String,
    ): BaseResponse<List<User>>

    @PATCH("user/followers")
    suspend fun toggleFollow(
        @Query("userId") userId: String,
    ): BaseResponse<UserDetails>
    //endregion

    //region Posts
    @GET("posts")
    suspend fun getPostDetails(
        @Query("postId") postId: String,
    ): BaseResponse<Post>

    @GET("comments")
    suspend fun getCommentDetails(
        @Query("commentId") commentId: String,
    ): BaseResponse<Comment>

    @FormUrlEncoded
    @PATCH("posts/like")
    suspend fun likePost(
        @Field("postId") postId: String,
    ): BaseResponse<Post>

    @FormUrlEncoded
    @PATCH("posts/dislike")
    suspend fun dislikePost(
        @Field("postId") postId: String,
    ): BaseResponse<Post>

    @DELETE("posts")
    suspend fun deletePost(
        @Query("postId") postId: String,
    ): BaseResponse<Any>

    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Part content: MultipartBody.Part,
        @Part image: MultipartBody.Part?,
    ): BaseResponse<Post>

    @FormUrlEncoded
    @PATCH("posts")
    suspend fun updatePost(
        @Field("postId") postId: String,
        @Field("describtion") content: String,
    ): BaseResponse<Post>
    //endregion

    //region Comments
    @GET("posts/comments")
    suspend fun getPostComments(
        @Query("postId") postId: String,
    ): BaseResponse<List<Comment>>

    @DELETE("comments")
    suspend fun deleteComment(
        @Query("commentId") commentId: String,
    ): BaseResponse<Any>

    @PATCH("comments/likes")
    suspend fun likeComment(
        @Body body: Map<String, String>,
    ): BaseResponse<Comment>

    @PATCH("comments/dislikes")
    suspend fun dislikeComment(
        @Body body: Map<String, String>,
    ): BaseResponse<Comment>

    @POST("comments")
    suspend fun createComment(
        @Body body: Map<String, String?>,
    ): BaseResponse<Comment>

    @FormUrlEncoded
    @PATCH("comments")
    suspend fun updateComment(
        @Field("commentId") commentId: String,
        @Field("content") content: String,
    ): BaseResponse<Comment>
    //endregion

    //region Community
    @FormUrlEncoded
    @POST("community")
    suspend fun createCommunity(
        @Field("communityName") communityName: String,
        @Field("describtion") description: String,
        @Field("category") category: String?,
    ): BaseResponse<Any>

    @GET("community")
    suspend fun getCommunityDetails(
        @Query("communityId") communityId: String,
    ): BaseResponse<CommunityDetails>

    @GET("community/get-posts")
    suspend fun getCommunityPosts(
        @Query("communityId") communityId: String,
    ): BaseResponse<List<CommunityPost>>

    @GET("community/unapproved/get-posts")
    suspend fun getCommunityUnapprovedPosts(
        @Query("communityId") communityId: String,
    ): BaseResponse<List<CommunityPost>>

    @POST("community/members/join")
    suspend fun sendJoinRequest(
        @Query("communityId") communityId: String,
    ): BaseResponse<CommunityDetails>

    @DELETE("community/members/join")
    suspend fun cancelJoinRequest(
        @Query("communityId") communityId: String,
        @Query("joinerId") userId: String,
    ): BaseResponse<CommunityDetails>

    @DELETE("community/members")
    suspend fun leaveCommunity(
        @Query("communityId") communityId: String,
    ): BaseResponse<CommunityDetails>

    @Multipart
    @PATCH("community/image")
    suspend fun uploadCommunityCoverImage(
        @Part image: MultipartBody.Part,
        @Part communityId: MultipartBody.Part,
    ): BaseResponse<String>

    @Multipart
    @POST("community/posts")
    suspend fun createPost(
        @Part content: MultipartBody.Part,
        @Part image: MultipartBody.Part?,
        @Query("communityId") communityId: String,
    ): BaseResponse<Post>

    @DELETE("community/posts")
    suspend fun deletePost(
        @Query("postId") postId: String,
        @Query("communityId") communityId: String,
    ): BaseResponse<Any>

    @FormUrlEncoded
    @PATCH("community/posts")
    suspend fun updatePost(
        @Field("postId") postId: String,
        @Field("describtion") content: String,
        @Query("communityId") communityId: String,
    ): BaseResponse<Post>

    @FormUrlEncoded
    @PATCH("community/posts/approve")
    suspend fun approvePost(
        @Field("postId") postId: String,
        @Field("communityId") communityId: String,
    ): BaseResponse<Any>

    @POST("community/members/role/join")
    suspend fun approveJoinRequest(
        @Query("communityId") communityId: String,
        @Query("joinerId") userId: String,
    ): BaseResponse<Any>

    @DELETE("community/members/role/join")
    suspend fun denyJoinRequest(
        @Query("communityId") communityId: String,
        @Query("joinerId") userId: String,
    ): BaseResponse<Any>

    @DELETE("community/members/role")
    suspend fun kickUser(
        @Query("communityId") communityId: String,
        @Query("joinerId") userId: String,
    ): BaseResponse<Any>

    @POST("community/admins")
    suspend fun promoteUser(
        @Query("communityId") communityId: String,
        @Body body: Map<String, Any>,
    ): BaseResponse<Any>

    @PATCH("community/admins/remove")
    suspend fun demoteUser(
        @Query("communityId") communityId: String,
        @Body body: Map<String, Any>,
    ): BaseResponse<Any>

    @PATCH("community/manager/publicity")
    suspend fun convertPublicity(
        @Query("communityId") communityId: String,
    ): BaseResponse<Any>
    //endregion

    //region Messages
    @GET("messages/conversation")
    suspend fun getConversations(
    ): BaseResponse<List<Conversation>>

    @GET("messages/conversation/details")
    suspend fun getConversation(
        @Query("conversationId") conversationId: String,
    ): BaseResponse<Conversation>

    @FormUrlEncoded
    @POST("messages/conversation")
    suspend fun startConversation(
        @Field("userId") userId: String,
    ): BaseResponse<Conversation>

    @DELETE("messages/conversation")
    suspend fun hideConversation(
        @Query("conversationId") conversationId: String,
    ): BaseResponse<Conversation>

    @GET("messages")
    suspend fun getMessages(
        @Query("conversationId") conversationId: String,
    ): BaseResponse<List<Message>>

    @FormUrlEncoded
    @POST("messages")
    suspend fun sendMessage(
        @Query("conversationId") conversationId: String,
        @Field("content") messageContent: String,
    ): BaseResponse<Conversation>

    @DELETE("messages")
    suspend fun deleteMessage(
        @Query("conversationId") conversationId: String,
        @Query("messageId") messageId: String,
    ): BaseResponse<Conversation>
    //endregion
}