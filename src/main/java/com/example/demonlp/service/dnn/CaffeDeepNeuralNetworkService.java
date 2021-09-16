package com.example.demonlp.service.dnn;

import com.example.demonlp.model.DnnObject;
import com.example.demonlp.service.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author sa
 * @date 14.09.2021
 * @time 16:42
 */
@Slf4j
@Service
public class CaffeDeepNeuralNetworkService
{
    private Net net;
    private final String CAFFE_PROTO = "src/main/resources/data/dnn/MobileNetSSD_deploy.prototxt";
    private final String CAFFE_MODEL = "src/main/resources/data/dnn/MobileNetSSD_deploy.caffemodel";

    //https://github.com/theAIGuysCode/yolov4-deepsort/blob/master/data/classes/voc.names
    private final String[] classNames = {"background",
            "aeroplane", "bicycle", "bird", "boat",
            "bottle", "bus", "car", "cat", "chair",
            "cow", "diningtable", "dog", "horse",
            "motorbike", "person", "pottedplant",
            "sheep", "sofa", "train", "tvmonitor"};

    private final ImageUtils imageUtils;

    public CaffeDeepNeuralNetworkService(ImageUtils imageUtils)
    {
        this.net = Dnn.readNetFromCaffe(CAFFE_PROTO, CAFFE_MODEL);
        this.imageUtils = imageUtils;
    }

    public Map<String, List<DnnObject>> findObjectsInImage(String urlPath)
    {
        try
        {
            Mat frame = imageUtils.readFromUrlToOpenCVFrame(urlPath);

            List<DnnObject> detectedObjects = getObjectsInFrame(frame, false);

            imageUtils.drawDetectionsInFrame(frame, detectedObjects);

            File outputFile = imageUtils.frameToFile(frame);

            return Map.of(outputFile.getAbsolutePath(), detectedObjects);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private int getObjectCount(Mat frame, boolean isGrayFrame, String objectName)
    {

        int inWidth = 320;
        int inHeight = 240;
        double inScaleFactor = 0.007843;
        double thresholdDnn = 0.2;
        double meanVal = 127.5;

        int personObjectCount = 0;
        Mat blob = null;
        Mat detections = null;

        try
        {
            if (isGrayFrame)
            {
                Imgproc.cvtColor(frame, frame, Imgproc.COLOR_GRAY2RGB);
            }

            blob = Dnn.blobFromImage(frame, inScaleFactor,
                    new Size(inWidth, inHeight),
                    new Scalar(meanVal, meanVal, meanVal),
                    false, false);
            net.setInput(blob);
            detections = net.forward();
            detections = detections.reshape(1, (int) detections.total() / 7);
            for (int i = 0; i < detections.rows(); ++i)
            {
                double confidence = detections.get(i, 2)[0];

                if (confidence < thresholdDnn)
                {
                    continue;
                }

                int classId = (int) detections.get(i, 1)[0];
                if (classNames[classId].toString() != objectName.toLowerCase())
                {
                    continue;
                }
                personObjectCount++;
            }
        }
        catch (Exception ex)
        {
            log.error("An error occurred DNN: ", ex);
        }
        return personObjectCount;
    }

    private List<DnnObject> getObjectsInFrame(Mat frame, boolean isGrayFrame)
    {

        int inWidth = 320;
        int inHeight = 240;
        double inScaleFactor = 0.007843;
        double thresholdDnn = 0.2;
        double meanVal = 127.5;

        Mat blob = null;
        Mat detections = null;
        List<DnnObject> objectList = new ArrayList<>();

        int cols = frame.cols();
        int rows = frame.rows();

        try
        {
            if (isGrayFrame)
            {
                Imgproc.cvtColor(frame, frame, Imgproc.COLOR_GRAY2RGB);
            }

            blob = Dnn.blobFromImage(frame, inScaleFactor,
                    new Size(inWidth, inHeight),
                    new Scalar(meanVal, meanVal, meanVal),
                    false, false);

            net.setInput(blob);
            detections = net.forward();
            System.out.println("geldi " + detections.total());
            detections = detections.reshape(1, (int) detections.total() / 7);

            //all detected objects
            for (int i = 0; i < detections.rows(); ++i)
            {
                double confidence = detections.get(i, 2)[0];

                if (confidence < thresholdDnn)
                {
                    continue;
                }

                int classId = (int) detections.get(i, 1)[0];

                //calculate position
                int xLeftBottom = (int) (detections.get(i, 3)[0] * cols);
                int yLeftBottom = (int) (detections.get(i, 4)[0] * rows);
                Point leftPosition = new Point(xLeftBottom, yLeftBottom);

                int xRightTop = (int) (detections.get(i, 5)[0] * cols);
                int yRightTop = (int) (detections.get(i, 6)[0] * rows);
                Point rightPosition = new Point(xRightTop, yRightTop);

                float centerX = (xLeftBottom + xRightTop) / 2;
                float centerY = (yLeftBottom - yRightTop) / 2;
                Point centerPoint = new Point(centerX, centerY);

                DnnObject dnnObject = new DnnObject(classId, classNames[classId].toString(), leftPosition, rightPosition, centerPoint);
                objectList.add(dnnObject);
            }

        }
        catch (Exception ex)
        {
            log.error("An error occurred DNN: ", ex);
        }
        return objectList;
    }
}
