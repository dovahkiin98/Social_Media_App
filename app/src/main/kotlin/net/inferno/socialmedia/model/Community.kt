package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class Community(
    @Json(name = "_id")
    val id: String,

    @Json(name = "communityName")
    val name: String,

    @Json(name = "coverImageName")
    val coverImageName: String?,
) {
    var coverImageUrl: String? = null

    override fun equals(other: Any?) = when (other) {
        is Community -> this.id == other.id

        else -> false
    }

    override fun hashCode() = id.hashCode()

    override fun toString() = "Community(id=${id}, name=${name})"
}