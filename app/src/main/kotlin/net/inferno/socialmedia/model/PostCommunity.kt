package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class PostCommunity(
    @Json(name = "communityId")
    val community: Community?,

    @Json(name = "removed")
    val removed: Boolean,
)