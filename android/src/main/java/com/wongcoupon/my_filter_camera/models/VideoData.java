package com.wongcoupon.my_filter_camera.models;


import java.util.HashMap;
import java.util.Map;

public class VideoData {
    private Integer size;
    private String video;
    private Integer eKYCID;
    private Rot rot;

    public VideoData(Integer size, String video, Integer eKYCID, Rot rot) {
        this.size = size;
        this.video = video;
        this.eKYCID = eKYCID;
        this.rot = rot;
    }
    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("size", size);
        data.put("video", video);
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


    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
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

