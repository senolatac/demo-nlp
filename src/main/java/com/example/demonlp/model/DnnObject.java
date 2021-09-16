package com.example.demonlp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencv.core.Point;

/**
 * @author sa
 * @date 14.09.2021
 * @time 18:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DnnObject
{
    private int objectClassId;
    private String objectName;
    private Point leftBottom;
    private Point rightTop;
    private Point centerCoordinate;
}
