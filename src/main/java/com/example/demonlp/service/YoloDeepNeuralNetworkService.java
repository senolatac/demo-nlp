package com.example.demonlp.service;

import com.example.demonlp.model.DnnObject;
import com.example.demonlp.service.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.dnn.DetectionModel;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author sa
 * @date 15.09.2021
 * @time 16:07
 */
@Slf4j
@Service
public class YoloDeepNeuralNetworkService
{
    private Net net;

    private final ImageUtils imageUtils;

    private final List<String> classNames;

    //https://github.com/AlexeyAB/darknet/releases
    public YoloDeepNeuralNetworkService(ImageUtils imageUtils,
                          @Value("classpath:dnn/yolov4.cfg") Resource YOLO_MODEL_CFG_R,
                          @Value("classpath:dnn/yolov4.weights") Resource YOLO_WEIGHTS_R,
                          @Value("classpath:dnn/coco.names") Resource YOLO_NAMES_R) throws IOException
    {
        this.net = Dnn.readNetFromDarknet(YOLO_MODEL_CFG_R.getFile().getAbsolutePath(), YOLO_WEIGHTS_R.getFile().getAbsolutePath());
        classNames = Files.readAllLines(Paths.get(YOLO_NAMES_R.getFile().getAbsolutePath()));
        this.imageUtils = imageUtils;
    }

    public Map<String, List<DnnObject>> extractObjectsInImage(String urlPath)
    {
        try
        {
            Mat frame = imageUtils.readFromUrlToOpenCVFrame(urlPath);

            List<DnnObject> detectedObjects = getObjectsInFrame(frame);

            File outputFile = imageUtils.frameToFile(frame);

            return Map.of(outputFile.getAbsolutePath(), detectedObjects);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private List<DnnObject> getObjectsInFrame(Mat img)
    {
        DetectionModel model = new DetectionModel(net);
        double inScaleFactor = 0.00392;

        model.setInputParams(inScaleFactor, new Size(288, 288), new Scalar(0), true);
        model.setInputSize(288, 288);

        MatOfInt classIds = new MatOfInt();
        MatOfFloat scores = new MatOfFloat();
        MatOfRect boxes = new MatOfRect();
        model.detect(img, classIds, scores, boxes, 0.6f, 0.4f);

        List<DnnObject> objectList = new ArrayList<>();

        for (int i = 0; i < classIds.rows(); i++) {
            Rect box = new Rect(boxes.get(i, 0));
            Imgproc.rectangle(img, box, new Scalar(0, 255, 0), 2);

            int classId = (int) classIds.get(i, 0)[0];
            double score = scores.get(i, 0)[0];
            String text = String.format("%s: %.2f", classNames.get(classId), score);
            Imgproc.putText(img, text, new Point(box.x, box.y - 5),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);

            DnnObject dnnObject = new DnnObject(classId, text, box.tl(), box.br(), null);
            objectList.add(dnnObject);
        }

        return objectList;
    }
}
