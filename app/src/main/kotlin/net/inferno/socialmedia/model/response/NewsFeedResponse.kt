package net.inferno.socialmedia.model.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.inferno.socialmedia.model.Post

@JsonClass(generateAdapter = true)
class NewsFeedResponse(
    @Json(name = "posts")
    val posts: List<Post>,
)