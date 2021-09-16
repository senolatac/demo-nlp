package com.example.demonlp.controller;

import com.example.demonlp.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sa
 * @date 14.09.2021
 * @time 14:08
 */
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class ApiController
{
    private final ColorAnalysisService colorAnalysisService;

    private final TextAnalysisService textAnalysisService;

    private final CaffeDeepNeuralNetworkService caffeDeepNeuralNetworkService;

    private final YoloDeepNeuralNetworkService yoloDNNService;

    private final FaceDetectionService faceDetectionService;

    /***
     *
     * @param url: imageUrl
     * @return ImageColor(color, hexFormat)
     */
    @PostMapping("color")
    public ResponseEntity<?> findColorOfImage(@RequestBody String url)
    {
        return ResponseEntity.ok(colorAnalysisService.findColorOfImage(url));
    }

    /***
     *
     * @param url: imageUrl
     * @return List of String => Texts in the image
     */
    @PostMapping("text")
    public ResponseEntity<?> findTextsOfImage(@RequestBody String url)
    {
        return ResponseEntity.ok(textAnalysisService.extractAlphabetsFromImage(url));
    }

    /***
     *
     * @param url: imageUrl
     * @return Map<Detection-Drawed-Local-File-URL, Object-Coordinates>
     */
    @PostMapping("caffe-dnn-object")
    public ResponseEntity<?> caffeModelFindObjectsOfImage(@RequestBody String url)
    {
        return ResponseEntity.ok(caffeDeepNeuralNetworkService.findObjectsInImage(url));
    }

    /***
     *
     * @param url: imageUrl
     * @return Map<Detection-Drawed-Local-File-URL, Object-Coordinates>
     */
    @PostMapping("yolo-dnn-object")
    public ResponseEntity<?> yoloModelFindObjectsOfImage(@RequestBody String url)
    {
        return ResponseEntity.ok(yoloDNNService.extractObjectsInImage(url));
    }

    /***
     *
     * @param url: imageUrl
     * @return Map<Detection-Drawed-Local-File-URL, Face-Count-In-Image>
     */
    @PostMapping("face")
    public ResponseEntity<?> findFacesInImage(@RequestBody String url)
    {
        return ResponseEntity.ok(faceDetectionService.extractFacesInImage(url));
    }
}
