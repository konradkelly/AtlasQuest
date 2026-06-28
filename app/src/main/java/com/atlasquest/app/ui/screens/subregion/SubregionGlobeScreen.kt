package com.atlasquest.app.ui.screens.subregion

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.atlasquest.app.data.model.Continent
import com.atlasquest.app.data.model.IsoNumeric
import com.atlasquest.app.data.model.Region
import org.json.JSONArray
import org.json.JSONObject

/**
 * The second map level: the same offline d3 globe as [GlobeScreen], but locked on
 * a single [continent] and coloured by its sub-regions. Tapping a sub-region
 * highlights it and pops an in-page modal (styled like the continent globe's
 * region panel) asking whether to take a quiz; confirming there calls back here
 * to start that region's quiz.
 *
 * The globe page (assets/globe/subregions.html) pulls its config — which countries
 * belong to which sub-region — from the [bridge] below, so [Region] stays the one
 * source of truth for membership.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SubregionGlobeScreen(
    continent: Continent,
    onBack: () -> Unit,
    onStartQuiz: (regionId: String) -> Unit,
) {
    val currentStartQuiz = rememberUpdatedState(onStartQuiz)

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    @Suppress("DEPRECATION")
                    settings.allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    settings.allowUniversalAccessFromFileURLs = true
                    setBackgroundColor(0xFF0A1628.toInt())

                    val mainHandler = Handler(Looper.getMainLooper())
                    addJavascriptInterface(object {
                        /** Called by the page on startup to learn this continent's sub-regions. */
                        @JavascriptInterface
                        fun getConfigJson(): String = buildConfigJson(continent)

                        /** Called when the user confirms the in-page quiz modal. */
                        @JavascriptInterface
                        fun onQuizConfirmed(regionId: String) {
                            // Validate against a real region before navigating.
                            if (Region.fromId(regionId) == null) return
                            mainHandler.post { currentStartQuiz.value(regionId) }
                        }
                    }, "AtlasQuest")

                    loadUrl("file:///android_asset/globe/subregions.html")
                }
            },
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
                .background(Color(0x80000000), shape = CircleShape),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
            )
        }
    }
}

/** Distinct fills for a continent's sub-regions (max 5 today); same palette family as the globe. */
private val PALETTE = listOf("#58CC02", "#1CB0F6", "#FFB020", "#FF4B4B", "#CE82FF", "#FF9600")

/**
 * Builds the JSON the globe page consumes:
 * `{ "subregions": [ { id, name, color, numericIds:[...], names:[...] } ] }`.
 */
private fun buildConfigJson(continent: Continent): String {
    val subregions = JSONArray()
    Region.forContinent(continent).forEachIndexed { i, region ->
        val numericIds = JSONArray().apply {
            // The 110m dataset keys features by zero-padded 3-digit ISO numeric ids
            // ("068", "032", "076"), so pad to match — otherwise every country with a
            // code below 100 (Brazil, Argentina, Bolivia, …) silently fails to render.
            region.isoCodes.forEach { iso ->
                IsoNumeric.alpha2ToNumeric[iso]?.let { put(it.padStart(3, '0')) }
            }
        }
        val names = JSONArray().apply {
            IsoNumeric.nameToRegionId.forEach { (name, regionId) -> if (regionId == region.id) put(name) }
        }
        subregions.put(
            JSONObject()
                .put("id", region.id)
                .put("name", region.displayName)
                .put("color", PALETTE[i % PALETTE.size])
                .put("numericIds", numericIds)
                .put("names", names)
        )
    }
    return JSONObject().put("subregions", subregions).toString()
}
