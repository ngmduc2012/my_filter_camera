package com.wongcoupon.my_filter_camera

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoCapture.withOutput
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import jp.co.cyberagent.android.gpuimage.GLTextureView
import jp.co.cyberagent.android.gpuimage.GPUImage
import com.wongcoupon.my_filter_camera.utils.GPUImageFilterTools
import java.io.File

class CustomCameraView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    public val gpuImage: GPUImage = GPUImage(context)
    private val glTextureView: GLTextureView
    private var utils: Utils

    init {
        // Initialize GLTextureView and GPUImage for filtered rendering
        glTextureView = GLTextureView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        gpuImage.setGLTextureView(glTextureView)
        gpuImage.setFilter(GPUImageFilterTools.createFilter(context, 9))

        utils= Utils();
        // Add views to the layout in the correct order
        addView(glTextureView)      // GLTextureView for GPUImage filtered output

        Log.d("CustomCameraView", "PreviewView and GLTextureView initialized and added to layout")

    }


    fun setBitmap(bitmap: Bitmap) {
        gpuImage.setImage(bitmap)
        glTextureView.requestRender()
//        Log.d("CustomCameraView", "Bitmap set and GLTextureView requested to render.")
    }

    fun upDateFilter(filterType: Int) {
        gpuImage.setFilter(GPUImageFilterTools.createFilter(context,filterType))
//        Log.d("CustomCameraView", "Bitmap set and GLTextureView requested to render.")
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        gpuImage.deleteImage()
        Log.d("CustomCameraView", "Resources released")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        utils.log( "View reattached to window")
    }



}


object CustomCameraViewHolder {
    private var customCameraView: CustomCameraView? = null

    fun getInstance(context: Context): CustomCameraView {
        if (customCameraView == null) {
            customCameraView = CustomCameraView(context)
        }
        return customCameraView!!
    }

    fun dispose() {
        customCameraView?.removeAllViews()
        customCameraView?.onDetachedFromWindow()
        customCameraView = null
    }
}

