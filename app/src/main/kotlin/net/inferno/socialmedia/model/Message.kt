package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.inferno.socialmedia.utils.DateString
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
class Message(
    @Json(name = "_id")
    val id: String,

    @Json(name = "content")
    val content: String,

    @DateString
    @Json(name = "date")
    val date: LocalDateTime,

    @Json(name = "sender")
    val sender: User,

    @Json(name = "isNegative")
    val isNegative: Boolean = false,
)