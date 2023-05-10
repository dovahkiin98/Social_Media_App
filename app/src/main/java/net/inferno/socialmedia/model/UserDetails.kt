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
) : User(id, firstName, lastName, profileImage, coverImage)