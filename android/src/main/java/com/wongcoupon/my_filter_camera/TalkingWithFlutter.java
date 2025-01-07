package com.wongcoupon.my_filter_camera;

import android.os.Handler;
import android.os.Looper;

import com.wongcoupon.my_filter_camera.models.CameraData;
import com.wongcoupon.my_filter_camera.models.VideoData;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;


public class TalkingWithFlutter {

    TalkingWithFlutter(Utils utils){
        this.utils = utils;
    }
    public MethodChannel methodChannel;

    public void remove(){
        methodChannel.setMethodCallHandler(null);
        methodChannel = null;
    }
    public void onTurnOnResponse(boolean result) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("user_accepted", result);
        invokeMethodUIThread("OnTurnOnResponse", map);
    }
    public void onRunningCamera(boolean result) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("isCameraRunning", result);
        invokeMethodUIThread("onRunningCamera", map);
    }

    public void onCameraTurnOnResponse(boolean result) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("user_accepted", result);
        invokeMethodUIThread("OnCameraTurnOnResponse", map);
    }

    // Implement sendResult method for scan results
    public void onSendResult(CameraData cameraData) {
        utils.log("onSendResult cameraData");
        HashMap<String, Object> event = new HashMap<>();
        event.put("name", "faceAndroid");
        event.put("data", cameraData.getData());
        utils.log("onSendResult cameraData 2");
        invokeMethodUIThread("OnSendResult", event);
    }
    // Implement sendResult method for record video
    public void onSendResultVideo(VideoData videoData) {
        utils.log("onSendResultVideo cameraData");
        HashMap<String, Object> event = new HashMap<>();
        event.put("name", "faceAndroid");
        event.put("data", videoData.getData());
        utils.log("onSendResult cameraData 2");
        invokeMethodUIThread("onSendResultVideo", event);
    }

    public void onConnectionStateChanged(int state) {
        utils.log("onConnectionStateChanged cameraData");
        HashMap<String, Object> map = new HashMap<>();
        map.put("state", state);
//        event.put("data", cameraData);
        utils.log("onConnectionStateChanged cameraData 2");
        invokeMethodUIThread("OnConnectionStateChanged", map);
    }


    public void registerTorchListener(int state) {
        HashMap<String, Object> torchState = new HashMap<>();
        torchState.put("name", "torchState");
        torchState.put("data", state);
        invokeMethodUIThread("OnTorchStateChanged", torchState);
    }


    private final Utils utils;

    // Send data back to Flutter plugin
    public void invokeMethodUIThread(final String method, HashMap<String, Object> data) {
        new Handler(Looper.getMainLooper()).post(() -> {
            //Could already be teared down at this moment
            if (methodChannel != null) {
                utils.log("invokeMethodUIThread");
                methodChannel.invokeMethod(method, data);
                utils.log("invokeMethodUIThread 2");
            } else {
                utils.log("invokeMethodUIThread: tried to call method on closed channel: " + method);
            }
        });
    }


    public void onListenStateChanged(int state) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("state", state);
        invokeMethodUIThread("OnListenStateChanged", map);
    }


}
