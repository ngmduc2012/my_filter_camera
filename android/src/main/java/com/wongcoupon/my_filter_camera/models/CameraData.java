package com.wongcoupon.my_filter_camera.models;


import java.util.HashMap;
import java.util.Map;

public class CameraData {
    private Integer size;
    private Integer guidID;
    private String faceImage;
    private String flippedFaceImage;
    private Integer eKYCID;
    private Rot rot;

    public CameraData( Integer size, Integer guidID, String faceImage, String flippedFaceImage, Integer eKYCID, Rot rot) {
        this.size = size;
        this.guidID = guidID;
        this.faceImage = faceImage;
        this.flippedFaceImage = flippedFaceImage;
        this.eKYCID = eKYCID;
        this.rot = rot;
    }
    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("size", size);
        data.put("guidID", guidID);
        data.put("faceImage", faceImage);
        data.put("flippedFaceImage", flippedFaceImage);
        data.put("eKYCID", eKYCID);
        data.put("rot", rot != null ? rot.getData() : null);
        return data;
    }


    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getGuidID() {
        return guidID;
    }

    public void setGuidID(Integer guidID) {
        this.guidID = guidID;
    }

    public String getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
    }

    public String getFlippedFaceImage() {
        return flippedFaceImage;
    }

    public void setFlippedFaceImage(String flippedFaceImage) {
        this.flippedFaceImage = flippedFaceImage;
    }

    public Integer getEKYCID() {
        return eKYCID;
    }

    public void setEKYCID(Integer eKYCID) {
        this.eKYCID = eKYCID;
    }

    public Rot getRot() {
        return rot;
    }

    public void setRot(Rot rot) {
        this.rot = rot;
    }
}

