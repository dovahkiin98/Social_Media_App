package net.inferno.socialmedia.utils

import android.os.Build
import android.os.ext.SdkExtensions
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.R)
fun isAdServicesAvailable(): Boolean {
    return SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 4 &&
            SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) >= 4
}