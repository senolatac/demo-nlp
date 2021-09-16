package com.example.demonlp.service.ocr;

import com.example.demonlp.service.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author sa
 * @date 14.09.2021
 * @time 15:31
 */
@Service
@RequiredArgsConstructor
public class TextAnalysisService
{
    private final Tesseract tesseract;

    private final ImageUtils imageUtils;

    public String extractAlphabetsFromImage(String urlPath)
    {
        try
        {
            return tesseract.doOCR(imageUtils.readImageFromUrl(urlPath));
        }
        catch (TesseractException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
