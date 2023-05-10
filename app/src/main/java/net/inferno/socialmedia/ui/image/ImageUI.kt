package net.inferno.socialmedia.ui.image

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import androidx.navigation.NavController
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import com.ortiz.touchview.TouchImageView
import net.inferno.socialmedia.view.BackIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageUI(
    navController: NavController,
    imageUrl: String,
) {
    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets.navigationBars,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            ZoomableImage(
                imageUrl,
                modifier = Modifier
                    .fillMaxSize()
            )

            TopAppBar(
                title = {},
                navigationIcon = {
                    CompositionLocalProvider(
                        LocalContentColor provides Color.White
                    ) {
                        BackIconButton {
                            navController.popBackStack()
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.2f),
                ),
            )
        }
    }
}

@Composable
fun ZoomableImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    val buildImageRequest = { view: ImageView ->
        val request = ImageRequest.Builder(view.context)
            .data(imageUrl)
            .crossfade(true)
            .placeholder(ColorDrawable(Color.Green.toArgb()))
            .target(view)
            .build()

        view.context.imageLoader.enqueue(request)
    }

    AndroidView(
        factory = { context ->
            val view = TouchImageView(context)

            buildImageRequest(view)
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

            view
        },
        update = { view ->
            buildImageRequest(view)
        },
        modifier = modifier,
    )
}