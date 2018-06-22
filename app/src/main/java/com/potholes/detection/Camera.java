package com.potholes.detection;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

/**
 * Created by Lelouch on 07/05/2018.
 */

public class Camera implements CameraBridgeViewBase.CvCameraViewListener2 {


    private static final String TAG = "OpenCVCamera";
    public Context context;
    private CameraBridgeViewBase cameraBridgeViewBase;
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this.context) {
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


    Camera(CameraBridgeViewBase camera, Context context) {
        this.context = context;
        cameraBridgeViewBase = camera;
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Detector.MIN_POTHOLES_AREA = 50 * 25;
        Detector.Max_obj = 4;
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
        Detector.detect(mFinalMat);

        return mFinalMat;
/*
        Imgproc.threshold(mIntermediateMat,mIntermediateMat,0, 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

// find contours:
        Imgproc.findContours(mIntermediateMat, contours, hierarchy, Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        int obj_qty =contours.size();

        Rect boundRect ;
        if(obj_qty > Max_obj) obj_qty = Max_obj;
        if(obj_qty>1  )
        {
            int contourIdx;
            for (contourIdx= 0; contourIdx < obj_qty ; contourIdx++) {

                double area = Imgproc.contourArea(contours.get(contourIdx));
                if (area > MIN_POTHOLES_AREA && area < MAX_POTHOLES_AREA )
                {
                    MatOfPoint2f         approxCurve = new MatOfPoint2f();

                    MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(contourIdx).toArray() );
                    double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;


                    Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

                    MatOfPoint points = new MatOfPoint( approxCurve.toArray() );
                    // Get bounding rect of contour
                    boundRect = Imgproc.boundingRect(points);
                    Imgproc.rectangle(mFinalMat, new Point(boundRect.x,boundRect.y), new Point(boundRect.x+boundRect.width,boundRect.y+boundRect.height),new Scalar(255,0,0));
                    Imgproc.drawContours(mFinalMat, contours, contourIdx, new Scalar(0, 0, 255), -1);
                }
            }
        }

}
*/
    }

    private void onActivityResume() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, context, baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV loaded successfully");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void disable() {
        try {
            cameraBridgeViewBase.disableView();
            cameraBridgeViewBase.setVisibility(SurfaceView.INVISIBLE);
        } catch (Exception e) {

        }

    }
}
