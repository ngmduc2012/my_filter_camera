package com.wongcoupon.my_filter_camera.camera_fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ICameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final int MIN_PREVIEW_PIXELS = 470 * 320;
    private static final int MAX_PREVIEW_PIXELS = 1920 * 1080;// 1280 * 800;

    SurfaceHolder mHolder;
    Camera mCamera = null;
    private Context m_Context;
    private static boolean m_bRot = false;

    public boolean m_flash = false;
    public int m_timer = 0;
    public int m_timePrint = 0;
    public int m_zoom = 0;
    public int m_zoomMax = 99;
    float mDist = 0;
    int m_CameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    String TAG = "Cuong";

    public void close() {

        Clear_Camera();
    }

    public ICameraPreview(Context context, Camera camera) {
        super(context);
        m_Context = context;
        int iSize = 50;
        mCamera = camera;
        try {
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        } catch (Exception e) {
        }
    }

    public ICameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_Context = context;

        try {
            if (mCamera == null) {
                mCamera = Camera.open();
            }
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        } catch (Exception e) {
        }
    }

    public void Clear_Camera() {
        if (mCamera != null) {
//            mCamera.cancelAutoFocus();
            stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e("Camera", "*** WARNING *** surfaceCreated() gave us a null surface!");
        }

        mHolder = holder;
        Configuration config = getResources().getConfiguration();
        if (mCamera == null) {
            mCamera = Camera.open(m_CameraId);
            m_zoom = 0;
            m_timer = 0;
            m_flash = false;
            m_timePrint = 0;
        }

        try {
            int width = this.getWidth();
            int height = this.getHeight();
            int rotation = 0;
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                rotation = 0;
                width = this.getHeight();
                height = this.getWidth();
            } else {
                rotation = 90;
            }

            Camera.Parameters parameters = mCamera.getParameters();
            m_zoomMax = parameters.getMaxZoom();
            Point screenResolution = new Point(width, height);
            Point cameraResolution = findBestPreviewSizeValue(parameters, screenResolution);
            parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
            parameters.set("jpeg-quality", 100);
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setPictureSize(cameraResolution.x, cameraResolution.y);
            parameters.setRotation(rotation);

//            parameters.getSupportedColorEffects();
//            parameters.setColorEffect(parameters.EFFECT_SEPIA);

            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(rotation);

            mCamera.setPreviewDisplay(mHolder);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }


    public void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }


    public void stopPreview() {
//        if (mCamera != null) {
//            mCamera.stopPreview();
//        }
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
//            stopPreview();
//            if (mCamera != null) {
//                mCamera.release();
//                mCamera = null;
//            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        mHolder = holder;
        if (mHolder.getSurface() == null) {
            return;
        }

        if (mCamera != null) {
            try {
                Camera.Parameters params = mCamera.getParameters();
                if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                mCamera.setParameters(params);

                mCamera.setPreviewDisplay(mHolder);
                startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void setZoom(int value) {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.isZoomSupported()) {
                if (value > parameters.getMaxZoom()) {
                    value = parameters.getMaxZoom();
                } else if (value < 0) {
                    value = 0;
                }
                parameters.setZoom(value);
                mCamera.setParameters(parameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int setFlash(int value) {
        int flash = 0;
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            if (m_CameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                switch (value) {
                    case 0:
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        flash = 1;
                        break;
                    case 1:
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                        flash = 2;
                        break;
                    default:
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        flash = 0;
                        break;
                }
                mCamera.setParameters(parameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flash;
    }

    public Boolean changeFlashState(int state) {
        try {
            if (!hasFlash()) {
                return false;
            }
            Camera.Parameters parameters = mCamera.getParameters();
            if (m_CameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                switch (state) {
                    case 0:
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        break;
                    case 1:
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        break;
                    case 2:
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                        break;
                }
                mCamera.setParameters(parameters);
            }
        }
        catch(Exception ignored){
            return false;
        }
        return true;
    }

    public boolean hasFlash() {
        if (mCamera == null) {
            return false;
        }
        Camera.Parameters parameters;
        try {
            parameters = mCamera.getParameters();
        } catch (RuntimeException ignored)  {
            return false;
        }

        if (parameters.getFlashMode() == null) {
            return false;
        }

        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || (supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF))) {
            return false;
        }

        return true;
    }

    public Point findBestSize(Camera.Parameters parameters, Point screenResolution) {

        List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            Camera.Size defaultSize = parameters.getPreviewSize();
            return new Point(defaultSize.width, defaultSize.height);
        }

        // Sort by size, descending
        List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(rawSupportedSizes);
        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;

                if (bPixels < aPixels) {
                    return -1;
                }

                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        StringBuilder previewSizesString = new StringBuilder();
        for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
            previewSizesString.append(supportedPreviewSize.width).append('x').append(supportedPreviewSize.height).append(' ');
        }

        Point bestSize = null;
//        float screenAspectRatio = (float) screenResolution.y / (float) screenResolution.x;
        float maxPixels = (float) screenResolution.y * (float) screenResolution.x;
        float diff = (float) 9 / (float) 16;
        for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
            int realWidth = supportedPreviewSize.width;
            int realHeight = supportedPreviewSize.height;
            int pixels = realWidth * realHeight;

            if (pixels < MIN_PREVIEW_PIXELS || pixels > maxPixels) {
                continue;
            }

            boolean isCandidatePortrait = realWidth < realHeight;
            int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
            int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;

            float aspectRatio = (float) maybeFlippedHeight / (float) maybeFlippedWidth;
            if (aspectRatio == diff) {
                bestSize = new Point(realWidth, realHeight);
                break;
            }
        }

        if (bestSize == null) {
            Camera.Size defaultSize = parameters.getPreviewSize();
            bestSize = new Point(defaultSize.width, defaultSize.height);
        }
        return bestSize;
    }

    public Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution) {
        // tránh trường hợp getSupportedPreviewSizes khác getSupportedPictureSizes dẫn tới không mở được camera
        List<Camera.Size> rawSupportedSizes = parameters.getSupportedPictureSizes(); // parameters.getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            Camera.Size defaultSize = parameters.getPreviewSize();
            return new Point(defaultSize.width, defaultSize.height);
        }

        // Sort by size, descending
        List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(rawSupportedSizes);
        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;

                if (bPixels < aPixels) {
                    return -1;
                }

                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        StringBuilder previewSizesString = new StringBuilder();
        for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
            previewSizesString.append(supportedPreviewSize.width).append('x').append(supportedPreviewSize.height).append(' ');
        }

        Point bestSize = null;
        float screenAspectRatio = (float) screenResolution.y / (float) screenResolution.x;

        float diff = Float.POSITIVE_INFINITY;
        for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
            int realWidth = supportedPreviewSize.width;
            int realHeight = supportedPreviewSize.height;
            int pixels = realWidth * realHeight;

            if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
                continue;
            }

            boolean isCandidatePortrait = realWidth < realHeight;
            int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
            int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;

            if (maybeFlippedWidth == screenResolution.y && maybeFlippedHeight == screenResolution.x) {
                Point exactPoint = new Point(realWidth, realHeight);
                return exactPoint;
            }

            float aspectRatio = (float) maybeFlippedWidth / (float) maybeFlippedHeight;
            float newDiff = Math.abs(aspectRatio - screenAspectRatio);

            if (newDiff < diff) {
                bestSize = new Point(realWidth, realHeight);
                diff = newDiff;
            }
        }

        if (bestSize == null) {
            Camera.Size defaultSize = parameters.getPreviewSize();
            bestSize = new Point(defaultSize.width, defaultSize.height);
        }
        return bestSize;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (event.getPointerCount() > 1) {
            try {
                if (mCamera != null) {
                    Camera.Parameters params = mCamera.getParameters();
                    // handle multi-touch events
                    if (action == MotionEvent.ACTION_POINTER_DOWN) {
                        mDist = getFingerSpacing(event);
                    } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                        mCamera.cancelAutoFocus();
                        handleZoom(event, params);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_UP) {
                try {
                    if (mCamera != null) {
                        Camera.Parameters params = mCamera.getParameters();
                        if (params != null) {
                            if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                            }
                            mCamera.setParameters(params);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        m_zoom = zoom;
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    /**
     * Determine the space between the first two fingers
     */
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // Switch
    public void onChangeSwicth() {
        Clear_Camera();

//        if (m_CameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
//            m_CameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
//        } else {
//            m_CameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
//        }
        surfaceCreated(mHolder);
        mCamera.startPreview();
    }
}
