package com.example.demonlp;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestOperations;

import java.io.IOException;

@SpringBootApplication
public class DemoNlpApplication
{

    static
    {
        //nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        //nu.pattern.OpenCV.loadLocally();
    }

    @Bean
    public RestOperations restOperations()
    {
        return new RestTemplateBuilder().build();
    }

    //brew install tesseract
    //brew install tesseract-lang
    //More Detail: https://www.baeldung.com/java-ocr-tesseract
    @Bean
    public Tesseract getTesseract() throws IOException
    {
        Tesseract instance = new Tesseract();
        instance.setDatapath("src/main/resources/data/");
        instance.setLanguage("eng");
        return instance;
    }

    public static void main(String[] args)
    {
        SpringApplication.run(DemoNlpApplication.class, args);
    }

}
