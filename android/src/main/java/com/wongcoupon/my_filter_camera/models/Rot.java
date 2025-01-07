package com.wongcoupon.my_filter_camera.models;

import java.util.HashMap;
import java.util.Map;

public class Rot {
    private float rotX;
    private float rotY;
    private float rotZ;

    public Rot(float rotX, float rotY, float rotZ) {
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
    }
    public Map<String, Float> getData() {
        Map<String, Float> data = new HashMap<>();
        data.put("rotX", rotX);
        data.put("rotY", rotY);
        data.put("rotZ", rotZ);
        return data;
    }
    // Getters and setters for each field
    public float getRotX() {
        return rotX;
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public void setRotZ(float rotZ) {
        this.rotZ = rotZ;
    }
}
