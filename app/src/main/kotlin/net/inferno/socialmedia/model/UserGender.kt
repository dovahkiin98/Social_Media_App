package net.inferno.socialmedia.model

enum class UserGender {
    MALE,
    FEMALE,
    ;

    override fun toString() = super.toString().lowercase()
}