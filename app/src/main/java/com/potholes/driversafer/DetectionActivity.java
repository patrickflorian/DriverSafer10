package com.potholes.driversafer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;

import com.potholes.View.Dialog.SimpleDialogBuilder;
import com.potholes.db.Potholes;
import com.potholes.detection.Detector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class DetectionActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    private static final String TAG = "OpenCVCamera";
    private CameraBridgeViewBase cameraBridgeViewBase;


    private AlertDialog baterryDialog;
    private AlertDialog potholeDialog;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:

                    cameraBridgeViewBase.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }

    };
    private Mat imageMAt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
        initDialog();


        cameraBridgeViewBase = findViewById(R.id.camera_view);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        checkBatteryLevel();

    }


    void initDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.exit_dialog_layout, null);

        SimpleDialogBuilder dialogBuilder = new SimpleDialogBuilder(this, alertView,
                "Low Battery level \n please plug on a charger or ", SimpleDialogBuilder.DIALOG_STYLE_DANGER);
        dialogBuilder.setCancelable(true);

        baterryDialog = dialogBuilder.create();

        dialogBuilder.setPositiveButtonListener("quitter", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baterryDialog.cancel();
                finish();
            }
        });

        dialogBuilder.setNegativeButtonListener("continuer", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baterryDialog.dismiss();
            }
        });
    }

    private void showEditBar() {
        Intent intent = new Intent(DetectionActivity.this, EditActivity.class);
        intent.putExtra("trou", new Potholes(0, 0, 25.6, true, 225));
        startActivity(intent);
    }

    private int getBatteryLevel() {
        int batteryLevel = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            if (batteryManager != null) {
                batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            }
        } else {
            Intent intent = new ContextWrapper(getApplicationContext()).
                    registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            batteryLevel = (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }

        return batteryLevel;
    }


    void checkBatteryLevel() {
        if (getBatteryLevel() < 40) {
            if (!baterryDialog.isShowing()) baterryDialog.show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV loaded successfully");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        Detector.MIN_POTHOLES_AREA = 50 * 25;
        Detector.Max_obj = 10;
    }

    @Override
    public void onCameraViewStopped() {
        try {
            showEditBar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mFinalMat = inputFrame.rgba();
        Detector.FRAME_HEIGHT = mFinalMat.height();
        Detector.FRAME_WIDTH = mFinalMat.width();
        Detector.MAX_POTHOLES_AREA = (Detector.FRAME_HEIGHT / 4) * (Detector.FRAME_WIDTH / 4);
        if (!Detector.hasfoundPothole) {
            Detector.detect(mFinalMat);
        } else {
            cameraBridgeViewBase.disableView();
        }

        imageMAt = mFinalMat.clone();
        return mFinalMat;

    }





}
