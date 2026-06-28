package com.atlasquest.app.ui.screens.globe

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.atlasquest.app.data.model.Continent
import com.atlasquest.app.data.model.Region

/**
 * The interactive 3D globe, rendered by a bundled offline WebView (assets/globe/).
 * When the user picks a continent and taps "Explore region", the page calls
 * AtlasQuest.onContinentSelected(name); we route that to the native sub-region map.
 * A single-country region like Eurasia instead calls AtlasQuest.onStartQuiz(id),
 * which jumps straight to that region's quiz.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GlobeScreen(
    onContinentSelected: (Continent) -> Unit,
    onStartQuiz: (regionId: String) -> Unit,
) {
    // Keep the latest callbacks so the long-lived WebView/bridge always calls through.
    val currentCallback = rememberUpdatedState(onContinentSelected)
    val currentStartQuiz = rememberUpdatedState(onStartQuiz)

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                // Allow the page to fetch() the bundled JSON from file:///android_asset.
                settings.allowFileAccess = true
                @Suppress("DEPRECATION")
                settings.allowFileAccessFromFileURLs = true
                @Suppress("DEPRECATION")
                settings.allowUniversalAccessFromFileURLs = true
                setBackgroundColor(0xFF0A1628.toInt())

                val mainHandler = Handler(Looper.getMainLooper())
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onContinentSelected(name: String) {
                        // JS bridge calls arrive off the main thread; hop back before navigating.
                        val continent = Continent.fromDisplayName(name) ?: return
                        mainHandler.post { currentCallback.value(continent) }
                    }

                    /** A single-country region (e.g. Eurasia) jumps straight to its quiz. */
                    @JavascriptInterface
                    fun onStartQuiz(regionId: String) {
                        if (Region.fromId(regionId) == null) return
                        mainHandler.post { currentStartQuiz.value(regionId) }
                    }
                }, "AtlasQuest")

                loadUrl("file:///android_asset/globe/index.html")
            }
        },
    )
}
