package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.inferno.socialmedia.utils.DateString
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class CommunityMember(
    @Json(name = "_id")
    val id: String,

    @Json(name = "memberId")
    val user: User,

    @DateString
    @Json(name = "joinedAt")
    val joinedAt: LocalDateTime,
)