package net.inferno.socialmedia.utils

import android.content.res.Configuration
import android.os.Build
import android.os.ext.SdkExtensions
import androidx.annotation.RequiresApi
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers

@RequiresApi(Build.VERSION_CODES.R)
fun isAdServicesAvailable(): Boolean {
    return SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 4 &&
            SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) >= 4
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