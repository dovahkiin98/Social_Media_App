package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommunityPost(
    @Json(name = "_id")
    val id: String,

    @Json(name = "postId")
    val post: Post,

    @Json(name = "approved")
    val approved: Boolean,
)