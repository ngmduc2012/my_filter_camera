package com.wongcoupon.my_filter_camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.TextView
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class CustomCameraViewFactory(context: Context) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val customCameraView = CustomCameraViewHolder.getInstance(context)
        return CustomCameraPlatformView(customCameraView)
    }
}

//when back from cameraview screen, it will call customcameraview again with same initial key
class CustomCameraPlatformView(  private val customCameraView: CustomCameraView ) : PlatformView {
    override fun getView(): View {
        Log.d("CustomCameraPlatformVie", "getView")
        return customCameraView
    }
    override fun dispose() {
//        customCameraView.removeAllViews()
//        customCameraView.onDetachedFromWindow()
        Log.d("CustomCameraPlatformVie", "Disposed resources")

    }
}

//class CustomCameraViewFactory :PlatformViewFactory(StandardMessageCodec.INSTANCE) {
//    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
//        return CustomCameraPlatformView(context,viewId,args)
//    }
//}
//
//class CustomCameraPlatformView(context: Context, id: Int, creationParams: Any?) : PlatformView {
//    @SuppressLint("SetTextI18n")
//    private val textView: TextView = TextView(context).apply {
//        textSize = 72f
//        setBackgroundColor(Color.RED)
//        text = "Rendered on a native Android view (id: $id)" }
//    override fun getView(): View {
//        return textView
//    }
//    override fun dispose() {}
//}
