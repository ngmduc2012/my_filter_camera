package com.wongcoupon.my_filter_camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

//method
public class Utils {

    private static final String TAG = "[MyFilterCamera-Android]";

    void log(String message )
    {
        Log.d(TAG, message);
    }


    // Learn more ASCII: https://www.cs.cmu.edu/~pattis/15-1XX/common/handouts/ascii.html
     static String convertAsciiToString(int[] asciiValues) {
        StringBuilder sb = new StringBuilder();
        for (int value : asciiValues) {
            sb.append((char) value);
        }
        return sb.toString();
    }
    public Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    /**
     * Resized image size is less than maxW and maxH
     * Scale w : h is corresponding scale maxW : maxW*h/w when the width of image greater than
     * max width of image maxW
     * Scale w : h is corresponding scale maxH*w/h : maxH when the height of image greater than
     * max height of image maxH
     * =====================================================================================
     * Giám kích cỡ hình ảnh không quá maxW và maxH
     * Tỷ lệ w : h tương ứng với maxW : maxW*h/w khi chiều dài ảnh w lớn hơn chiều dài tối đa maxW
     * Tỷ lệ w : h tương ứng với maxH*w/h : maxH khi chiều cao ảnh h lớn hơn chiều cao tối đa maxH
     * */
    private Bitmap resizeImage(Bitmap bmp) {
        int maxW = 1024;
        int maxH = 1024;
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        if (w > maxW && h > maxH) {
            if ((double) maxW / w < (double) maxH / h) {
                return Bitmap.createScaledBitmap(bmp, maxW, maxW * h / w, true);
            } else {
                return Bitmap.createScaledBitmap(bmp, maxH * w / h, maxH, true);
            }
        } else if (w > maxW && h <= maxH) {
            return Bitmap.createScaledBitmap(bmp, maxW, maxW * h / w, true);
        } else if (w <= maxW && h > maxH) {
            return Bitmap.createScaledBitmap(bmp, maxH * w / h, maxH, true);
        } else {
            return bmp;
        }
    }
    public Bitmap toFlip(Bitmap bitmap, boolean xFlip, boolean yFlip) {
        Matrix matrix = new Matrix();
        matrix.postScale(
                xFlip ? -1f : 1f,
                yFlip ? -1f : 1f,
                bitmap.getWidth() / 2f,
                bitmap.getHeight() / 2f
        );
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public byte[] toJpeg(Bitmap bitmap, int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
    public Bitmap allocateBitmapIfNecessary(int width, int height, Bitmap bitmap) {
        if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        return bitmap;
    }


}
