package net.inferno.socialmedia.utils

import android.content.res.Configuration
import android.os.Build
import android.os.ext.SdkExtensions
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import net.inferno.socialmedia.model.Community
import net.inferno.socialmedia.model.User
import kotlin.math.absoluteValue

@RequiresApi(Build.VERSION_CODES.R)
fun isAdServicesAvailable(): Boolean {
    return SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 4 &&
            SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) >= 4
}

fun colorFromUser(user: User) = colorFromName("${user.firstName} ${user.lastName}")

fun colorFromCommunity(community: Community) = colorFromName(community.name)

private fun colorFromName(name: String): Color {
    var hash = 0
    for (c in name.chars()) {
        hash = c + ((hash shl 5) - hash)
    }

    return Color.hsl(
        (hash % 360).toFloat().absoluteValue,
        0.5f,
        0.5f,
    )
}

@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_4,
)
@Preview(
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_MASK and Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL_4,
)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION,
)
annotation class CustomPreview