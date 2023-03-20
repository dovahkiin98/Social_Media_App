package net.inferno.socialmedia.model.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class LoginResponse(
    success: Boolean,
    error: String?,

    @Json(name = "token")
    val token: String?,

    @Json(name = "user")
    val user: Map<String, Any>?,
) : BaseResponse(
    success = success,
    error = error,
)