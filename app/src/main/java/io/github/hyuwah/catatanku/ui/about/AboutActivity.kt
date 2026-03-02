package io.github.hyuwah.catatanku.ui.about

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import io.github.hyuwah.catatanku.BuildConfig
import io.github.hyuwah.catatanku.R
import io.github.hyuwah.catatanku.ui.common.DefaultAppToolBar
import io.github.hyuwah.catatanku.ui.theme.AppTheme
import io.github.hyuwah.catatanku.ui.theme.AppTypography
import java.util.Calendar

class AboutActivity : AppCompatActivity() {

    private val sharedPref: SharedPreferences by lazy {
        getSharedPreferences(getString(R.string.pref_file_key), MODE_PRIVATE)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Scaffold(
                    topBar = {
                        DefaultAppToolBar(
                            text = "About",
                            onNavClicked = { finish() }
                        )
                    }
                ) { paddingValues ->
                    AboutContent(
                        modifier = Modifier.padding(paddingValues),
                        onContactClicked = {
                            // redirect to GitHub profile
                            Intent(Intent.ACTION_VIEW).apply {
                                data = "https://github.com/hyuwah/".toUri()
                                startActivity(this)
                            }
                        },
                        onOtherProjectClicked = {
                            // redirect to GitHub project
                            Intent(Intent.ACTION_VIEW).apply {
                                data = "https://github.com/hyuwah?tab=repositories".toUri()
                                startActivity(this)
                            }
                        },
                        onDebugToggle = ::toggleDebugMode
                    )
                }
            }
        }
    }

    private fun toggleDebugMode() {
        val debugToggle = sharedPref.getBoolean(getString(R.string.pref_key_isdebug), false)
        sharedPref.edit {
            putBoolean(getString(R.string.pref_key_isdebug), !debugToggle)
            apply()
        }
        Toast.makeText(
            this,
            "Debug Mode: " + if (!debugToggle) "Activated" else "Deactivated",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
private fun AboutContent(
    modifier: Modifier = Modifier,
    onContactClicked: () -> Unit = {},
    onOtherProjectClicked: () -> Unit = {},
    onDebugToggle: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Image(painterResource(R.mipmap.ic_launcher_round), contentDescription = null)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "CatatanKu",
            textAlign = TextAlign.Center,
            style = AppTypography.displaySmall
        )
        Text(
            text = "Simple note-taking apps",
            textAlign = TextAlign.Center,
            style = AppTypography.headlineSmall,
        )

        Spacer(modifier = Modifier.height(32.dp))

        ListItem(
            headlineContent = { Text(text = "Version ${BuildConfig.VERSION_NAME}") },
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDebugToggle
            )
        )
        ListItem(
            headlineContent = { Text(text = "Contact Developer") },
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                onClick = onContactClicked
            )
        )
        ListItem(
            headlineContent = { Text(text = "Check other projects on Github") },
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                onClick = onOtherProjectClicked
            )
        )
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Copyrights © ${Calendar.getInstance()[Calendar.YEAR]}",
            textAlign = TextAlign.Center,
            style = AppTypography.labelLarge
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutContentPreview() {
    AppTheme {
        AboutContent()
    }
}