package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class UserDetails(
    @Json(name = "_id")
    id: String,

    @Json(name = "firstName")
    firstName: String,

    @Json(name = "lastName")
    lastName: String,

    @Json(name = "profileImage")
    profileImage: UserImage? = null,

    @Json(name = "coverImage")
    coverImage: UserImage? = null,

    @Json(name = "age")
    val age: Int,

    @Json(name = "email")
    val email: String,

    @Json(name = "phoneNumber")
    val phoneNumber: String = "",

    @Json(name = "followes")
    val followes: MutableList<String> = mutableListOf(),

    @Json(name = "followers")
    val followers: MutableList<String> = mutableListOf(),

    @Json(name = "posts")
    val posts: List<String> = listOf(),

    @Json(name = "adminedCommunities")
    val adminedCommunities: List<Community> = listOf(),

    @Json(name = "managedCommunities")
    val managedCommunities: List<Community> = listOf(),

    @Json(name = "blockedCommunities")
    val blockedCommunities: List<Community> = listOf(),

    @Json(name = "communities")
    val communities: List<CommunityRequest> = listOf(),
) : User(id, firstName, lastName, profileImage, coverImage) {

    @Json(ignore = true)
    val allCommunities = (managedCommunities + adminedCommunities + communities
        .map { it.community }).distinct()

    fun isAdmin(community: Community) = adminedCommunities.contains(community)

    fun isManager(community: Community) = managedCommunities.contains(community)

    override fun equals(other: Any?) = when (other) {
        is User -> this.id == other.id
        else -> false
    }

    override fun hashCode() = id.hashCode()
}