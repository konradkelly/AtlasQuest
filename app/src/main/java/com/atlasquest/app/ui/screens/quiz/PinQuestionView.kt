package com.atlasquest.app.ui.screens.quiz

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

/**
 * Owns the pin-globe WebView for the lifetime of a quiz. Every question is a
 * pin drop, so the page is loaded once up front and reused across questions
 * (re-creating it costs a few hundred ms of load each time); QuizScreen
 * destroys it on dispose.
 *
 * `evaluateJavascript` before the page finishes loading is a silent no-op, so
 * calls are queued until the page reports `onPageReady` over the bridge.
 */
class PinGlobeController(context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var pageReady = false
    private val pending = mutableListOf<String>()

    /** Reassigned per composition so the bridge always calls the live handlers. */
    var onPinPlaced: () -> Unit = {}
    var onPinConfirmed: (lat: Double, lng: Double, countryId: String, countryName: String) -> Unit =
        { _, _, _, _ -> }

    @SuppressLint("SetJavaScriptEnabled")
    val webView: WebView = object : WebView(context) {
        override fun onTouchEvent(event: MotionEvent): Boolean {
            // The quiz column is verticalScroll; the globe's drag-to-rotate must
            // win gesture arbitration or every rotation scrolls the quiz instead.
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            return super.onTouchEvent(event)
        }
    }.apply {
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        @Suppress("DEPRECATION")
        settings.allowFileAccessFromFileURLs = true
        setBackgroundColor(0xFF0A1628.toInt())

        addJavascriptInterface(object {
            @JavascriptInterface
            fun onPageReady() {
                mainHandler.post {
                    pageReady = true
                    pending.forEach { evaluateJavascript(it, null) }
                    pending.clear()
                }
            }

            @JavascriptInterface
            fun onPinPlaced() {
                mainHandler.post { this@PinGlobeController.onPinPlaced() }
            }

            @JavascriptInterface
            fun onPinConfirmed(lat: Double, lng: Double, countryId: String, countryName: String) {
                // Bridge calls arrive off the main thread; hop back before touching state.
                mainHandler.post {
                    this@PinGlobeController.onPinConfirmed(lat, lng, countryId, countryName)
                }
            }
        }, "AtlasQuest")

        loadUrl("file:///android_asset/globe/pin.html")
    }

    private fun call(js: String) {
        if (pageReady) webView.evaluateJavascript(js, null) else pending += js
    }

    fun loadQuestion() {
        call("AtlasPin.loadQuestion()")
    }

    /** Locks in the current pin; the page answers with onPinConfirmed. */
    fun confirm() {
        call("AtlasPin.confirm()")
    }

    fun zoomIn() = call("AtlasPin.zoomBy(1.6)")
    fun zoomOut() = call("AtlasPin.zoomBy(0.625)")

    fun reveal(answerLat: Double, answerLng: Double, answerCountryId: String, correct: Boolean) {
        // JSONObject.quote guards the id string against JS injection/escaping issues.
        val idLiteral = JSONObject.quote(answerCountryId)
        call("AtlasPin.reveal($answerLat, $answerLng, $idLiteral, $correct)")
    }

    fun destroy() {
        (webView.parent as? ViewGroup)?.removeView(webView)
        webView.destroy()
    }
}

@Composable
fun PinQuestionView(
    controller: PinGlobeController,
    questionId: Long,
    answerRevealed: Boolean,
    answerLat: Double,
    answerLng: Double,
    answerCountryId: String,
    pinCorrect: Boolean?,
    onPinPlaced: () -> Unit,
    onPinConfirmed: (lat: Double, lng: Double, countryId: String, countryName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentPlaced by rememberUpdatedState(onPinPlaced)
    val currentConfirm by rememberUpdatedState(onPinConfirmed)
    SideEffect {
        controller.onPinPlaced = { currentPlaced() }
        controller.onPinConfirmed = { lat, lng, countryId, countryName ->
            currentConfirm(lat, lng, countryId, countryName)
        }
    }

    LaunchedEffect(questionId) {
        controller.loadQuestion()
    }
    LaunchedEffect(questionId, answerRevealed) {
        if (answerRevealed) {
            controller.reveal(answerLat, answerLng, answerCountryId, pinCorrect == true)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            // The long-lived WebView may still be attached to a previous
            // composition's host; detach before this AndroidView adopts it.
            (controller.webView.parent as? ViewGroup)?.removeView(controller.webView)
            controller.webView
        },
    )
}
