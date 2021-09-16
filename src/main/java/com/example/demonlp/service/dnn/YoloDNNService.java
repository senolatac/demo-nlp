package com.example.demonlp.service.dnn;

import com.example.demonlp.model.DnnObject;
import com.example.demonlp.service.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
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
 * @time 14:23
 */
@Slf4j
@Service
public class YoloDNNService
{
    private Net net;

    private final ImageUtils imageUtils;

    private final List<String> classNames;

    public YoloDNNService(ImageUtils imageUtils,
                          @Value("classpath:data/dnn/yolov4.cfg") Resource YOLO_MODEL_CFG_R,
                          @Value("classpath:data/dnn/yolov4.weights") Resource YOLO_WEIGHTS_R,
                          @Value("classpath:data/dnn/coco.names") Resource YOLO_NAMES_R) throws IOException
    {
        this.net = Dnn.readNetFromDarknet(YOLO_MODEL_CFG_R.getFile().getAbsolutePath(), YOLO_WEIGHTS_R.getFile().getAbsolutePath());
        classNames = Files.readAllLines(Paths.get(YOLO_NAMES_R.getFile().getAbsolutePath()));
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

    private List<DnnObject> getObjectsInFrame(Mat frame, boolean isGrayFrame) {

        int inWidth = 288;
        int inHeight = 288;
        double inScaleFactor = 0.00392;


        List<Mat> result = new ArrayList<>();
        List<String> outBlobNames = getOutputNames(net);

        try {
            if (isGrayFrame)
                Imgproc.cvtColor(frame, frame, Imgproc.COLOR_GRAY2RGB);

            Mat blob = Dnn.blobFromImage(frame, inScaleFactor,
                    new Size(inWidth, inHeight),
                    new Scalar(0),
                    true, false);

            net.setInput(blob);
            net.forward(result, outBlobNames);

            float confThreshold = 0.6f; //Insert thresholding beyond which the model will detect objects//
            List<Integer> clsIds = new ArrayList<>();
            List<Float> confs = new ArrayList<>();
            List<Rect2d> rects = new ArrayList<>();
            log.info("sizee: {}", result.size());
            for (int i = 0; i < result.size(); ++i)
            {
                // each row is a candidate detection, the 1st 4 numbers are
                // [center_x, center_y, width, height], followed by (N-4) class probabilities
                Mat detections = result.get(i);
                for (int j = 0; j < detections.rows(); ++j)
                {
                    Mat row = detections.row(j);
                    Mat scores = row.colRange(5, detections.cols());
                    Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                    float confidence = (float)mm.maxVal;
                    Point classIdPoint = mm.maxLoc;
                    if (confidence > confThreshold)
                    {
                        int centerX = (int)(row.get(0,0)[0] * frame.cols()); //scaling for drawing the bounding boxes//
                        int centerY = (int)(row.get(0,1)[0] * frame.rows());
                        int width   = (int)(row.get(0,2)[0] * frame.cols());
                        int height  = (int)(row.get(0,3)[0] * frame.rows());
                        int left    = centerX - width  / 2;
                        int top     = centerY - height / 2;

                        clsIds.add((int)classIdPoint.x);
                        confs.add((float)confidence);
                        rects.add(new Rect2d(left, top, width, height));

                        log.info("level: {} clsId: {}", detections.get(j, 1)[0], classIdPoint.x);
                    }
                }
            }
            float nmsThresh = 0.5f;
            MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
            Rect2d[] boxesArray = rects.toArray(new Rect2d[0]);
            MatOfRect2d boxes = new MatOfRect2d(boxesArray);
            MatOfInt indices = new MatOfInt();
            Dnn.NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices); //We draw the bounding boxes for objects here//

            List<DnnObject> objectList = new ArrayList<>();
            int [] ind = indices.toArray();
            int j=0;
            for (int i = 0; i < ind.length; ++i)
            {
                int idx = ind[i];
                Rect2d box = boxesArray[idx];
                //Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(0,0,255), 2);
                //i=j;
                DnnObject dnnObject = new DnnObject(idx, classNames.get(idx), box.tl(), box.br(), null);
                objectList.add(dnnObject);
            }
            return objectList;

        } catch (Exception ex) {
            log.error("An error occurred DNN: ", ex);
        }
        return null;
    }

    private List<String> getOutputNames(Net net) {
        List<String> names = new ArrayList<>();

        List<Integer> outLayers = net.getUnconnectedOutLayers().toList();
        List<String> layersNames = net.getLayerNames();

        outLayers.forEach((item) -> names.add(layersNames.get(item - 1)));//unfold and create R-CNN layers from the loaded YOLO model//
        return names;
    }
}
