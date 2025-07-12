package io.github.hyuwah.catatanku.ui.common

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.Toolbar
import io.github.hyuwah.catatanku.R
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