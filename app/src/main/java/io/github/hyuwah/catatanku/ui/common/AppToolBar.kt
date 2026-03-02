package io.github.hyuwah.catatanku.ui.common

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.Toolbar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.hyuwah.catatanku.R
import io.github.hyuwah.catatanku.ui.theme.onPrimaryContainerLight
import io.github.hyuwah.catatanku.ui.theme.primaryContainerLight
import io.github.hyuwah.catatanku.utils.adjustInsets
import io.github.hyuwah.catatanku.utils.getThemeColor

class AppToolBar(private val context: Context, private val attrs: AttributeSet? = null) :
    Toolbar(context, attrs) {

    init {
        adjustInsets(statusBar = true)
        val colorPrimary = context.getThemeColor(R.attr.colorPrimary)
        val titleTextColor = context.getThemeColor(R.attr.colorOnPrimary)
        val subTitleTextColor = context.getThemeColor(R.attr.colorOnPrimary)
        setBackgroundColor(colorPrimary)
        setTitleTextColor(titleTextColor)
        setSubtitleTextColor(subTitleTextColor)
        overflowIcon?.setTint(titleTextColor)
        navigationIcon?.setTint(titleTextColor)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultAppToolBar(
    text: String,
    modifier: Modifier = Modifier,
    onNavClicked: () -> Unit = {},
    navIcon: ImageVector? = Icons.AutoMirrored.Filled.ArrowBack
) {
    TopAppBar(
        title = {
            Text(
                text = text,
                color = onPrimaryContainerLight
            )
        },
        navigationIcon = {
            navIcon?.let {
                IconButton(
                    onClick = onNavClicked
                ) {
                    Icon(
                        it,
                        contentDescription = "Back",
                        tint = onPrimaryContainerLight
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = primaryContainerLight
        ),
        modifier = modifier
    )
}