package net.inferno.socialmedia.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.inferno.socialmedia.model.UserAddress
import net.inferno.socialmedia.model.UserGender
import java.time.LocalDate
import java.util.Calendar

@JsonClass(generateAdapter = true)
data class SignupRequest(
    @Json(name = "email")
    val email: String = "",

    @Json(name = "password")
    val password: String = "",

    @Json(ignore = true)
    val confirmPassword: String = "",

    @Json(name = "firstName")
    val firstName: String = "",

    @Json(name = "lastName")
    val lastName: String = "",

    @Json(ignore = true)
    val dateOfBirth: LocalDate? = null,

    @Json(name = "age")
    val age: Int = -1,

    @Json(name = "address")
    val address: UserAddress = UserAddress(
        country = "",
    ),

    @Json(name = "sex")
    val gender: UserGender? = null,
)