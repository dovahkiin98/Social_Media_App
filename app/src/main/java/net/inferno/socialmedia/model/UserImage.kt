package net.inferno.socialmedia.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserImage(
    @Json(name = "style")
    val style: ImageStyle,

    @Json(name = "imageName")
    val name: String,
)

@JsonClass(generateAdapter = true)
data class ImageStyle(
    @Json(name = "top")
    val top: String,

    @Json(name = "scale")
    val scale: String?,
)