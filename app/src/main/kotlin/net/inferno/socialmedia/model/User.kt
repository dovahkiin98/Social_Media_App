package net.inferno.socialmedia.model

import com.google.firebase.firestore.PropertyName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class User(
    @Json(name = "_id")
    @PropertyName("_id")
    val id: String,

    @Json(name = "firstName")
    @PropertyName("firstName")
    val firstName: String,

    @Json(name = "lastName")
    @PropertyName("lastName")
    val lastName: String,

    @Json(name = "profileImage")
    @PropertyName("profileImage")
    val profileImage: UserImage? = null,

    @Json(name = "coverImage")
    val coverImage: UserImage? = null,
) {
    var profileImageUrl: String? = null
    var coverImageUrl: String? = null

    override fun toString() = "User(${id}, $firstName $lastName)"

    override fun equals(other: Any?) = when (other) {
        is User -> this.id == other.id
        else -> false
    }

    override fun hashCode() = id.hashCode()

    companion object {
        fun empty(id: String) = User(
            id = id,
            firstName = "",
            lastName = "",
        )
    }
}