package com.example.demonlp.service.utils;

import com.example.demonlp.model.DnnObject;
import com.example.demonlp.service.HttpRequestExecutor;
import lombok.RequiredArgsConstructor;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * @author sa
 * @date 14.09.2021
 * @time 15:35
 */
@Component
@RequiredArgsConstructor
public class ImageUtils
{
    private final HttpRequestExecutor httpRequestExecutor;

    public BufferedImage readImageFromUrl(String urlPath) throws IOException
    {
        URI uri = UriComponentsBuilder.fromHttpUrl(urlPath).build().toUri();
        RequestEntity<Void> requestEntity = RequestEntity.get(uri).build();

        // images takes too much resource, we won't use proxy
        byte[] imageContent = httpRequestExecutor.executeRequest(requestEntity, byte[].class);

        BufferedImage img;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageContent))
        {
            //We should download it as image. Otherwise, url also includes metadata of image.
            img = ImageIO.read(byteArrayInputStream);
            ImageIO.write(img, MediaType.IMAGE_PNG.getSubtype(), byteArrayOutputStream);
        }
        return img;
    }

    public Mat readFromUrlToOpenCVFrame(String urlPath) throws IOException
    {
        BufferedImage bufferedImage = readImageFromUrl(urlPath);
        String fileName = String.format("%s.png", UUID.randomUUID().toString());
        File file = new File(fileName);
        ImageIO.write(bufferedImage, MediaType.IMAGE_PNG.getSubtype(), file);
        return Imgcodecs.imread(file.getAbsolutePath());
    }

    public Mat drawDetectionsInFrame(Mat frame, List<DnnObject> detectedObjects)
    {
        for (DnnObject obj: detectedObjects)
        {
            Imgproc.rectangle(frame,obj.getLeftBottom(),obj.getRightTop(),new Scalar(255,0,0),1);
        }
        return frame;
    }

    public Mat drawDetectionsInFrame(Mat frame, MatOfRect faceDetections)
    {
        // Drawing boxes
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(
                    frame,                                               // where to draw the box
                    new Point(rect.x, rect.y),                            // bottom left
                    new Point(rect.x + rect.width, rect.y + rect.height), // top right
                    new Scalar(0, 0, 255),
                    3                                                     // RGB colour
            );
        }
        return frame;
    }

    public File frameToFile(Mat frame)
    {
        String outputFileName = String.format("%s.png", UUID.randomUUID().toString());
        Imgcodecs.imwrite(outputFileName,frame);

        return new File(outputFileName);
    }
}
