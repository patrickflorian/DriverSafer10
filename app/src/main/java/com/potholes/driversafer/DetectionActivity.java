package com.potholes.driversafer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.potholes.View.Dialog.SimpleDialogBuilder;
import com.potholes.db.local.potholes.Potholes;
import com.potholes.db.local.potholes.PotholesDB;
import com.potholes.detection.Detector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class DetectionActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    private static final String TAG = "OpenCVCamera";
    private CameraBridgeViewBase cameraBridgeViewBase;


    private AlertDialog baterryDialog;
    private AlertDialog potholeDialog;

    private Intent intent;

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
    private boolean editIsShowwing = false;
    private Location lastLocation;

    BroadcastReceiver broadcastReceiverForDetection = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            lastLocation = intent.getParcelableExtra(GeoLocationService.LOCATION_DATA);
            Toast.makeText(context, lastLocation.toString(), Toast.LENGTH_SHORT).show();
        }
    };
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
        GeoLocationService.activity = DetectionActivity.this;

        GeoLocationService.start(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverForDetection,
                new IntentFilter(GeoLocationService.LOCATION_UPDATE));

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


    private Bitmap convertMatToBitMap(Mat img) {

        Bitmap bmp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);

        try {

            Utils.matToBitmap(img, bmp);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return bmp;
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
        Detector.hasfoundPothole = false;
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV loaded successfully");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        GeoLocationService.activity = DetectionActivity.this;
        GeoLocationService.start(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverForDetection,
                new IntentFilter(GeoLocationService.LOCATION_UPDATE));

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverForDetection);
        GeoLocationService.stop(this);
        super.onPause();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Detector.MIN_POTHOLES_AREA = 50 * 25;
        Detector.Max_obj = 10;

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mFinalMat = inputFrame.rgba();
        Detector.FRAME_HEIGHT = mFinalMat.height();
        Detector.FRAME_WIDTH = mFinalMat.width();
        Detector.MAX_POTHOLES_AREA = (Detector.FRAME_HEIGHT / 4) * (Detector.FRAME_WIDTH / 4);
        if (Detector.detect(mFinalMat)) {
            try {
                //if(!editIsShowwing){
                intent = new Intent(DetectionActivity.this, EditActivity.class);
                Potholes p = new Potholes(5.3491364, 10.423561, Detector.surface);

                //Création d'une instance de ma classe PotholesDB
                PotholesDB potholesDB = new PotholesDB(this);


                //On ouvre la base de données pour écrire dedans
                potholesDB.open();
                //On insère le trou que l'on vient de créer
                long result = potholesDB.insertPotholes(p);
                if (result != -1)
                    Log.d(TAG, "Adding potholes :: New row added, row id: " + result);
                else
                    Log.d(TAG, " Adding potholes : Something wrong");
                potholesDB.close();

                // intent.putExtra("trou", p);
//                intent.putExtra("img",convertMatToBitMap(mFinalMat));
//                startActivityForResult(intent, 2);
//                editIsShowwing = true;
                //}
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        imageMAt = mFinalMat.clone();
        return mFinalMat;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 2) {
            String message = data.getStringExtra("MESSAGE");
            if (message.equals("success"))
                Toast.makeText(this, "Saved success fully", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "failed to save", Toast.LENGTH_SHORT).show();
        }
        editIsShowwing = false;
    }





}
