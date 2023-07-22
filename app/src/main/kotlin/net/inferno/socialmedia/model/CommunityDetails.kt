package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.inferno.socialmedia.utils.DateString
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
class CommunityDetails(
    @Json(name = "_id")
    id: String,

    @Json(name = "communityName")
    name: String,

    @Json(name = "describtion")
    val description: String,

    @Json(name = "coverImageName")
    coverImageName: String? = null,

    @Json(name = "public")
    val isPublic: Boolean,

    @Json(name = "postApproval")
    val requiresPostApproval: Boolean,

    @Json(name = "manager")
    val manager: User,

    @Json(name = "admins")
    val admins: List<User> = listOf(),

    @Json(name = "blockedUsers")
    val blockedUsers: List<User> = listOf(),

    @Json(name = "members")
    val members: List<CommunityMember> = listOf(),

    @Json(name = "waitingList")
    val pendingMembers: List<PendingMember> = listOf(),

    @Json(name = "posts")
    val posts: List<CommunityPostMini> = listOf(),

    @DateString
    @Json(name = "createdAt")
    val createdAt: LocalDateTime,
) : Community(id, name, coverImageName) {
    fun isMember(user: User) = members.any { it.user == user }

    fun isPending(user: User) = pendingMembers.any { it.user == user }

    fun isAdmin(user: User) = admins.contains(user)

    fun isManager(user: User) = manager == user

    val hasPendingPosts get() = posts.any { !it.approved }
}

@JsonClass(generateAdapter = true)
data class CommunityPostMini(
    @Json(name = "_id")
    val id: String,

    @Json(name = "postId")
    val postId: String,

    @Json(name = "approved")
    val approved: Boolean,
)