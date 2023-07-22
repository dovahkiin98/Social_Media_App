package net.inferno.socialmedia.data

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import net.inferno.socialmedia.BuildConfig
import net.inferno.socialmedia.data.di.ApplicationScope
import net.inferno.socialmedia.data.di.DefaultDispatcher
import net.inferno.socialmedia.data.remote.SocialMediaService
import net.inferno.socialmedia.model.Comment
import net.inferno.socialmedia.model.Community
import net.inferno.socialmedia.model.CommunityDetails
import net.inferno.socialmedia.model.CommunityPost
import net.inferno.socialmedia.model.Conversation
import net.inferno.socialmedia.model.Message
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.model.UserDetails
import net.inferno.socialmedia.model.UserImage
import net.inferno.socialmedia.model.UserNotification
import net.inferno.socialmedia.model.request.SignupRequest
import net.inferno.socialmedia.model.response.BaseResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val remoteDataSource: SocialMediaService,
    private val preferencesDataStore: PreferencesDataStore,
    private val preferences: SharedPreferences = preferencesDataStore.preferences,
    private val moshi: Moshi,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope,
) {
    val conversationsCollection = firestore.collection("conversations")

    //region User
    suspend fun login(
        email: String,
        password: String,
    ) {
        val response = makeRequest {
            remoteDataSource.login(email, password)
        }

        val token = response.token!!

        preferences.edit {
            putString("token", token)
            putString("email", email)
        }

        val user = response.data!!

        user.profileImageUrl = getUserProfileImage(user)
        user.coverImageUrl = getUserCoverImage(user)
        user.allCommunities.forEach {
            it.coverImageUrl = getCommunityCoverImage(it)
        }

        saveUser(user)
    }

    suspend fun signup(request: SignupRequest) {
        val response = makeRequest {
            remoteDataSource.signup(request)
        }

        val token = response.token!!

        preferences.edit {
            putString("token", token)
            putString("email", request.email)
        }

        saveUser(response.data!!)
    }

    suspend fun getNotifications(): List<UserNotification> {
        val response = makeRequest {
            remoteDataSource.getNotifications()
        }

        return response.data!!
    }

    suspend fun getNewsFeed(): List<Post> {
        val response = makeRequest {
            remoteDataSource.getNewsFeed(100)
        }

        val posts = response.data!!.posts

        for (post in posts) {
            post.files.forEach { image ->
                image.imageUrl = getPostImage(post, image)
            }
            post.publisher.profileImageUrl = getUserProfileImage(post.publisher)
        }

        return posts
    }

    fun getSavedUserFlow() = preferencesDataStore.savedUser.map {
        if (!it.isNullOrBlank()) {
            moshi.adapter(UserDetails::class.java).fromJson(it)!!
        } else {
            null
        }
    }

    fun getUserId() = preferences.getString("userId", null)!!

    private suspend fun getSavedUser() = getSavedUserFlow().first()

    private suspend fun saveUser(user: UserDetails) {
        preferencesDataStore.saveUser(
            moshi.adapter(UserDetails::class.java).toJson(user)
        )

        preferences.edit {
            putString("userId", user.id)
        }
    }

    suspend fun getUserDetails(userId: String? = null): UserDetails {
        val savedUser = getSavedUser()!!

        val response = makeRequest {
            remoteDataSource.getUser(userId ?: savedUser.id)
        }

        val user = response.data!!
        user.profileImageUrl = getUserProfileImage(user)
        user.coverImageUrl = getUserCoverImage(user)
        user.allCommunities.forEach {
            it.coverImageUrl = getCommunityCoverImage(it)
        }

        if (userId == null) {
            saveUser(user)
        }

        return user
    }

    suspend fun getUserPosts(userId: String? = null): List<Post> {
        val savedUser = getSavedUser()!!

        val response = makeRequest {
            remoteDataSource.getUserPosts(userId ?: savedUser.id)
        }

        val posts = response.data!!
        for (post in posts) {
            post.files.forEach { image ->
                image.imageUrl = getPostImage(post, image)
            }
            post.publisher.profileImageUrl = getUserProfileImage(post.publisher)
        }

        return posts
    }

    suspend fun uploadProfileImage(
        croppedImage: File,
    ): UserDetails {
        val savedUser = getSavedUser()!!

        val fileBody = croppedImage.asRequestBody("image/*".toMediaType())
        val filePart = MultipartBody.Part.createFormData(
            "image",
            croppedImage.name,
            fileBody,
        )

        val styleBody = "{\"top\":\"0px\",\"scale\":1}"
        val stylePart = MultipartBody.Part.createFormData(
            "style",
            styleBody,
        )

        val response = makeRequest {
            remoteDataSource.uploadProfileImage(
                filePart,
                stylePart,
            )
        }

        savedUser.profileImageUrl = getUserProfileImage(savedUser, response.data!!)
        saveUser(savedUser)

        return savedUser
    }

    suspend fun uploadCoverImage(
        croppedImage: File,
    ): UserDetails {
        val savedUser = getSavedUser()!!

        val fileBody = croppedImage.asRequestBody("image/*".toMediaType())
        val filePart = MultipartBody.Part.createFormData(
            "image",
            croppedImage.name,
            fileBody,
        )

        val styleBody = "{\"top\":\"0px\",\"scale\":1}"
        val stylePart = MultipartBody.Part.createFormData(
            "style",
            styleBody,
        )

        val response = makeRequest {
            remoteDataSource.uploadCoverImage(
                filePart,
                stylePart,
            )
        }

        savedUser.coverImageUrl = getUserCoverImage(savedUser, response.data!!)
        saveUser(savedUser)

        return savedUser
    }

    suspend fun getUserFollowers(userId: String?): List<User> {
        val savedUser = getSavedUser()!!
        val response = makeRequest {
            remoteDataSource.getFollowers(userId ?: savedUser.id)
        }

        val users = response.data!!
        users.forEach { user ->
            user.profileImageUrl = getUserProfileImage(user)
        }

        return users
    }

    suspend fun getUserFollowings(userId: String?): List<User> {
        val savedUser = getSavedUser()!!

        val response = makeRequest {
            remoteDataSource.getFollowing(userId ?: savedUser.id)
        }

        val users = response.data!!
        users.forEach { user ->
            user.profileImageUrl = getUserProfileImage(user)
        }

        return users
    }

    suspend fun toggleFollow(user: User) {
        val savedUser = getSavedUser()!!

        val response = makeRequest {
            remoteDataSource.toggleFollow(user.id)
        }

        if (savedUser.followes.contains(user.id)) {
            savedUser.followes.remove(user.id)
        } else {
            savedUser.followes.add(user.id)
        }

//        val currentUser = response.data!!
//        currentUser.profileImageUrl = getUserProfileImage(currentUser)

        saveUser(savedUser)
    }

    suspend fun logout() {
        preferences.edit {
            remove("token")
            remove("userId")
        }

        preferencesDataStore.saveUser("")
    }
    //endregion

    //region Posts
    suspend fun getPostDetails(postId: String): Post {
        val response = makeRequest {
            remoteDataSource.getPostDetails(postId)
        }

        val post = response.data!!

        post.publisher.profileImageUrl = getUserProfileImage(post.publisher)
        post.files.forEach {
            it.imageUrl = getPostImage(post, it)
        }
        if (post.community != null) {
            post.community.community!!.coverImageUrl =
                getCommunityCoverImage(post.community.community)
        }

        return post
    }

    suspend fun getCommentDetails(commentId: String): Comment {
        val response = makeRequest {
            remoteDataSource.getCommentDetails(commentId)
        }

        val comment = response.data!!

        comment.user.profileImageUrl = getUserProfileImage(comment.user)

        populateReplies(comment)

        return comment
    }

    private fun populateReplies(comment: Comment) {
        comment.replies.forEach { reply ->
            reply.user.profileImageUrl = getUserProfileImage(reply.user)

            if (reply.replies.isNotEmpty()) {
                populateReplies(reply)
            }
        }
    }

    suspend fun likePost(post: Post): Post {
        val response = makeRequest {
            remoteDataSource.likePost(post.id)
        }

        val newPost = response.data!!

        newPost.publisher.profileImageUrl = getUserProfileImage(newPost.publisher)
        newPost.files.forEach {
            it.imageUrl = getPostImage(newPost, it)
        }

        return newPost
    }

    suspend fun dislikePost(post: Post): Post {
        val response = makeRequest {
            remoteDataSource.dislikePost(post.id)
        }

        val newPost = response.data!!

        newPost.publisher.profileImageUrl = getUserProfileImage(newPost.publisher)
        newPost.files.forEach {
            it.imageUrl = getPostImage(newPost, it)
        }

        return newPost
    }

    suspend fun deletePost(
        post: Post,
        communityId: String? = null,
    ) {
        val response = makeRequest {
            if (communityId != null) {
                remoteDataSource.deletePost(post.id, communityId)
            } else {
                remoteDataSource.deletePost(post.id)
            }
        }
    }

    suspend fun createPost(
        content: String,
        image: File?,
        communityId: String?,
    ): Post {
        val filePart = if (image != null) {
            val body = image.asRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData(
                "image",
                image.name,
                body,
            )
        } else null

        val contentPart = MultipartBody.Part.createFormData(
            "describtion",
            content,
        )

        val response = makeRequest {
            if (communityId != null) {
                remoteDataSource.createPost(
                    content = contentPart,
                    image = filePart,
                    communityId = communityId,
                )
            } else {
                remoteDataSource.createPost(
                    content = contentPart,
                    image = filePart,
                )
            }
        }

        val newPost = response.data!!

        newPost.publisher.profileImageUrl = getUserProfileImage(newPost.publisher)

        return newPost
    }

    suspend fun updatePost(
        post: Post,
        communityId: String?,
    ): Post {
        val response = makeRequest {
            if (communityId != null) {
                remoteDataSource.updatePost(post.id, post.content, communityId)
            } else {
                remoteDataSource.updatePost(post.id, post.content)
            }
        }

        val newPost = response.data!!

        newPost.publisher.profileImageUrl = getUserProfileImage(newPost.publisher)

        return newPost
    }
    //endregion

    //region Comment
    suspend fun getPostComments(postId: String): List<Comment> {
        val response = makeRequest {
            remoteDataSource.getPostComments(postId)
        }

        val comments = response.data!!

        for (comment in comments) {
            comment.user.profileImageUrl = getUserProfileImage(comment.user)

            populateReplies(comment)
        }

        return comments
    }

    suspend fun deleteComment(comment: Comment) {
        val response = makeRequest {
            remoteDataSource.deleteComment(comment.id)
        }
    }

    suspend fun likeComment(comment: Comment): Comment {
        val response = makeRequest {
            remoteDataSource.likeComment(
                mapOf(
                    "reaction" to "like",
                    "postId" to comment.postId,
                    "commentId" to comment.id,
                )
            )
        }

        val newComment = response.data!!

        newComment.user.profileImageUrl = getUserProfileImage(newComment.user)

        populateReplies(newComment)

        return newComment
    }

    suspend fun dislikeComment(comment: Comment): Comment {
        val response = makeRequest {
            remoteDataSource.dislikeComment(
                mapOf(
                    "reaction" to "dislike",
                    "postId" to comment.postId,
                    "commentId" to comment.id,
                )
            )
        }

        val newComment = response.data!!

        newComment.user.profileImageUrl = getUserProfileImage(newComment.user)

        populateReplies(newComment)

        return newComment
    }

    suspend fun createComment(
        postId: String,
        content: String,
        commentId: String?,
    ): Comment {
        val response = makeRequest {
            remoteDataSource.createComment(
                mapOf(
                    "postId" to postId,
                    "content" to content,
                    "repliedCommentId" to commentId,
                )
            )
        }

        val newComment = response.data!!

        newComment.user.profileImageUrl = getUserProfileImage(newComment.user)

        return newComment
    }

    suspend fun updateComment(
        comment: Comment,
        content: String,
    ): Comment {
        val response = makeRequest {
            remoteDataSource.updateComment(comment.id, content)
        }

        val newComment = response.data!!

        newComment.user.profileImageUrl = getUserProfileImage(newComment.user)

        return newComment
    }
    //endregion

    //region Community
    private fun populateCommunity(community: CommunityDetails) {
        community.coverImageUrl = getCommunityCoverImage(community)
        community.pendingMembers.forEach {
            it.user.profileImageUrl = getUserProfileImage(it.user)
        }
        community.members.forEach {
            it.user.profileImageUrl = getUserProfileImage(it.user)
        }
        community.admins.forEach {
            it.profileImageUrl = getUserProfileImage(it)
        }
        community.manager.profileImageUrl = getUserProfileImage(community.manager)
    }

    suspend fun getCommunityDetails(
        communityId: String,
    ): CommunityDetails {
        val response = makeRequest {
            remoteDataSource.getCommunityDetails(communityId)
        }

        val community = response.data!!

        populateCommunity(community)

        return community
    }

    suspend fun getCommunityPosts(
        communityId: String,
    ): List<CommunityPost> {
        val response = makeRequest {
            remoteDataSource.getCommunityPosts(communityId)
        }

        val posts = response.data!!

        posts.forEach {
            it.post.files.forEach { image ->
                image.imageUrl = getPostImage(it.post, image)
            }

            it.post.publisher.profileImageUrl = getUserProfileImage(it.post.publisher)
        }

        return posts
    }

    suspend fun getCommunityUnapprovedPosts(
        communityId: String,
    ): List<CommunityPost> {
        val response = makeRequest {
            remoteDataSource.getCommunityUnapprovedPosts(communityId)
        }

        val posts = response.data!!

        posts.forEach {
            it.post.files.forEach { image ->
                image.imageUrl = getPostImage(it.post, image)
            }

            it.post.publisher.profileImageUrl = getUserProfileImage(it.post.publisher)
        }

        return posts
    }

    suspend fun sendJoinRequest(
        communityId: String,
    ): CommunityDetails {
        val response = makeRequest {
            remoteDataSource.sendJoinRequest(communityId)
        }

        val community = response.data!!

        populateCommunity(community)

        return community
    }

    suspend fun cancelJoinRequest(
        communityId: String,
    ): CommunityDetails {
        val savedUser = getSavedUser()!!

        val response = makeRequest {
            remoteDataSource.cancelJoinRequest(
                communityId,
                savedUser.id,
            )
        }

        val community = response.data!!

        populateCommunity(community)

        return community
    }

    suspend fun leaveCommunity(
        communityId: String,
    ): CommunityDetails {
        val response = makeRequest {
            remoteDataSource.leaveCommunity(communityId)
        }

        val community = response.data!!

        populateCommunity(community)

        return community
    }

    suspend fun approvePost(
        post: Post,
        communityId: String,
    ) {
        val response = makeRequest {
            remoteDataSource.approvePost(post.id, communityId)
        }
    }

    suspend fun approveJoinRequest(
        communityId: String,
        userId: String,
    ) {
        val response = makeRequest {
            remoteDataSource.approveJoinRequest(communityId, userId)
        }
    }

    suspend fun denyJoinRequest(
        communityId: String,
        userId: String,
    ) {
        val response = makeRequest {
            remoteDataSource.denyJoinRequest(communityId, userId)
        }
    }

    suspend fun kickUser(
        communityId: String,
        userId: String,
    ) {
        val response = makeRequest {
            remoteDataSource.kickUser(communityId, userId)
        }
    }

    suspend fun promoteUser(
        communityId: String,
        userId: String,
    ) {
        val response = makeRequest {
            remoteDataSource.promoteUser(communityId, mapOf("adminsId" to listOf(userId)))
        }
    }

    suspend fun demoteUser(
        communityId: String,
        userId: String,
    ) {
        val response = makeRequest {
            remoteDataSource.demoteUser(communityId, mapOf("adminsId" to listOf(userId)))
        }
    }

    suspend fun convertPublicity(communityId: String) {
        val response = makeRequest {
            remoteDataSource.convertPublicity(communityId)
        }
    }

    suspend fun uploadCommunityCoverImage(
        community: Community,
        croppedImage: File,
    ): String {
        val fileBody = croppedImage.asRequestBody("image/*".toMediaType())
        val filePart = MultipartBody.Part.createFormData(
            "image",
            croppedImage.name,
            fileBody,
        )

        val stylePart = MultipartBody.Part.createFormData(
            "communityId",
            community.id,
        )

        val response = makeRequest {
            remoteDataSource.uploadCommunityCoverImage(
                filePart,
                stylePart,
            )
        }

        community.coverImageUrl = getCommunityCoverImage(community, response.data!!)

        return response.data
    }
    //endregion

    //region Messages
    private suspend fun getConversations(): List<Conversation> {
        val response = makeRequest {
            remoteDataSource.getConversations()
        }

        val conversations = response.data!!

        conversations.forEach {
            it.users.forEach { user ->
                user.profileImageUrl = getUserProfileImage(user, user.profileImage)
            }
        }

        return conversations
    }

    fun getConversationsFlow(): Flow<List<Conversation>> {
        val userId = getUserId()

        return conversationsCollection.whereArrayContains(
            "users", userId,
        ).snapshots().map { getConversations() }
    }

    suspend fun getConversation(
        conversationId: String,
    ): Conversation {
        val response = makeRequest {
            remoteDataSource.getConversation(conversationId)
        }

        val conversation = response.data!!

        conversation.users.forEach { user ->
            user.profileImageUrl = getUserProfileImage(user, user.profileImage)
        }

        return conversation
    }

    suspend fun startConversation(
        userId: String,
    ): Conversation {
        val response = makeRequest {
            remoteDataSource.startConversation(userId)
        }

        return response.data!!
    }

    suspend fun hideConversation(
        conversationId: String,
    ) {
        val response = makeRequest {
            remoteDataSource.hideConversation(conversationId)
        }
    }

    private suspend fun getMessages(conversationId: String): List<Message> {
        val response = makeRequest {
            remoteDataSource.getMessages(conversationId)
        }

        val messages = response.data!!

        messages.forEach {
            it.sender.profileImageUrl = getUserProfileImage(it.sender, it.sender.profileImage)
        }

        return messages
    }

    fun getMessagesFlow(conversationId: String): Flow<List<Message>> {
        val userId = getUserId()

        return conversationsCollection
            .document(conversationId)
            .collection("messages")
            .snapshots().map { getMessages(conversationId) }
    }


    suspend fun sendMessage(
        conversationId: String,
        message: String,
    ) {
        val response = makeRequest {
            remoteDataSource.sendMessage(conversationId, message)
        }
    }

    suspend fun deleteMessage(
        conversationId: String,
        messageId: String,
    ) {
        val response = makeRequest {
            remoteDataSource.deleteMessage(conversationId, messageId)
        }
    }
    //endregion

    //region Images
    private fun getUserProfileImage(
        user: User,
        image: UserImage? = user.profileImage,
    ): String? {
        val url = preferences.getString("url", apiIP(IP))!!

        return if (image?.name?.isNotEmpty() == true) {
            url.toUri().buildUpon()
                .appendPath("user-profile-image")
                .appendPath(image.name)
                .appendQueryParameter("userId", user.id)
                .toString()
        } else {
            null
        }
    }

    private fun getUserCoverImage(
        user: User,
        image: UserImage? = user.coverImage,
    ): String? {
        val url = getAPIUrl()

        return if (image?.name?.isNotEmpty() == true) {
            url.toUri().buildUpon()
                .appendPath("user-cover-image")
                .appendPath(image.name)
                .appendQueryParameter("userId", user.id)
                .toString()
        } else {
            null
        }
    }

    private fun getPostImage(
        post: Post,
        image: Post.PostImage,
    ): String {
        val url = getAPIUrl()

        return url.toUri().buildUpon()
            .appendPath("post-file")
            .appendPath(image.fileName)
            .appendQueryParameter("postId", post.id)
            .toString()
    }

    private fun getCommunityCoverImage(
        community: Community,
        image: String? = null,
    ): String? {
        val url = getAPIUrl()

        if (community.coverImageName != null) {
            return url.toUri().buildUpon()
                .appendPath("community")
                .appendPath("image")
                .appendPath(image ?: community.coverImageName)
                .appendQueryParameter("communityId", community.id)
                .toString()
        }

        return null
    }
//endregion

    private fun getAPIUrl() = preferences.getString("url", apiIP(IP))!!

    fun updateIP(ip: String) {
        preferences.edit {
            putString("ip", ip)
            putString("url", apiIP(ip))
        }
    }

    companion object {
        const val TIMEOUT = 8_000L

        const val IP = "192.168.1.103"
        fun apiIP(ip: String) = "http://$ip:1000/api/"
    }

    private suspend fun <T : BaseResponse<*>> makeRequest(request: suspend () -> T): T {
        if (BuildConfig.DEBUG) {
            delay(500)
        }

        val response = withTimeout(TIMEOUT) {
            withContext(dispatcher) {
                request()
            }
        }

        if (!response.success) {
            throw Exception(response.error)
        }

        return response
    }
}