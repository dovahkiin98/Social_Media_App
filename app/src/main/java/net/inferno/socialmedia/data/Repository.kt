package net.inferno.socialmedia.data

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.core.net.toUri
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import net.inferno.socialmedia.data.remote.RemoteDataSource
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.model.UserDetails
import net.inferno.socialmedia.model.UserImage
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
    private val remoteDataSource: RemoteDataSource,
    private val preferencesDataStore: PreferencesDataStore,
    private val preferences: SharedPreferences = preferencesDataStore.preferences,
    private val moshi: Moshi,
) {
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

    fun getSavedUserFlow() = preferencesDataStore.savedUser.map {
        moshi.adapter(UserDetails::class.java).fromJson(it)!!
    }

    suspend fun getSavedUser() = getSavedUserFlow().first()

    private suspend fun saveUser(user: UserDetails) {
        preferencesDataStore.saveUser(
            moshi.adapter(UserDetails::class.java).toJson(user)
        )

        preferences.edit {
            putString("userId", user.id)
        }
    }

    suspend fun getUserDetails(userId: String? = null): UserDetails {
        val savedUser = getSavedUser()
        val response = makeRequest {
            remoteDataSource.getUser(userId ?: savedUser.id)
        }

        val user = response.data!!
        user.profileImageUrl = getUserProfileImage(user)
        user.coverImageUrl = getUserCoverImage(user)

        if (userId == null) {
            saveUser(user)
        }

        return user
    }

    suspend fun getUserPosts(userId: String? = null): List<Post> {
        val savedUser = getSavedUser()

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
        val savedUser = getSavedUser()

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
        val savedUser = getSavedUser()

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
        val savedUser = getSavedUser()
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
        val savedUser = getSavedUser()
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
        val savedUser = getSavedUser()
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

    fun logout() {
        preferences.edit {
            remove("token")
        }
    }
    //endregion

    //region Posts
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

    suspend fun deletePost(post: Post) {
        val response = makeRequest {
            remoteDataSource.deletePost(post.id)
        }
    }
    //endregion

    //region Images
    private fun getUserProfileImage(
        user: User,
        image: UserImage? = user.profileImage,
    ): String? {
        val url = preferences.getString("url", "http://192.168.234.158:1000/api/")!!

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
        val url = preferences.getString("url", "http://192.168.234.158:1000/api/")!!

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
        val url = preferences.getString("url", "http://192.168.234.158:1000/api/")!!

        return url.toUri().buildUpon()
            .appendPath("post-file")
            .appendPath(image.fileName)
            .appendQueryParameter("postId", post.id)
            .toString()
    }
    //endregion

    fun updateUrl(url: String) {
        preferences.edit {
            putString("url", url)
        }
    }

    companion object {
        const val TIMEOUT = 8_000L
    }

    private suspend fun <T : BaseResponse<*>> makeRequest(request: suspend () -> T): T {
        val response = request()

        if (!response.success) {
            throw Exception(response.error)
        }

        return response
    }
}