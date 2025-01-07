//ICamera_Mode_1Fragment

package com.wongcoupon.my_filter_camera.camera_fragment;

import static android.os.Build.VERSION_CODES.R;

import static androidx.camera.core.impl.utils.Threads.checkMainThread;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.flutter.view.TextureRegistry;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class ICamera_Mode_1Fragment extends Fragment {

//    public ICameraPreview icameraPreview;

    public PreviewView camera1Preview;
    public GPUImageView gpuImageViewMode1;

    public FrameLayout layoutCamera;
    public FrameLayout rootView;
    public ImageView imageView;

    int m_rotation = 0;

    public int capCnt = 0;
    private Point m_point;
    OrientationEventListener myOrientationEventListener;
    TextView date1Txt, date2Txt, date3Txt, date4Txt;
    TextView CamInfo1Txt, CamInfo2Txt, CamInfo3Txt, CamInfo4Txt;
    Handler m_Msghandler;

    public ImageView imageFrame;
    SharedPreferences sharedPreferences;

    Context m_Context;
    String cameraConfig = "";
    private boolean isSupportRotation = true;
    private boolean isSupportRotation180Degrees = true;

    public static final int SET_TIMER_LAYOUT_SIZE = 2112;
    public int iFilter = 9;

    public ICamera_Mode_1Fragment() {
    }

    @NonNull
    public Preview.SurfaceProvider getCameraSurfaceProvider(TextureRegistry.SurfaceTextureEntry textureEntry, Executor executor) {
        return request -> {
            SurfaceTexture texture = textureEntry.surfaceTexture();
            texture.setDefaultBufferSize(request.getResolution().getWidth(), request.getResolution().getHeight());
            Surface surface = new Surface(texture);
            request.provideSurface(surface, executor, result -> {
            });
        };
    }


//    public ICamera_Mode_1Fragment(Context context, Point point, ICameraPreview iCameraPreview) {
//        m_Context = context;
//        m_point = point;
//        icameraPreview = iCameraPreview;
//    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_icamera_mode_1, container, false);
//
//        if (m_Context == null) {
//            m_Context = getActivity();
//        }
//        if (m_point == null) {
//            m_point = MantaConfig.getPointScreen();
//        }
//
//        rootView = (FrameLayout) view.findViewById(R.id.rootView);
//        layoutCamera = (FrameLayout) view.findViewById(R.id.layoutCamera);
//
//        imageView = (ImageView) view.findViewById(R.id.imageView);
//
//        //#(1074) TuanHM
//        imageFrame = (ImageView) view.findViewById(R.id.imageFrame);
//
//        date1Txt = (TextView) view.findViewById(R.id.date1Txt);
//        date2Txt = (TextView) view.findViewById(R.id.date2Txt);
//        date3Txt = (TextView) view.findViewById(R.id.date3Txt);
//        date4Txt = (TextView) view.findViewById(R.id.date4Txt);
//
//        CamInfo1Txt = (TextView) view.findViewById(R.id.CamInfo1Txt);
//        CamInfo2Txt = (TextView) view.findViewById(R.id.CamInfo2Txt);
//        CamInfo3Txt = (TextView) view.findViewById(R.id.CamInfo3Txt);
//        CamInfo4Txt = (TextView) view.findViewById(R.id.CamInfo4Txt);
//
//        date1Txt.setText(MantaConfig.changeTypeDate("", false));
//        date2Txt.setText(MantaConfig.changeTypeDate("", false));
//        date3Txt.setText(MantaConfig.changeTypeDate("", false));
//        date4Txt.setText(MantaConfig.changeTypeDate("", false));
//
//        CamInfo1Txt.setText(cameraConfig);
//        CamInfo2Txt.setText(cameraConfig);
//        CamInfo3Txt.setText(cameraConfig);
//        CamInfo4Txt.setText(cameraConfig);
//
//        CamInfo1Txt.setVisibility(View.INVISIBLE);
//        CamInfo2Txt.setVisibility(View.INVISIBLE);
//        CamInfo3Txt.setVisibility(View.INVISIBLE);
//        CamInfo4Txt.setVisibility(View.INVISIBLE);
//
//        date1Txt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                date1Txt.setText(MantaConfig.changeTypeDate(date1Txt.getText().toString(), true));
//            }
//        });
//        date2Txt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                date2Txt.setText(MantaConfig.changeTypeDate(date2Txt.getText().toString(), true));
//            }
//        });
//        date3Txt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                date3Txt.setText(MantaConfig.changeTypeDate(date3Txt.getText().toString(), true));
//            }
//        });
//        date4Txt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                date4Txt.setText(MantaConfig.changeTypeDate(date4Txt.getText().toString(), true));
//            }
//        });
//
//        if (MantaConfig.setting_b_show_date_text) {
//            if (45 <= m_rotation && m_rotation < 135) {
//                date1Txt.setVisibility(View.INVISIBLE);
//                date2Txt.setVisibility(View.VISIBLE);
//                date3Txt.setVisibility(View.INVISIBLE);
//                date4Txt.setVisibility(View.INVISIBLE);
//
//                CamInfo1Txt.setVisibility(View.INVISIBLE);
//                CamInfo2Txt.setVisibility(View.VISIBLE);
//                CamInfo3Txt.setVisibility(View.INVISIBLE);
//                CamInfo4Txt.setVisibility(View.INVISIBLE);
//
//            } else if (135 <= m_rotation && m_rotation < 225) {
//                date1Txt.setVisibility(View.INVISIBLE);
//                date2Txt.setVisibility(View.INVISIBLE);
//                date3Txt.setVisibility(View.VISIBLE);
//                date4Txt.setVisibility(View.INVISIBLE);
//
//                CamInfo1Txt.setVisibility(View.INVISIBLE);
//                CamInfo2Txt.setVisibility(View.INVISIBLE);
//                CamInfo3Txt.setVisibility(View.VISIBLE);
//                CamInfo4Txt.setVisibility(View.INVISIBLE);
//
//            } else if (225 <= m_rotation && m_rotation < 315) {
//                date1Txt.setVisibility(View.INVISIBLE);
//                date2Txt.setVisibility(View.INVISIBLE);
//                date3Txt.setVisibility(View.INVISIBLE);
//                date4Txt.setVisibility(View.VISIBLE);
//
//                CamInfo1Txt.setVisibility(View.INVISIBLE);
//                CamInfo2Txt.setVisibility(View.INVISIBLE);
//                CamInfo3Txt.setVisibility(View.INVISIBLE);
//                CamInfo4Txt.setVisibility(View.VISIBLE);
//
//            } else {
//                date1Txt.setVisibility(View.VISIBLE);
//                date2Txt.setVisibility(View.INVISIBLE);
//                date3Txt.setVisibility(View.INVISIBLE);
//                date4Txt.setVisibility(View.INVISIBLE);
//
//                CamInfo1Txt.setVisibility(View.VISIBLE);
//                CamInfo2Txt.setVisibility(View.INVISIBLE);
//                CamInfo3Txt.setVisibility(View.INVISIBLE);
//                CamInfo4Txt.setVisibility(View.INVISIBLE);
//            }
//        } else {
//            date1Txt.setVisibility(View.INVISIBLE);
//            date2Txt.setVisibility(View.INVISIBLE);
//            date3Txt.setVisibility(View.INVISIBLE);
//            date4Txt.setVisibility(View.INVISIBLE);
//
//            CamInfo1Txt.setVisibility(View.INVISIBLE);
//            CamInfo2Txt.setVisibility(View.INVISIBLE);
//            CamInfo3Txt.setVisibility(View.INVISIBLE);
//            CamInfo4Txt.setVisibility(View.INVISIBLE);
//        }
//
//        camera1Preview = view.findViewById(R.id.camera1Preview);
//        camera1Preview.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
//        gpuImageViewMode1 = view.findViewById(R.id.giv_mode_1);
//        setDefaultFilter();
//        ListenerRotate();
//        return view;
//    }

//    public void setRootViewParams(int height) {
//        rootView.post(() -> {
//            FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams) rootView.getLayoutParams();
//            layout.gravity = Gravity.CENTER | Gravity.TOP;
//            layout.width = (int) (height * CanvasConfig.CANVAS_RATIO);
//            layout.height = height;
//            rootView.setLayoutParams(layout);
//            m_Msghandler.obtainMessage(SET_TIMER_LAYOUT_SIZE, 0, -1).sendToTarget();
//        });
//    }

    public void setHandler(Handler handler) {
        m_Msghandler = handler;
    }

//    // Fixed Defect #3347
//    public void ListenerRotate() {
//        try {
//            myOrientationEventListener = new OrientationEventListener(getContext(), SensorManager.SENSOR_DELAY_NORMAL) {
//                @Override
//                public void onOrientationChanged(int i) {
//                    if (i == ORIENTATION_UNKNOWN) {
//                        return;
//                    }
//                    m_rotation = i;
//
//                    if (MantaConfig.setting_b_show_date_text) {
//                        if (45 <= m_rotation && m_rotation < 135) {
//                            date1Txt.setVisibility(View.INVISIBLE);
//                            date2Txt.setVisibility(View.VISIBLE);
//                            date3Txt.setVisibility(View.INVISIBLE);
//                            date4Txt.setVisibility(View.INVISIBLE);
//
//                            CamInfo1Txt.setVisibility(View.INVISIBLE);
//                            CamInfo2Txt.setVisibility(View.VISIBLE);
//                            CamInfo3Txt.setVisibility(View.INVISIBLE);
//                            CamInfo4Txt.setVisibility(View.INVISIBLE);
//                            camera1Preview.setRotation(0);
//                            gpuImageViewMode1.setRotation(0);
//
//                        } else if (135 <= m_rotation && m_rotation < 225) {
//                            if (!isSupportRotation180Degrees) return;
//                            date1Txt.setVisibility(View.VISIBLE);
//                            date2Txt.setVisibility(View.INVISIBLE);
//                            date3Txt.setVisibility(View.INVISIBLE);
//                            date4Txt.setVisibility(View.INVISIBLE);
//
//                            CamInfo1Txt.setVisibility(View.VISIBLE);
//                            CamInfo2Txt.setVisibility(View.INVISIBLE);
//                            CamInfo3Txt.setVisibility(View.INVISIBLE);
//                            CamInfo4Txt.setVisibility(View.INVISIBLE);
//                            camera1Preview.setRotation(180);
//                            gpuImageViewMode1.setRotation(180);
//
//                        } else if (225 <= m_rotation && m_rotation < 315) {
//                            date1Txt.setVisibility(View.INVISIBLE);
//                            date2Txt.setVisibility(View.INVISIBLE);
//                            date3Txt.setVisibility(View.INVISIBLE);
//                            date4Txt.setVisibility(View.VISIBLE);
//
//                            CamInfo1Txt.setVisibility(View.INVISIBLE);
//                            CamInfo2Txt.setVisibility(View.INVISIBLE);
//                            CamInfo3Txt.setVisibility(View.INVISIBLE);
//                            CamInfo4Txt.setVisibility(View.VISIBLE);
//                            camera1Preview.setRotation(0);
//                            gpuImageViewMode1.setRotation(0);
//
//                        } else {
//                            date1Txt.setVisibility(View.VISIBLE);
//                            date2Txt.setVisibility(View.INVISIBLE);
//                            date3Txt.setVisibility(View.INVISIBLE);
//                            date4Txt.setVisibility(View.INVISIBLE);
//
//                            CamInfo1Txt.setVisibility(View.VISIBLE);
//                            CamInfo2Txt.setVisibility(View.INVISIBLE);
//                            CamInfo3Txt.setVisibility(View.INVISIBLE);
//                            CamInfo4Txt.setVisibility(View.INVISIBLE);
//                            camera1Preview.setRotation(0);
//                            gpuImageViewMode1.setRotation(0);
//                        }
//                    } else {
//                        if (45 <= m_rotation && m_rotation < 135) {
//                            CamInfo1Txt.setVisibility(View.INVISIBLE);
//                            CamInfo2Txt.setVisibility(View.VISIBLE);
//                            CamInfo3Txt.setVisibility(View.INVISIBLE);
//                            CamInfo4Txt.setVisibility(View.INVISIBLE);
//                            camera1Preview.setRotation(0);
//                            gpuImageViewMode1.setRotation(0);
//
//                        } else if (135 <= m_rotation && m_rotation < 225) {
//                            if (!isSupportRotation180Degrees) return;
//                            CamInfo1Txt.setVisibility(View.VISIBLE);
//                            CamInfo2Txt.setVisibility(View.INVISIBLE);
//                            CamInfo3Txt.setVisibility(View.INVISIBLE);
//                            CamInfo4Txt.setVisibility(View.INVISIBLE);
//                            camera1Preview.setRotation(180);
//                            gpuImageViewMode1.setRotation(180);
//
//                        } else if (225 <= m_rotation && m_rotation < 315) {
//                            CamInfo1Txt.setVisibility(View.INVISIBLE);
//                            CamInfo2Txt.setVisibility(View.INVISIBLE);
//                            CamInfo3Txt.setVisibility(View.INVISIBLE);
//                            CamInfo4Txt.setVisibility(View.VISIBLE);
//                            camera1Preview.setRotation(0);
//                            gpuImageViewMode1.setRotation(0);
//
//                        } else {
//                            CamInfo1Txt.setVisibility(View.VISIBLE);
//                            CamInfo2Txt.setVisibility(View.INVISIBLE);
//                            CamInfo3Txt.setVisibility(View.INVISIBLE);
//                            CamInfo4Txt.setVisibility(View.INVISIBLE);
//                            camera1Preview.setRotation(0);
//                            gpuImageViewMode1.setRotation(0);
//                        }
//                    }
//                }
//            };
//
//            if (myOrientationEventListener.canDetectOrientation()) {
//                //Only enable when supporting rotated 180 degrees
//                if (isSupportRotation) {
//                    myOrientationEventListener.enable();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    //#1081 Tuan add
    public void hideCamInfo() {
        imageFrame.setVisibility(View.INVISIBLE);
        CamInfo1Txt.setVisibility(View.INVISIBLE);
        CamInfo2Txt.setVisibility(View.INVISIBLE);
        CamInfo3Txt.setVisibility(View.INVISIBLE);
        CamInfo4Txt.setVisibility(View.INVISIBLE);
    }

    public void showCamInfo() {
        // #1082 fix error show frame and camera info
        imageFrame.setVisibility(View.VISIBLE);
        if (45 <= m_rotation && m_rotation < 135) {
            CamInfo1Txt.setVisibility(View.INVISIBLE);
            CamInfo2Txt.setVisibility(View.VISIBLE);
            CamInfo3Txt.setVisibility(View.INVISIBLE);
            CamInfo4Txt.setVisibility(View.INVISIBLE);
        } else if (135 <= m_rotation && m_rotation < 225) {
            CamInfo1Txt.setVisibility(View.INVISIBLE);
            CamInfo2Txt.setVisibility(View.INVISIBLE);
            CamInfo3Txt.setVisibility(View.VISIBLE);
            CamInfo4Txt.setVisibility(View.INVISIBLE);

        } else if (225 <= m_rotation && m_rotation < 315) {
            CamInfo1Txt.setVisibility(View.INVISIBLE);
            CamInfo2Txt.setVisibility(View.INVISIBLE);
            CamInfo3Txt.setVisibility(View.INVISIBLE);
            CamInfo4Txt.setVisibility(View.VISIBLE);

        } else {
            CamInfo1Txt.setVisibility(View.VISIBLE);
            CamInfo2Txt.setVisibility(View.INVISIBLE);
            CamInfo3Txt.setVisibility(View.INVISIBLE);
            CamInfo4Txt.setVisibility(View.INVISIBLE);
        }
    }

    public void setCameraInfo(String info) {

        if (CamInfo1Txt == null || CamInfo2Txt == null || CamInfo3Txt == null || CamInfo4Txt == null)
            return;
        CamInfo1Txt.setText(info);
        CamInfo2Txt.setText(info);
        CamInfo3Txt.setText(info);
        CamInfo4Txt.setText(info);
    }

    void setDefaultFilter() {
        iFilter = 9;
        GPUImageFilter filterDefault = new GPUImageFilter(
                GPUImageFilter.NO_FILTER_VERTEX_SHADER,
                GPUImageFilter.NO_FILTER_FRAGMENT_SHADER
        );
        gpuImageViewMode1.setFilter(filterDefault);
    }
}