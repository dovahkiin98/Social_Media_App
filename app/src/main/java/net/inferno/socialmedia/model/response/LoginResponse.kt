package net.inferno.socialmedia.model.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.inferno.socialmedia.model.User
import net.inferno.socialmedia.model.UserDetails

@JsonClass(generateAdapter = true)
class LoginResponse(
    success: Boolean,
    error: String?,
    data: UserDetails?,

    @Json(name = "token")
    val token: String?,
) : BaseResponse<UserDetails>(
    success = success,
    error = error,
    data = data,
)