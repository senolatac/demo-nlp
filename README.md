## Natural-Language-Processing Demo Project
#### Object Detection, Face Detection, Color Analysis, Text Extraction

### Pre-Requirements
####Install Tesseract for OCR
```
//To use OCR text-detection system, you should install:
brew install tesseract
brew install tesseract-lang
//More Detail: https://www.baeldung.com/java-ocr-tesseract
```
####Install OpenCV
```
xcode-select --install
brew install ant
brew edit opencv
//In the text editor that will open, change the line -DBUILD_opencv_java=OFF to -DBUILD_opencv_java=ON, and save the file.
brew install --build-from-source opencv

//Then specify env variables like:
-Djava.library.path=/usr/local/Cellar/opencv/4.5.3_2/share/java/opencv4/

More detail: https://medium.com/macoclock/setting-up-mac-for-opencv-java-development-with-intellij-idea-fd2153eb634f
```

#### Download YOLO models and configuration
```
https://github.com/AlexeyAB/darknet/releases
```

### Libraries
1. OpenCV for Face-Detection and Object-Detection
2. Tesseract(tesse4j) for OCR, text-detection
