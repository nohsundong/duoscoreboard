package com.myowndime.duoscoreboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.ui.common.KeepScreenOn
import com.myowndime.duoscoreboard.ui.navigation.DuoScoreboardNavHost
import com.myowndime.duoscoreboard.ui.theme.DuoScoreboardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepository = (application as DuoScoreboardApp).container.settingsRepository

        setContent {
            val settings by settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())

            DuoScoreboardTheme(themeMode = settings.themeMode) {
                KeepScreenOn(enabled = settings.keepScreenOn)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    DuoScoreboardNavHost()
                }
            }
        }
    }
}
