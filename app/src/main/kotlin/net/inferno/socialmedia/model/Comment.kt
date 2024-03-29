package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.inferno.socialmedia.utils.DateString
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class Comment(
    @Json(name = "_id")
    val id: String,

    @Json(name = "content")
    val content: String,

    @Json(name = "postId")
    val postId: String,

    @Json(name = "user")
    val user: User,

    @Json(name = "likedBy")
    val likes: List<String> = listOf(),

    @Json(name = "dislikedBy")
    val dislikes: List<String> = listOf(),

    @Json(name = "badComment")
    val isBadComment: Boolean = false,

    @Json(name = "repliedBy")
    val replies: List<Comment> = listOf(),

    @Json(name = "repliedTo")
    val repliedTo: String? = null,

    @DateString
    @Json(name = "createdAt")
    val createdAt: LocalDateTime?,

    @DateString
    @Json(name = "updatedAt")
    val updatedAt: LocalDateTime?,
) {
    override fun equals(other: Any?) = when (other) {
        is Comment -> this.id == other.id
        else -> false
    }

    override fun hashCode() = id.toInt()

    override fun toString() = "Comment($id, $content)"
}
