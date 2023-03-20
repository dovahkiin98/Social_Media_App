package net.inferno.socialmedia.model.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class BaseResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "err")
    val error: String?,
)