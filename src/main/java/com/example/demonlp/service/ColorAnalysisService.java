package com.example.demonlp.service;

import com.example.demonlp.model.ImageColor;
import com.example.demonlp.service.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * @author sa
 * @date 14.09.2021
 * @time 13:53
 */
@Service
@RequiredArgsConstructor
public class ColorAnalysisService
{
    private final ImageUtils imageUtils;

    public ImageColor findColorOfImage(String urlPath)
    {
        try
        {
            Map imageColors = readImageAndFindImageColors(urlPath);
            return getMostCommonColour(imageColors);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private Map<Integer, Integer> readImageAndFindImageColors(String urlPath) throws IOException
    {
        BufferedImage image = imageUtils.readImageFromUrl(urlPath);

        int height = image.getHeight();
        int width = image.getWidth();

        Map<Integer, Integer> m = new HashMap();
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                int rgb = image.getRGB(i, j);
                int[] rgbArr = getRGBArr(rgb);
                // Filter out grays....
                if (!isGray(rgbArr))
                {
                    Integer counter = m.getOrDefault(rgb, 0);
                    counter++;
                    m.put(rgb, counter);
                }
            }
        }
        return m;
    }

    private ImageColor getMostCommonColour(Map<Integer, Integer> map)
    {
        List list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (Comparator) (o1, o2) -> ((Comparable) ((Map.Entry) (o1)).getValue())
                .compareTo(((Map.Entry) (o2)).getValue()));
        Map.Entry<Integer, Integer> me = (Map.Entry) list.get(list.size() - 1);
        int[] rgb = getRGBArr(me.getKey());

        return ImageColor.builder()
                .color(maxColor(rgb))
                .hex(toHexString(rgb[0]) + toHexString(rgb[1]) + toHexString(rgb[2]))
                .build();
    }

    private String maxColor(int[] array)
    {
        int maxAt = 0;

        for (int i = 0; i < array.length; i++)
        {
            maxAt = array[i] > array[maxAt] ? i : maxAt;
        }
        if (maxAt == 0)
        {
            return "red";
        }
        else if (maxAt == 1)
        {
            return "green";
        }
        else
        {
            return "blue";
        }
    }

    private String toHexString(int val)
    {
        String hex = Integer.toHexString(val);
        return hex.length() == 1 ? String.format("0%s", hex) : hex;
    }

    private int[] getRGBArr(int pixel)
    {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        return new int[]{red, green, blue};

    }

    private boolean isGray(int[] rgbArr)
    {
        int rgDiff = rgbArr[0] - rgbArr[1];
        int rbDiff = rgbArr[0] - rgbArr[2];
        // Filter out black, white and grays...... (tolerance within 10 pixels)
        int tolerance = 10;
        if (rgDiff > tolerance || rgDiff < -tolerance)
        {
            if (rbDiff > tolerance || rbDiff < -tolerance)
            {
                return false;
            }
        }
        return true;
    }
}
