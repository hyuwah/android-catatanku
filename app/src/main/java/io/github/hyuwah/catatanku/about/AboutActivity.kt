package io.github.hyuwah.catatanku.about

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import io.github.hyuwah.catatanku.BuildConfig
import io.github.hyuwah.catatanku.R
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element
import java.util.Calendar

class AboutActivity : AppCompatActivity() {

    private val sharedPref: SharedPreferences by lazy {
        getSharedPreferences(getString(R.string.pref_file_key), MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val aboutPage = AboutPage(this)
            .isRTL(false)
            .setImage(R.mipmap.ic_launcher)
            .setDescription("CatatanKu\nSimple note-taking apps")
            .addItem(versionElement)
            .addGroup("Connect with developer")
            .addEmail("muhammad.whydn@gmail.com", "Contact developer")
            .addWebsite("http://hyuwah.github.io/", "Visit developer website")
            .addGitHub("hyuwah", "Check other projects on Github")
            .addItem(copyRightsElement)
            .create()
        setContentView(aboutPage)
    }

    private val versionElement = Element().apply {
        title = "Version ${BuildConfig.VERSION_NAME}"
        setOnClickListener {
            toggleDebugMode()
        }
    }

    private val copyRightsElement = Element().apply {
        title = "Copyrights © ${Calendar.getInstance()[Calendar.YEAR]}"
        // setIconDrawable(R.drawable.about_icon_copy_right);
        iconTint = mehdi.sakout.aboutpage.R.color.about_item_icon_color
        iconNightTint = android.R.color.white
        gravity = Gravity.CENTER
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