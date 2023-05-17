package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommunityRequest(
    @Json(name = "_id")
    val id: String,

    @Json(name = "communityId")
    val community: Community,

    @Json(name = "approved")
    val approved: Boolean,
)