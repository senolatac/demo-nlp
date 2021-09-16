package com.example.demonlp.service.face;

import com.example.demonlp.model.DnnObject;
import com.example.demonlp.service.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author sa
 * @date 15.09.2021
 * @time 11:27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaceDetectionService
{
    private static final CascadeClassifier classifier = new CascadeClassifier("src/main/resources/data/face/lbpcascade_frontalface.xml");

    private final ImageUtils imageUtils;

    public Map<String, Integer> extractFacesInImage(String urlPath)
    {
        try
        {
            Mat frame = imageUtils.readFromUrlToOpenCVFrame(urlPath);
            // Detecting the face in the snap
            MatOfRect faceDetections = new MatOfRect();
            classifier.detectMultiScale(frame, faceDetections);
            log.info("Detected {} faces", faceDetections.toArray().length);

            imageUtils.drawDetectionsInFrame(frame, faceDetections);

            File outputFile = imageUtils.frameToFile(frame);

            return Map.of(outputFile.getAbsolutePath(), faceDetections.toArray().length);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
