package io.github.hyuwah.catatanku.utils

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) { bindingInflater.invoke(layoutInflater) }

@ColorInt
fun Context.getThemeColor(@AttrRes attribute: Int) = TypedValue().apply {
    theme.resolveAttribute(attribute, this, true)
}.data

fun View.adjustInsets(
    statusBar: Boolean = false,
    navigationBar: Boolean = false,
    onApply: (View, Insets) -> Unit = { view, insets ->
        view.updatePadding(top = insets.top, bottom = insets.bottom)
    }
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val typeMask = when {
                statusBar && navigationBar -> WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
                statusBar -> WindowInsetsCompat.Type.statusBars()
                navigationBar -> WindowInsetsCompat.Type.navigationBars()
                else -> 0
            }
            val insets = windowInsets.getInsets(typeMask)
            onApply(view, insets)
            WindowInsetsCompat.CONSUMED
        }
    }
}