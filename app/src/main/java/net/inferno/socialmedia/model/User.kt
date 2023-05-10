package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class User(
    @Json(name = "_id")
    val id: String,

    @Json(name = "firstName")
    val firstName: String,

    @Json(name = "lastName")
    val lastName: String,

    @Json(name = "profileImage")
    val profileImage: UserImage? = null,

    @Json(name = "coverImage")
    val coverImage: UserImage? = null,
) {
    var profileImageUrl: String? = null
    var coverImageUrl: String? = null
}