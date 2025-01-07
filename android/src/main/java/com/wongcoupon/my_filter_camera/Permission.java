package com.wongcoupon.my_filter_camera;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.PluginRegistry;

public class Permission implements PluginRegistry.RequestPermissionsResultListener {

    @Override
    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        OperationOnPermission operation = operationsOnPermission.get(requestCode);

        if (operation != null && grantResults.length > 0) {
            operation.op(grantResults[0] == PackageManager.PERMISSION_GRANTED, permissions[0]);
            return true;
        } else {
            return false;
        }
    }

    private int lastEventId = 2452;

    public interface OperationOnPermission {
        void op(boolean granted, String permission);
    }

    private final Map<Integer, OperationOnPermission> operationsOnPermission = new HashMap<>();

    void ensurePermissions(List<String> permissions, Context context, ActivityPluginBinding binding, OperationOnPermission operation) {
        // only request permission we don't already have
        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (permission != null && ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        // no work to do?
        if (permissionsNeeded.isEmpty()) {
            operation.op(true, null);
            return;
        }

        askPermission(permissionsNeeded, operation, binding);
    }

    private void askPermission(List<String> permissionsNeeded, OperationOnPermission operation, ActivityPluginBinding binding) {
        // finished asking for permission? call callback
        if (permissionsNeeded.isEmpty()) {
            operation.op(true, null);
            return;
        }

        String nextPermission = permissionsNeeded.remove(0);
        //Save callback and request permission
        operationsOnPermission.put(lastEventId, (granted, perm) -> {
            operationsOnPermission.remove(lastEventId);
            if (!granted) {
                operation.op(false, perm);
                return;
            }
            // recursively ask for next permission
            askPermission(permissionsNeeded, operation, binding);
        });
        //request permission
        ActivityCompat.requestPermissions(
//                activityBinding.op(),
                binding.getActivity(),
                new String[]{nextPermission},
                lastEventId);

        lastEventId++;
    }
}
