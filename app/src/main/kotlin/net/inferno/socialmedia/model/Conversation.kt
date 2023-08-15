package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.Flow
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

    @Json(name = "lastMessage")
    val lastMessage: Message?,
) {
    fun getOtherUser(userId: String) = users.first { it.id != userId }
}