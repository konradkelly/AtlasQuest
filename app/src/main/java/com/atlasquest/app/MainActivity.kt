package com.atlasquest.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.atlasquest.app.ui.AtlasQuestNavHost
import com.atlasquest.app.ui.theme.AtlasQuestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AtlasQuestTheme {
                AtlasQuestNavHost()
            }
        }
    }
}
