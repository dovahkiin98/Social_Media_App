package net.inferno.socialmedia.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageActivity
import com.theartofdev.edmodo.cropper.CropImageOptions
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CropImageContract :
    ActivityResultContract<Intent, CropImageView.CropResult?>() {
    override fun createIntent(
        context: Context,
        input: Intent
    ): Intent = input

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): CropImageView.CropResult? {
        val result = if (intent != null) {
            IntentCompat.getParcelableExtra(
                intent,
                CropImage.CROP_IMAGE_EXTRA_RESULT,
                CropImage.ActivityResult::class.java,
            )
        } else null

        return if (result == null || resultCode == Activity.RESULT_CANCELED) {
            null
        } else {
            result
        }
    }
}

data class CropImageContractOptions(
    val uri: Uri?,
    val cropImageOptions: CropImageOptions,
)

sealed class CropException(message: String) : Exception(message) {
    class Cancellation :
        CropException("$EXCEPTION_PREFIX cropping has been cancelled by the user") {
        internal companion object {
            private const val serialVersionUID: Long = -6896269134508601990L
        }
    }

    class FailedToLoadBitmap(uri: Uri, message: String?) :
        CropException("$EXCEPTION_PREFIX Failed to load sampled bitmap: $uri\r\n$message") {
        internal companion object {
            private const val serialVersionUID: Long = 7791142932960927332L
        }
    }

    class FailedToDecodeImage(uri: Uri) :
        CropException("$EXCEPTION_PREFIX Failed to decode image: $uri") {
        internal companion object {
            private const val serialVersionUID: Long = 3516154387706407275L
        }
    }

    internal companion object {
        private const val serialVersionUID: Long = 4933890872862969613L
        const val EXCEPTION_PREFIX = "crop:"
    }
}

fun getFilePathFromUri(context: Context, uri: Uri, uniqueName: Boolean = false): String =
    if (uri.path?.contains("file://") == true) {
        uri.path!!
    } else {
        getFileFromContentUri(context, uri, uniqueName).path
    }

fun getFileFromContentUri(context: Context, contentUri: Uri, uniqueName: Boolean): File {
    // Preparing Temp file name
    val fileExtension = getFileExtension(context, contentUri) ?: ""
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = ("temp_file_" + if (uniqueName) timeStamp else "") + ".$fileExtension"
    // Creating Temp file
    val tempFile = File(context.cacheDir, fileName)
    tempFile.createNewFile()
    // Initialize streams
    var oStream: FileOutputStream? = null
    var inputStream: InputStream? = null

    try {
        oStream = FileOutputStream(tempFile)
        inputStream = context.contentResolver.openInputStream(contentUri)

        inputStream?.let { copy(inputStream, oStream) }
        oStream.flush()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        // Close streams
        inputStream?.close()
        oStream?.close()
    }

    return tempFile
}

private fun getFileExtension(context: Context, uri: Uri): String? =
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri))
    } else {
        uri.path?.let { MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(it)).toString()) }
    }

@Throws(IOException::class)
private fun copy(source: InputStream, target: OutputStream) {
    val buf = ByteArray(8192)
    var length: Int
    while (source.read(buf).also { length = it } > 0) {
        target.write(buf, 0, length)
    }
}