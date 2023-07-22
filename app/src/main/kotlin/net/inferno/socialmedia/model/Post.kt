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

    @Json(name = "dislikes")
    val dislikes: List<String> = listOf(),

    @Json(name = "comments")
    val comments: List<String> = listOf(),

    @Json(name = "badComments")
    val hasBadComments: Boolean = false,

    @DateString
    @Json(name = "createdAt")
    val createdAt: LocalDateTime? = null,

    @DateString
    @Json(name = "updatedAt")
    val updatedAt: LocalDateTime? = null,

    @Json(name = "category")
    val category: String? = null,

    @Json(name = "community")
    val _community: PostCommunity,
) {
    val community = if (_community.community != null) _community else null

    @JsonClass(generateAdapter = true)
    class PostImage(
        @Json(name = "_id")
        val id: String,

        @Json(name = "fileName")
        val fileName: String,
    ) {
        @Json(name = "imageUrl")
        var imageUrl: String? = null
    }

    override fun equals(other: Any?) = when (other) {
        is Post -> this.id == other.id
        else -> false
    }

    override fun hashCode() = id.toInt()

    override fun toString() = "Post(id=$id, content=$content)"

    companion object {
        fun empty(id: String) = Post(
            id = id,
            content = "",
            publisher = User.empty(""),
            _community = PostCommunity(community = null, removed = false),
        )
    }
}