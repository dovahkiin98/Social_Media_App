package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.inferno.socialmedia.utils.DateString
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class Post(
    @Json(name = "_id")
    val id: String,

    @Json(name = "describtion")
    val content: String,

    @Json(name = "publisher")
    val publisher: User,

    @Json(name = "files")
    val files: List<PostImage> = listOf(),

    @Json(name = "likes")
    val likes: List<String> = listOf(),

    @Json(name = "comments")
    val comments: List<String> = listOf(),

    @DateString
    @Json(name = "createdAt")
    val createdAt: LocalDateTime? = null,

    @DateString
    @Json(name = "updatedAt")
    val updatedAt: LocalDateTime? = null,
) {
    @JsonClass(generateAdapter = true)
    class PostImage(
        @Json(name = "_id")
        val id: String,

        @Json(name = "fileName")
        val fileName: String,
    ) {
        var imageUrl: String? = null
    }

    override fun equals(other: Any?) = when(other) {
        is Post -> this.id == other.id
        else -> false
    }

    override fun hashCode() = id.toInt()
}