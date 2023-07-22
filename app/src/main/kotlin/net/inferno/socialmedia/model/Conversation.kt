package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.inferno.socialmedia.utils.DateString
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
class Conversation(
    @Json(name = "_id")
    val id: String,

    @DateString
    @Json(name = "lastUpdated")
    val lastUpdated: LocalDateTime,

    @Json(name = "users")
    val users: List<User>,
) {
    fun getOtherUser(userId: String) = users.first { it.id != userId }
}