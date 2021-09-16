package com.example.demonlp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sa
 * @date 15.09.2021
 * @time 11:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageColor
{
    private String color;
    private String hex;

    public String getHex()
    {
        if (hex == null) {
            return null;
        }
        return String.format("#%s", hex);
    }
}
