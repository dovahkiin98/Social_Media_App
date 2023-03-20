package net.inferno.socialmedia.model

import android.graphics.drawable.Drawable

data class UserApp(
    val name: String,
    val packageName: String,
    val drawable: Drawable,
)