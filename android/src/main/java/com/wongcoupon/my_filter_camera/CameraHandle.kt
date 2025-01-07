package com.wongcoupon.my_filter_camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.TextureRegistry
import io.flutter.view.TextureRegistry.SurfaceTextureEntry
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.wongcoupon.my_filter_camera.models.CameraData
import com.wongcoupon.my_filter_camera.utils.GPUImageFilterTools
import com.wongcoupon.my_filter_camera.utils.YuvToRgbConverter
import com.wongcoupon.my_filter_camera.utils.rotate
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.pow


///Learn more:
// Video camera X:  https://developer.android.com/media/camera/camerax/video-capture

class CameraHandle(
    private val activity: Activity, // create surfaceTexture(buffer luu tru hinh anh: ve len be mat ma khong hien thi truc tiep
    private val textureRegistry: TextureRegistry,
    private val utils: Utils,
    private val talkingWithFlutter: TalkingWithFlutter,
) {

    private val resolution = Size(1920, 1080)

    //    @SuppressLint("RestrictedApi")
    private val imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
//        .setDefaultResolution(Size(1920,1280))
        .setTargetResolution(this.resolution)
        .build()

    var cameraExecutor: ExecutorService? = null
    private var cameraProvider: ProcessCameraProvider? = null
    var camera: Camera? = null
    var preview: Preview? = null
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private lateinit var mGpuImage: GPUImage
    private lateinit var mGpuFilter: GPUImageFilter
    private var textureEntry: SurfaceTextureEntry? = null


    private lateinit var customCameraView: CustomCameraView
    private lateinit var customCameraView2: CustomCameraView2
    private lateinit var gpuImageView: GPUImageView
    private var imageAnalysis: ImageAnalysis? = null

    private lateinit var videoCapture: androidx.camera.video.VideoCapture<Recorder>
    private var currentRecording: Recording? = null
    private var audioEnabled = false
    private lateinit var mainThreadExecutor: Executor
    private var filterIndex: Int = 9
    private var contrastValue: Double = 60.0
    private var scaleRedValue: Float = 1.0f
    private var scaleGreenValue: Float = 1.0f
    private var scaleBlueValue: Float = 1.0f
    private var gammaValue: Double = 1.0

    init {
        mainThreadExecutor = ContextCompat.getMainExecutor(activity)
    }


    fun removeDir(call: MethodCall, result: MethodChannel.Result) {
        try {
            val imagesDir = File(activity.getExternalFilesDir(null), "Images")
            if (imagesDir.exists()) {
                imagesDir.deleteRecursively()
            }
            val videoDir = File(activity.getExternalFilesDir(null), "Videos")
            if (videoDir.exists()) {
                videoDir.deleteRecursively()
            }
            // Xóa các video trong MediaStore.Video.Media
            val contentResolver = activity.contentResolver
            val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            // Tạo query để tìm tất cả video được ứng dụng tạo ra
            val selection = "${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("CameraX-recording-%")

            // Xóa các tệp phù hợp với query
            val deletedRows = contentResolver.delete(uri, selection, selectionArgs)

            utils.log("Removed $deletedRows video(s) from MediaStore")
            utils.log("Removed videos from external files directory")
            result.success("All videos and Images removed successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            result.error("RemoveError", e.message, null)
            utils.log("Remove Image and Video Error")
        }
    }

    ///Start the camera
    @SuppressLint("ClickableViewAccessibility")
    @ExperimentalGetImage
    fun start(call: MethodCall, result: MethodChannel.Result) {
        val enableCamera2 = call.argument<Boolean>("enableCamera2") ?: false
        val handler = Handler(activity.mainLooper)
        if (enableCamera2) {
            customCameraView2.upDateFilter(filterIndex)
        } else {
            customCameraView.upDateFilter(filterIndex)
        }
        if (camera != null && preview != null && textureEntry != null) {
            val resolution = Objects.requireNonNull(
                preview!!.resolutionInfo
            )!!.resolution
            val portrait = camera!!.cameraInfo.sensorRotationDegrees % 180 == 0
            val width = resolution.width.toDouble()
            val height = resolution.height.toDouble()
            val size: MutableMap<String, Any> = HashMap()
            if (portrait) {
                size["width"] = width
                size["height"] = height
            } else {
//                size["width"] = height
//                size["height"] = width
                size["width"] = width
                size["height"] = height
            }

            val answer: MutableMap<String, Any> = HashMap()
            answer["textureId"] = textureEntry!!.id()
            answer["size"] = size
            answer["torchable"] = camera!!.cameraInfo.hasFlashUnit()
            result.success(answer)

            handler.postDelayed({
                talkingWithFlutter.onRunningCamera(true)
            }, 500)
        } else {
            facing = call.argument<Int>("facing") ?: 0
            val ratio = call.argument<Int>("ratio")
            val torchArgument = call.argument<Boolean>("torch")
            val torch = torchArgument != null && torchArgument

            val future = ProcessCameraProvider.getInstance(this.activity)
            val executor = ContextCompat.getMainExecutor(this.activity)
            future.addListener({
                try {
                    cameraProvider = future.get()
                    if (cameraProvider == null) {
                        result.error("cameraProvider", "cameraProvider is null", null)
                        return@addListener
                    }
                    cameraProvider!!.unbindAll()
                    textureEntry = textureRegistry.createSurfaceTexture()
                    if (textureEntry == null) {
                        result.error("textureEntry", "textureEntry is null", null)
                        return@addListener
                    }

                    // Preview
                    val surfaceProvider = Preview.SurfaceProvider { request ->
                        val texture = textureEntry!!.surfaceTexture()
                        texture.setDefaultBufferSize(
                            request.resolution.width,
                            request.resolution.height
                        )
                        val surface = Surface(texture)
                        request.provideSurface(surface, executor) {
                        }
                    }

                    // Build the preview to be shown on the Flutter texture
                    val previewBuilder = Preview.Builder()
                    if (ratio != null) {
                        if (ratio == 0) {
                            previewBuilder.setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        } else {
                            previewBuilder.setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        }
                    }
//                    previewBuilder.setTargetResolution(resolution)
                    preview = previewBuilder.build().apply {
                        setSurfaceProvider(surfaceProvider)
                        setTargetRotation(Surface.ROTATION_90)
                    }

                    // Select the correct camera
                    cameraSelector =
                        if (facing == 0) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

//                    val sizeABC = Size(1980, 1080)// cho ra kết quả 2560x1440
                    //ImageAnalysis builder
                    val imageAnalyzer = ImageAnalysis.Builder()
//                        .setTargetResolution(resolution)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                        .apply {
                            setAnalyzer(
                                (cameraExecutor)!!,
                                LuminosityAnalyzer(
                                    utils,
                                    converter,
                                    cameraSelector,
                                    enableCamera2

                                )
                            )
                        }
                    // build a recorder, which can:
                    //   - record video/audio to MediaStore(only shown here), File, ParcelFileDescriptor
                    //   - be used create recording(s) (the recording performs recording)
                    val quality = Quality.HIGHEST
                    val qualitySelector = QualitySelector.from(quality)
                    val recorder = Recorder.Builder()
                        .setQualitySelector(qualitySelector)
                        .build()
                    videoCapture = androidx.camera.video.VideoCapture.withOutput(recorder)
                    camera = cameraProvider!!.bindToLifecycle(
                        (activity as LifecycleOwner),
                        cameraSelector,
                        imageCapture,
                        preview,
                        ///TODO enable video capture
//                        videoCapture,
                        imageAnalyzer,
                    )

                    imageAnalysis?.clearAnalyzer()
                    imageAnalysis = imageAnalyzer
                    if (camera == null) {
                        result.error("camera", "camera is null", null)
                        return@addListener
                    }
                    // Register the torch listener

                    // Assuming talkingWithFlutter is an instance of TalkingWithFlutter
                    camera!!.cameraInfo.torchState
                        .observe(
                            (activity as LifecycleOwner)
                        ) { state: Int ->
                            talkingWithFlutter.registerTorchListener(
                                state
                            )
                        }
                    ///Brightness
                    cameraControl = camera!!.cameraControl
                    cameraInfo = camera!!.cameraInfo
                    exposureState = cameraInfo.exposureState

                    // Enable torch if provided
                    camera!!.cameraControl.enableTorch(torch)

                    val resolution =
                        preview!!.resolutionInfo!!.resolution
                    val portrait =
                        camera!!.cameraInfo.sensorRotationDegrees % 180 == 0
                    val width = resolution.width.toDouble()
                    val height = resolution.height.toDouble()
                    val size = if (portrait) mapOf(
                        "width" to width,
                        "height" to height
                    ) else mapOf("width" to height, "height" to width)
                    val answer = mapOf(
                        "textureId" to textureEntry!!.id(),
                        "size" to size,
                        "torchable" to camera!!.cameraInfo.hasFlashUnit(),
                    )
                    result.success(answer)
                    handler.postDelayed({
                        talkingWithFlutter.onRunningCamera(true)
                    }, 500)
                } catch (e: Exception) {
                    result.error("CameraProviderError", e.message, null)
                }
            }, executor)
        }
    }


    fun toggleTorch(call: MethodCall, result: MethodChannel.Result) {
        if (camera == null) {
            result.error(TAG, "Called toggleTorch() while stopped!", null)
            return
        }
        checkNotNull(call.arguments())
        camera!!.cameraControl.enableTorch(call.arguments<Any>() == 1)
        result.success(null)
    }

    @SuppressLint("RestrictedApi")
    fun stop(result: MethodChannel.Result) {
        try {
            talkingWithFlutter.onRunningCamera(false)
            imageAnalysis?.clearAnalyzer()
//            cameraExecutor?.shutdown()
            cameraProvider?.unbindAll()
//            cameraProvider?.shutdown()
            textureEntry?.release()
            textureEntry = null
            camera = null
            preview = null

            filterIndex = 9
            result.success(null)
        } catch (e: Exception) {
            result.error("StopError", e.message, null)
        }
    }

    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo
    private lateinit var exposureState: ExposureState

    fun adjustBrightness(call: MethodCall, result: MethodChannel.Result) {
        val value = call.argument<Int>("brightnessValue") ?: 0
        if (exposureState.isExposureCompensationSupported) {
            val currentExposureCompensationIndex = exposureState.exposureCompensationIndex
            val minExposureCompensationIndex = exposureState.exposureCompensationRange.lower
            val maxExposureCompensationIndex = exposureState.exposureCompensationRange.upper

            // Calculate the new exposure compensation index
            var newExposureCompensationIndex = value

            // Clamp the new index within the supported range
            newExposureCompensationIndex = newExposureCompensationIndex.coerceIn(
                minExposureCompensationIndex,
                maxExposureCompensationIndex
            )

            // Set the new exposure compensation index
            cameraControl.setExposureCompensationIndex(newExposureCompensationIndex)
                .addListener({
                    utils.log("set brightness: ADJUSTBRIGHTNESS $newExposureCompensationIndex")
                    utils.log("currentExposureCompensationIndex: ADJUSTBRIGHTNESS $currentExposureCompensationIndex")
                    result.success(newExposureCompensationIndex)
                }, ContextCompat.getMainExecutor(activity))
        } else {
            // Handle the case where exposure compensation is not supported
            result.error(
                "ExposureCompensationNotSupported",
                "Exposure compensation is not supported on this device.",
                null
            )
        }
    }

    fun adjustRGB(call: MethodCall, result: MethodChannel.Result) {
        val red = call.argument<Double>("redValue") ?: 0.0
        val green = call.argument<Double>("greenValue") ?: 0.0
        val blue = call.argument<Double>("blueValue") ?: 0.0
        scaleRedValue = red.toFloat()
        scaleGreenValue = green.toFloat()
        scaleBlueValue = blue.toFloat()
        result.success(mapOf("red" to red, "green" to green, "blue" to blue))
    }

    ///Adjust Contrast
    fun adjustContrast(call: MethodCall, result: MethodChannel.Result) {
        val value = call.argument<Double>("contrastValue") ?: 0.0
        contrastValue = value
        result.success(contrastValue)
        /*  // Tạo hoặc truy cập thư mục "Images"
          val imagesDir = File(activity.getExternalFilesDir(null), "Images")
          if (!imagesDir.exists()) {
              imagesDir.mkdirs() // Tạo thư mục nếu chưa tồn tại
          }

          // Đặt tên tệp và lưu trong thư mục "Images"
          val file = File(imagesDir, UUID.randomUUID().toString() + ".jpg")
          val src= BitmapFactory.decodeFile(imagePath)
          val dst = adjustContrastToBitmap(src,contrastValue)
          val bytes = utils.toJpeg(dst, 100)
          try {

              FileOutputStream(file).use { fos ->
                  fos.write(bytes)
              }
          } catch (e: IOException) {
              e.printStackTrace()
          }
          result.success(
              mapOf(
                  "path" to file.absolutePath,
                  "name" to file.name,
                  "mimeType" to "image/jpeg",
                  "size" to file.length()
              )
          )
          utils.log("finish adjust Contrast" + file.absolutePath)
  */
    }

    ///Adjust Gamma
    fun adjustGamma(call: MethodCall, result: MethodChannel.Result) {
        val value = call.argument<Double>("GammaValue") ?: 0.0
        gammaValue = value
        result.success(gammaValue)
    }


    /**
     * ################################################################################################
     * FUNCTION   : Capture Image
     * DESCRIPTION:
     *
     *
     * ------------------------------------------------------------------------------------------------
     * CHỨC NĂNG: Cài đặt trạng thái ban đầu
     * MÔ TẢ    :
     * (1) Tạo file lưu dưới dạng cache.
     * (2) Trả về kết quả là đường dẫn file.
     * ################################################################################################
     */
    fun getTimestamp(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault())
        return sdf.format(Date())
    }

    //    var isCapturing = false
    private var filteredBitmap: Bitmap? = null

    @ExperimentalGetImage
    fun capture(call: MethodCall, result: MethodChannel.Result) {
        val imagesDir = File(activity.getExternalFilesDir(null), "Images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs() // Tạo thư mục nếu chưa tồn tại
        }
        val fileName = "captured_image_${getTimestamp()}.jpg"
        val file = File(imagesDir, fileName)


        imageCapture.takePicture(
            ContextCompat.getMainExecutor(activity.applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                @SuppressLint("UnsafeExperimentalUsageError")
                override fun onCaptureSuccess(image: ImageProxy) {
                    // capture 3
                    val bmp = utils.imageProxyToBitmap(image)
                    val rotation = image.imageInfo.rotationDegrees
                    val matrix = Matrix()
                    matrix.postRotate(rotation.toFloat())
                    val sceneBitmap =
                        Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                    mGpuImage.setImage(sceneBitmap)
                    mGpuImage.setFilter(GPUImageFilterTools.createFilter(activity, filterIndex))
                    val bytes = utils.toJpeg(mGpuImage.bitmapWithFilterApplied, 100)

                    try {
                        FileOutputStream(file).use { fos ->
                            fos.write(bytes)
                            fos.flush() // Ensure all data is written
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    utils.log("Prepare onSendResult" + file.absolutePath)
                    talkingWithFlutter.onSendResult(
                        CameraData(
                            null,
                            null,
                            file.absolutePath,
                            null,
                            null,
                            null
                        )
                    )
                    result.success(
                        mapOf(
                            "path" to file.absolutePath,
                            "name" to file.name,
                            "mimeType" to "image/jpeg",
                            "size" to file.length()
                        )
                    )
                    utils.log("finish onSendResult change" + file.absolutePath)
                    image.close()
                }

                override fun onError(error: ImageCaptureException) {
                    talkingWithFlutter.onSendResult(
                        CameraData(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                        )
                    )
                }
            })
    }

    /**
     * ################################################################################################
     * FUNCTION   : Capture Image in the frame
     * DESCRIPTION:
     *
     *
     * ------------------------------------------------------------------------------------------------
     * CHỨC NĂNG: Chụp hình ảnh trong 1 khung hình.
     * MÔ TẢ    :
     * (1) Lấy các giá trị của khung hình được gửi từ flutter
     * (2) Tạo file lưu dưới dạng cache.
     * (3) Xử lý ảnh.
     * (4) Tạo flie và trả về kết quả là đường dẫn file.
     * ################################################################################################
     */

/*
    @ExperimentalGetImage
    fun takePicture(call: MethodCall, result: MethodChannel.Result) {
        // takePicture 1
        val boxWidth = call.argument<Double>("boxWidth") ?: return
        val boxHeight = call.argument<Double>("boxHeight") ?: return
        val boxTop = call.argument<Double>("boxTop") ?: return
        val boxLeft = call.argument<Double>("boxLeft") ?: return
        val screenWidth = call.argument<Double>("screenWidth") ?: return
        val screenHeight = call.argument<Double>("screenHeight") ?: return
        result.success(null)

        // takePicture 2
        val fileCrop = File(activity.cacheDir, UUID.randomUUID().toString() + ".jpg")
        val file = File(activity.cacheDir, UUID.randomUUID().toString() + "TruePath.jpg")
        imageCapture.takePicture(ContextCompat.getMainExecutor(activity.applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    // takePicture 3
                    processImage(
                        boxWidth,
                        boxHeight,
                        boxTop,
                        boxLeft,
                        screenWidth,
                        screenHeight,
                        fileCrop,
                        file,
                        image
                    )


                    talkingWithFlutter.onSendResult(
                        CameraData(
                            null,
                            null,
                            fileCrop.absolutePath,
                            file.absolutePath,
                            null,
                            null
                        )
                    )
                    utils.log("finish onSendResult" + fileCrop.absolutePath)
                    image.close()
                }

                override fun onError(error: ImageCaptureException) {
                    talkingWithFlutter.onSendResult(
                        CameraData(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                        )
                    )
                }
            })
    }
*/

    /**
     * ################################################################################################
     * FUNCTION   : Filter
     * DESCRIPTION:
     *
     *
     * ------------------------------------------------------------------------------------------------
     * CHỨC NĂNG: Update filter
     * MÔ TẢ    :
     * (1)
     * (2)
     * ################################################################################################
     */
    fun updateFilter(call: MethodCall, result: MethodChannel.Result) {
        // Check if the camera is initialized
        if (camera == null) {
            result.error("CAMERA_NOT_INITIALIZED", "Camera not initialized", null)
            return
        }
        // Get the filter type (ensure it's passed as an integer)
        val filterType = call.argument<Int>("filterType") ?: run {
            result.error("INVALID_ARGUMENT", "Filter type argument is missing", null)
            return
        }
        // Select the appropriate filter based on the passed filterType
//        mGpuFilter = GPUImageFilterTools.createFilter(activity, filterType)
        // Send back a success result
        customCameraView.upDateFilter(filterType)
        customCameraView2.upDateFilter(filterType)
        filterIndex = filterType
        result.success("Filter updated successfully")
    }


    private fun processImage(
        boxWidth: Double, boxHeight: Double,
        boxTop: Double, boxLeft: Double,
        screenWidth: Double, screenHeight: Double,
        fileCrop: File, file: File, image: ImageProxy
    ) {
        val bmp = utils.imageProxyToBitmap(image)

        val rotation = image.imageInfo.rotationDegrees
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())

        val croppedWidth: Double
        val croppedHeight: Double
        val top: Double
        val left: Double
        val bytesFace: ByteArray
        val bytes: ByteArray

        // Handle image rotation
        if (rotation == 0) {
            top = bmp.height * boxTop / screenHeight
            croppedHeight = boxHeight * bmp.height / screenHeight
            croppedWidth = croppedHeight * boxWidth / boxHeight
            left = (bmp.width - croppedWidth) / 2
        } else {
            croppedWidth = bmp.width / screenHeight * boxHeight
            croppedHeight = croppedWidth * boxWidth / boxHeight
            top = (bmp.height - croppedHeight) / 2

            // Camera Front
            left = if (facing == 0) {
                bmp.width - bmp.width / screenHeight * boxTop - croppedWidth
            } else { // Camera Back
                bmp.width / screenHeight * boxTop
            }
        }

        // Crop the image
        val bmpSelf = Bitmap.createScaledBitmap(
            bmp,
            (bmp.width * 0.3).toInt(),
            (bmp.height * 0.3).toInt(),
            true
        )

        val flippedBitmap = utils.toFlip(
            Bitmap.createBitmap(
                bmpSelf,
                0,
                0,
                bmpSelf.width,
                bmpSelf.height,
                matrix,
                true
            ),
            true,
            false
        )
        bytesFace = utils.toJpeg(flippedBitmap, 100)
        ///use this function instead of `file.writeBytes(bytesFace);`
        try {
            FileOutputStream(file).use { fos ->
                fos.write(bytesFace)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        // Camera Front
        if (facing == 0) {
            val cameraFrontBitmap = utils.toFlip(
                Bitmap.createBitmap(
                    bmp,
                    left.toInt(),
                    top.toInt(),
                    croppedWidth.toInt(),
                    croppedHeight.toInt(),
                    matrix,
                    true
                ),
                true,
                false
            )
            bytes = utils.toJpeg(cameraFrontBitmap, 100)
        } else { // Camera Back

            bytes = utils.toJpeg(
                Bitmap.createBitmap(
                    bmp,
                    left.toInt(),
                    top.toInt(),
                    croppedWidth.toInt(),
                    croppedHeight.toInt(),
                    matrix,
                    true
                ),
                100
            )
        }
        try {
            FileOutputStream(file).use { fos ->
                fos.write(bytes)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        image.close()
    }


    companion object {
        const val REQUEST_CODE: Int = 22022022
        private const val NO_GUID = 20122012
        const val DEFAULT_QUALITY_IDX: Int = 0
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val TAG: String = CameraHandle::class.java.simpleName
        var facing: Int = 0
    }


    private var bitmap: Bitmap? = null
    private lateinit var converter: YuvToRgbConverter

    @SuppressLint("UnsafeOptInUsageError")
    inner class LuminosityAnalyzer(
        private val utils: Utils,
        private val converter: YuvToRgbConverter,
        private val cameraSelector: CameraSelector,
        private val enableCamera2: Boolean = false
    ) : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
//            if (!isCapturing) {
            val bitmap = utils.allocateBitmapIfNecessary(
                image.width,
                image.height,
                bitmap
            )
            converter.yuvToRgb(image.image!!, bitmap)
            val rotatedBitmap = bitmap.rotate(cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
//            val dst = adjustGammaToBitmap(rotatedBitmap)

            if (enableCamera2) {
                customCameraView2.post {
                    customCameraView2.setBitmap(
                        rotatedBitmap
                    )
                }
            } else {
                customCameraView.post {
                    customCameraView.setBitmap(
                        rotatedBitmap
                    )
                }
            }
//            bitmap.recycle()
            image.close()
        }

        override fun updateTransform(matrix: Matrix?) {
            super.updateTransform(matrix)
        }
    }




    @ExperimentalGetImage
    fun applyImageSample(call: MethodCall, result: MethodChannel.Result) {
        val context = activity.applicationContext
        val filters = PluginFilterEnum.values()

        val imageList = mutableListOf<Map<String, String>>()
        val originalBitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.image)

        // Khởi tạo GPUImage instance
        val mGpuImage = GPUImage(context)

        for (filterType in filters) {
            // Tạo filter và áp dụng nó lên ảnh
            val filter = when (filterType) {
                PluginFilterEnum.GRAYSCALE -> GPUImageFilterTools.createFilter(activity, 0)
                PluginFilterEnum.MONOCHROME -> GPUImageFilterTools.createFilter(activity, 1)
//                PluginFilterEnum.SEPIA -> GPUImageFilterTools.createFilter(activity, 2)
//                PluginFilterEnum.SKETCH -> GPUImageFilterTools.createFilter(activity, 3)
//                PluginFilterEnum.SMOOTH_TOON -> GPUImageFilterTools.createFilter(activity, 4)
//                PluginFilterEnum.VIGNETTE -> GPUImageFilterTools.createFilter(activity, 5)
//                PluginFilterEnum.BULGE_DISTORTION -> GPUImageFilterTools.createFilter(activity, 6)
//                PluginFilterEnum.BULGE_DISTORTION2 -> GPUImageFilterTools.createFilter(activity, 7)
//                PluginFilterEnum.SWIRL -> GPUImageFilterTools.createFilter(activity, 8)
                PluginFilterEnum.NORMAL -> GPUImageFilterTools.createFilter(activity, 9)
                else -> GPUImageFilterTools.createFilter(
                    activity,
                    9
                )// Bộ lọc mặc định (không làm gì cả)
            }

            // Áp dụng bộ lọc lên ảnh
            mGpuImage.setFilter(filter)
            mGpuImage.setImage(originalBitmap)

            // Lấy ảnh đã được áp dụng filter
            val filteredBitmap = mGpuImage.bitmapWithFilterApplied

            // Chuyển Bitmap thành byte array
            val byteArrayOutputStream = ByteArrayOutputStream()
            filteredBitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // Chuyển byte array thành chuỗi Base64
            val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            // Tạo map chứa tên và Base64 của ảnh
            val imageData = mapOf(
                "name" to filterType.filterName,
                "code" to filterType.code.toString(),
                "image" to base64Image
            )

            // Thêm map vào danh sách
            imageList.add(imageData)
        }

        // Gửi danh sách các ảnh (bao gồm tên và Base64) về Flutter
        result.success(mapOf("images" to imageList))
    }

    enum class PluginFilterEnum(val code: Int, val filterName: String) {
        NORMAL(9, "Original"),
        GRAYSCALE(0, "GrayScale"),
        MONOCHROME(1, "MonoChrome");
//        SEPIA(2, "Sepia"),
//        SKETCH(3, "Sketch"),
//        SMOOTH_TOON(4, "Smooth Toon"),
//        VIGNETTE(5, "Vignette"),
//        BULGE_DISTORTION(6, "Bulge Distortion"),
//        BULGE_DISTORTION2(7, "Bulge Distortion"),
//        SWIRL(8, "Swirl");

        companion object {
            fun fromCode(code: Int): PluginFilterEnum {
                return values().firstOrNull { it.code == code } ?: NORMAL
            }
        }
    }


    fun initCameraExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        converter = YuvToRgbConverter(activity)
        gpuImageView = GPUImageView(activity)
        mGpuImage = GPUImage(activity)
        customCameraView = CustomCameraViewHolder.getInstance(activity)
        customCameraView2 = CustomCameraViewHolder2.getInstance(activity)
        // Initialize mGpuFilter here with a default filter
        mGpuFilter = GPUImageFilterTools.createFilter(activity, 9)

    }

    private var outputFilePath: String? = null
    private var finalizeFuture: CompletableFuture<ByteArray>? = null
    private var recordingStoppedCallback: (() -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun startVideoRecording(call: MethodCall, result: MethodChannel.Result) {
        // Create MediaStoreOutputOptions for the recorder
        outputFilePath = null
        val name = "CameraX-recording-" +
                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            activity.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()

        // Configure Recorder and Start recording to the mediaStoreOutput
        currentRecording = videoCapture.output
            .prepareRecording(activity, mediaStoreOutput)
            .apply { if (audioEnabled) withAudioEnabled() }
            .start(mainThreadExecutor, captureListener)

        Log.i(TAG, "Recording started")
    }

    /**
     * CaptureEvent listener.
     */
    private val captureListener = Consumer<VideoRecordEvent> { event ->
        // Cache the recording state
        when (event) {
            is VideoRecordEvent.Start -> {
                Log.i(TAG, "Recording started")
            }

            is VideoRecordEvent.Finalize -> {
                if (event.hasError()) {
                    Log.e(TAG, "Video capture failed: ${event.error}")
                } else {
                    // Set the outputFilePath here to ensure it's available after recording
                    outputFilePath = event.outputResults.outputUri.toString()
                    Log.i(TAG, "Video capture succeeded: $outputFilePath")
                    // Call result.success with the file path directly from here, if needed
                    notifyRecordingStopped()
                }
            }
        }
    }


    private fun copyFileFromContentUri(
        context: Context,
        contentUri: Uri,
        destinationPath: String,
        newName: String
    ): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(contentUri)
        val directory = File(destinationPath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "$newName.mp4")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
            }
        }
        return file.absolutePath
    }


    fun stopVideoRecording(call: MethodCall, result: MethodChannel.Result) {
        if (currentRecording != null) {
            currentRecording!!.stop()
            currentRecording = null
            recordingStoppedCallback = {
                if (outputFilePath != null) {
                    val contentUri = Uri.parse(outputFilePath)
                    val filePath = copyFileFromContentUri(
                        context = activity,
                        contentUri = contentUri,
                        destinationPath = activity.getExternalFilesDir(null).toString(),
                        newName = "video_recorded"
                    )

                    val videoDir = File(activity.getExternalFilesDir(null), "Videos")
                    if (!videoDir.exists()) {
                        videoDir.mkdirs() // Tạo thư mục nếu chưa tồn tại
                    }
                    val file = File(filePath)
                    val fileCopy = File(videoDir, file.name)
                    file.copyTo(fileCopy, overwrite = true)
                    file.delete()
                    val xFilePath = mapOf(
                        "path" to fileCopy.absolutePath,
                        "name" to fileCopy.name,
                        "mimeType" to "video/mp4",
                        "size" to fileCopy.length()
                    )
                    result.success(xFilePath)
                } else {
                    result.error("RecordingError", "Failed to retrieve output file path", null)
                }
            }
            utils.log("Recording stopped!")
        } else {
            result.error("RecordingError", "No recording in progress", null)
        }
    }


    private fun notifyRecordingStopped() {
        recordingStoppedCallback?.invoke()
        recordingStoppedCallback = null // Reset callback after invocation
    }


    fun pauseVideoRecording(call: MethodCall, result: MethodChannel.Result) {
        if (currentRecording != null) {
            currentRecording!!.pause()
            utils.log("Recording paused!")
        }
    }

    fun resumeVideoRecording(call: MethodCall, result: MethodChannel.Result) {
        if (currentRecording != null) {
            currentRecording!!.resume()
            utils.log("Recording resume!")
        }
    }
}