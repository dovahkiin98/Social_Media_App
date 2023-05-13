package net.inferno.socialmedia.ui.cropImage

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageOptions
import com.theartofdev.edmodo.cropper.CropImageView
import net.inferno.socialmedia.R
import net.inferno.socialmedia.theme.SocialMediaTheme
import net.inferno.socialmedia.view.BackIconButton
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
class CropImageActivity : ComponentActivity() {
    private lateinit var cropImageView: CropImageView


    private lateinit var options: CropImageOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val bundle = intent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE)!!
        options =
            bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS)!!

        setContent {
            SocialMediaTheme(
                darkTheme = true
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                            },
                            navigationIcon = {
                                BackIconButton {
                                    onBackPressedDispatcher.onBackPressed()
                                }
                            },
                            actions = {
                                TextButton(
                                    onClick = {
                                        val outputUri = getOutputUri()

                                        cropImageView.saveCroppedImageAsync(
                                            outputUri,
                                            options.outputCompressFormat,
                                            options.outputCompressQuality,
                                            options.outputRequestWidth,
                                            options.outputRequestHeight,
                                            options.outputRequestSizeOptions,
                                        )
                                    }
                                ) {
                                    Text(stringResource(id = R.string.crop))
                                }
                            },
                        )
                    },
                ) { paddingValues ->
                    AndroidView(
                        factory = { context ->
                            val cropImageUri =
                                bundle.getParcelable<Uri>(CropImage.CROP_IMAGE_EXTRA_SOURCE)

                            cropImageView = CropImageView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )

                                setOnCropImageCompleteListener { _, result ->
                                    setResult(result.uri, result.error, result.sampleSize)
                                }

                                setOnSetImageUriCompleteListener { view, _, _ ->
                                    if (options.initialCropWindowRectangle != null) {
                                        view.cropRect = options.initialCropWindowRectangle
                                    }

                                    if (options.initialRotation > -1) {
                                        view.rotatedDegrees = options.initialRotation
                                    }

                                    view.setAspectRatio(options.aspectRatioX, options.aspectRatioY)
                                }

                                setImageUriAsync(cropImageUri)
                            }

                            cropImageView
                        },
                        update = {

                        },
                        modifier = Modifier
                            .padding(paddingValues)
                    )
                }
            }
        }
    }

    private fun getOutputUri(): Uri {
        var outputUri: Uri? = options.outputUri

        if (outputUri == null || outputUri == Uri.EMPTY) {
            outputUri = try {
                val ext = when (options.outputCompressFormat) {
                    Bitmap.CompressFormat.JPEG -> ".jpg"
                    Bitmap.CompressFormat.PNG -> ".png"
                    else -> ".webp"
                }

                Uri.fromFile(
                    File.createTempFile(
                        "cropped", ext,
                        cacheDir,
                    )
                )
            } catch (e: IOException) {
                throw RuntimeException("Failed to create temp file for output image", e)
            }
        }
        return outputUri!!
    }

    private fun setResult(uri: Uri?, error: Exception?, sampleSize: Int) {
        val resultCode =
            if (error == null) RESULT_OK else CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE
        setResult(resultCode, getResultIntent(uri, error, sampleSize))
        finish()
    }

    private fun getResultIntent(
        uri: Uri?,
        error: Exception?,
        sampleSize: Int,
    ): Intent {
        val result = CropImage.ActivityResult(
            cropImageView.imageUri,
            uri,
            error,
            cropImageView.cropPoints,
            cropImageView.cropRect,
            cropImageView.rotatedDegrees,
            cropImageView.wholeImageRect,
            sampleSize,
        )


        return Intent().apply {
            putExtras(intent)
            putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result)
        }
    }
}