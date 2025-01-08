package com.wongcoupon.my_filter_camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.TextureRegistry;

/**
 * MyFilterCamera
 */
public class MyFilterCamera implements FlutterPlugin, MethodCallHandler, PluginRegistry.ActivityResultListener, ActivityAware , PluginRegistry.RequestPermissionsResultListener{

    private Context context;
    private Activity activity;
    private ActivityPluginBinding activityBinding;
    private TextureRegistry textureRegistry;
    private final Utils utils = new Utils();
    private final TalkingWithFlutter talkingWithFlutter = new TalkingWithFlutter(utils);
    private final Permission permission = new Permission();
    private CustomCameraViewFactory customCameraViewFactory;
    private PluginRegistry.RequestPermissionsResultListener listener;
    private CameraHandle camera;



    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        context = flutterPluginBinding.getApplicationContext();
        textureRegistry = flutterPluginBinding.getTextureRegistry();
        talkingWithFlutter.methodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "my_filter_camera/methods");
        talkingWithFlutter.methodChannel.setMethodCallHandler(this);
        // Initialize customCameraViewFactory only once
        if (customCameraViewFactory == null) {
            customCameraViewFactory = new CustomCameraViewFactory(context);
        }

        flutterPluginBinding
                .getPlatformViewRegistry()
                .registerViewFactory("custom_camera_view", customCameraViewFactory);

    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        context = null;
        talkingWithFlutter.remove();
        customCameraViewFactory=null;

    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CameraHandle.REQUEST_CODE) {
            talkingWithFlutter.onCameraTurnOnResponse(resultCode == Activity.RESULT_OK);
            return true;
        }
        return false;
    }

    @Override
    public boolean onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        if (listener != null) {
            return listener.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        return false;
    }




    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "checkPermission": {
                checkPermission(result);
                break;
            }
//            case "request": {
//                requestPermission(result);
//                break;
//            }
            case "start": {
                if (camera != null) {
                    camera.start(call, result);
                } else {
                    result.error("CameraError", "Camera is not initialized", null);
                }
                break;
            }
            case "stop": {
                if (camera != null) {
                    camera.stop(result);
                } else {
                    result.error("CameraError", "Camera is not initialized", null);
                }
                break;
            }

            case "torch": {
                if (camera != null) {
                    camera.toggleTorch(call, result);
                } else {
                    result.error("CameraError", "Camera is not initialized", null);
                }
                break;
            }
            // Take the photo (whole screen)
            case "capture": {
                if (camera != null) {
                    camera.capture(call, result);
                } else {
                    result.error("CameraError", "Camera is not initialized", null);
                }
                break;
            }
            // Take the photo within frame
           /* case "takePicture": {
                if (camera != null) {
                    camera.takePicture(call, result);
                } else {
                    result.error("CameraError", "Camera is not initialized", null);
                }
                break;
            }*/
            case "updateFilter": {
                camera.updateFilter(call,result);
                break;
            }
            case "startVideoRecording": {
                camera.startVideoRecording(call,result);
                break;
            }
            case "stopVideoRecording": {
                camera.stopVideoRecording(call,result);
                break;
            }
            case "pauseVideoRecording": {
                camera.pauseVideoRecording(call,result);
                break;
            }
            case "resumeVideoRecording": {
                camera.resumeVideoRecording(call,result);
                break;
            }
            case "applyImageSample": {
                camera.applyImageSample(call,result);
                break;
            }
            case "adjustBrightness": {
                camera.adjustBrightness(call,result);
                break;
            }
            case "adjustContrast": {
                camera.adjustContrast(call,result);
                break;
            }
            case "adjustGamma": {
                camera.adjustGamma(call,result);
                break;
            }
            case "adjustRGB": {
                camera.adjustRGB(call,result);
                break;
            }
            case "removeDir": {
                camera.removeDir(call,result);
                break;
            }
            default:
                result.notImplemented();
                break;
        }
    }
    private void checkPermission(@NonNull Result result) {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);

        if (activityBinding != null) {
            permission.ensurePermissions(permissions, context, activityBinding, (granted, perm) -> {
                if (!granted) {
                    result.error("checkPermission", String.format("FlutterCameraPlus requires %s permission", perm), null);
                    return;
                }
                result.success(true);
                talkingWithFlutter.onCameraTurnOnResponse(true);
            });
        } else {
            result.error("ActivityError", "Activity is not attached", null);
        }
    }

@Override
public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
    this.activityBinding = binding;
    this.camera = new CameraHandle(activity, textureRegistry, utils, talkingWithFlutter);
    camera.initCameraExecutor();
    // Gán listener cho phép xử lý kết quả quyền
    this.listener = permission;
    binding.addRequestPermissionsResultListener(permission);
}

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        this.activity = null;
        this.activityBinding = null;
        this.camera = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        this.activity = null;
        this.activityBinding = null;
        this.camera = null;
    }
}
