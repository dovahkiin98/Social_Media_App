package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class UserNotification(
    @Json(name = "_id")
    val id: String,

    @Json(name = "subject")
    val subject: NotificationSubject,

    @Json(name = "notifier")
    val notifier: String?,
) {
    override fun hashCode() = id.hashCode()
}

@JsonClass(generateAdapter = true)
class NotificationSubject(
    @Json(name = "model")
    val model: String,

    @Json(name = "action")
    val action: String,

    @Json(name = "id")
    val id: String,
)
