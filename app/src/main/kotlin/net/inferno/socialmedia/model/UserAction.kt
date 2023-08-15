package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.inferno.socialmedia.utils.DateString
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
class UserAction(
    @Json(name = "_id")
    val id: String,

    @DateString
    @Json(name = "date")
    val date: LocalDateTime,

    @Json(name = "action")
    val action: String,

    @Json(name = "actionScore")
    val actionScore: Int,

    @Json(name = "score")
    val score: Int,

    @Json(ignore = true)
    var category: String = "",
)