package com.potholes.detection;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Lelouch on 25/04/2018.
 */

public class Detector {

    public static int FRAME_WIDTH;
    public static int FRAME_HEIGHT;

    public static double MAX_POTHOLES_AREA;
    public static double MIN_POTHOLES_AREA;
    public static int Max_obj = 100;
    public static boolean hasfoundPothole = false;
    public static double surface = 0;
    private static Scalar hsvMin = new Scalar(0, 0, 0);
    private static Scalar hsvMax = new Scalar(50, 50, 250);


    // function to extract the road part in the image

    private static Mat extractRoadMask(Mat input) {
        FRAME_WIDTH = input.width();
        FRAME_HEIGHT = input.height();
        Mat hsv = new Mat();
        Mat dilate_element = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(5, 5));
        Imgproc.cvtColor(input, hsv, Imgproc.COLOR_BGR2HSV);

        Core.inRange(hsv, hsvMin, hsvMax, hsv);

        Imgproc.dilate(hsv, hsv, dilate_element);
        /// fincontours of the mask
        double largest_area = 0;
        double a = 0;
        int largest_contour_index = 0;
        List<MatOfPoint> roadContours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(hsv, roadContours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE); // Find the contours in the image
        int count = roadContours.size();
        if (count > 0) {
            for (int i = 0; i < count; i++) // iterate through each contour.
            {
                a = Imgproc.contourArea(roadContours.get(i), false);  //  Find the area of contour
                if (a > largest_area) {
                    largest_area = a;
                    largest_contour_index = i;                //Store the index of largest contour// Find the bounding rectangle for biggest contour
                }

            }
        }
        List<MatOfPoint> hullContours = new ArrayList<MatOfPoint>();
        MatOfInt hull = new MatOfInt();
        MatOfPoint road_contour = roadContours.get(largest_contour_index);
        Imgproc.convexHull(road_contour, hull);
        List<Point> l = new ArrayList<Point>();
        int[] intlist = hull.toArray();
        hullContours.clear();
        for (int i = 0; i < intlist.length; i++) {

            l.add(road_contour.toList().get(hull.toList().get(i)));
        }
        road_contour.fromList(l);

         /*MatOfPoint mopHull = new MatOfPoint();
         mopHull.create((int) hull.size().height, 1, CvType.CV_32SC2);

         for (int j = 0; j < hull.size().height; j++) {
             int index = (int)hull.get(j, 0)[0];
             double[] point = new double[] { road_contour.get(index, 0)[0], road_contour.get(index, 0)[1]};
             mopHull.put(j, 0, point);
             hullContours.add(mopHull);
         }
*/
        Mat Mask = Mat.zeros(hsv.size(), hsv.type());

        Imgproc.fillPoly(Mask, Arrays.asList(road_contour), new Scalar(255, 255, 255));

        return Mask;
    }


    public static boolean detect(Mat frame) {

        Mat input = new Mat();

        frame.copyTo(input);
        Mat mask = extractRoadMask(input);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        input.copyTo(input, mask);

        Imgproc.GaussianBlur(input, input, new Size(3, 3), 10);
        Imgproc.cvtColor(input, input, Imgproc.COLOR_BGR2GRAY);

        // applying the canny algorithms
        Imgproc.Canny(input, input, 50, 200);

        Mat dilate_element = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(5, 5));

        Imgproc.dilate(input, input, dilate_element);
        Imgproc.dilate(input, input, dilate_element);
        Imgproc.dilate(input, input, dilate_element);
        Imgproc.dilate(input, input, dilate_element);
        Imgproc.dilate(input, input, dilate_element);

        //find contours: detection des contours des nids de poules
        Imgproc.findContours(input, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        int obj_qty = contours.size();

        Rect boundRect;
        if (obj_qty > Max_obj) obj_qty = Max_obj;
        if (obj_qty > 1) {
            int contourIdx;

            Detector.hasfoundPothole = true;
            for (contourIdx = 0; contourIdx < obj_qty; contourIdx++) {

                double area = Imgproc.contourArea(contours.get(contourIdx));
                if (area > MIN_POTHOLES_AREA && area < MAX_POTHOLES_AREA) {
                    MatOfPoint2f approxCurve = new MatOfPoint2f();

                    MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(contourIdx).toArray());
                    double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;

                    surface = area;
                    Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

                    MatOfPoint points = new MatOfPoint(approxCurve.toArray());
                    // Get bounding rect of contour
                    boundRect = Imgproc.boundingRect(points);
                    Imgproc.rectangle(frame, new Point(boundRect.x, boundRect.y), new Point(boundRect.x + boundRect.width, boundRect.y + boundRect.height), new Scalar(255, 0, 0));

                }
            }

        }

        return hasfoundPothole;
    }
}
