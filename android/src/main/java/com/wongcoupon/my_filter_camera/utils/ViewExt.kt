package com.wongcoupon.my_filter_camera.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.View
import androidx.core.view.ViewCompat

inline fun View.doOnLayout(crossinline action: (view: View) -> Unit) {
    if (ViewCompat.isLaidOut(this) && !isLayoutRequested) {
        action(this)
    } else {
        doOnNextLayout { action(it) }
    }
}

inline fun View.doOnNextLayout(crossinline action: (view: View) -> Unit) {
    addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            view.removeOnLayoutChangeListener(this)
            action(view)
        }
    })
}

fun Bitmap.rotate(boolean: Boolean): Bitmap {
    val degrees = if (boolean) 90f else 90 + 180f
//    val degrees = if (boolean) 0f else  0f
    val matrix = Matrix().apply { postRotate(degrees) }
    if (!boolean) matrix.postScale(-1f, 1f, width / 2f, height / 2f)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
