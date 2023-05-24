package net.inferno.socialmedia.ui.main

import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresExtension
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.privacysandbox.ads.adservices.topics.GetTopicsRequest
import androidx.privacysandbox.ads.adservices.topics.TopicsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.inferno.socialmedia.data.PreferencesDataStore
import net.inferno.socialmedia.theme.SocialMediaTheme
import net.inferno.socialmedia.utils.isAdServicesAvailable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preferences: PreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SocialMediaTheme {
                MainActivityUI(
                    preferences = preferences,
                )
            }
        }

        if (Build.VERSION.SDK_INT >= 30 && isAdServicesAvailable()) {
            topicGetter()
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 4)
    @RequiresExtension(extension = SdkExtensions.AD_SERVICES, version = 4)
    private fun topicGetter() {
        val topicsManager = TopicsManager.obtain(this)

        if (topicsManager != null) {
            val topicsRequestBuilder = GetTopicsRequest.Builder()
                .setAdsSdkName(baseContext.packageName)

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        topicsManager.getTopics(topicsRequestBuilder.build())
                    }

                    val topics = response.topics

                    for (i in topics.indices) {
                        Log.i("Topic", topics[i].topicId.toString())
                    }

                    if (topics.isEmpty()) {
                        Log.i("Topic", "Returned Empty")
                    }
                } catch (e: Exception) {
                    Log.i("Topic", e.toString())
                }
            }
        } else {
            Log.i("Topic", "Topics Manager is null")
        }
    }
}
