package ru.turkov;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageReader {
    public Color getPixelColor(BufferedImage bufferedImage, int x, int y) {
        return new Color(bufferedImage.getRGB(x, y));
    }
    public Integer getLightness(BufferedImage bufferedImage, int x, int y) {
        Color color = new Color(bufferedImage.getRGB(x, y));
        Double lightness = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
        return lightness.intValue();
    }
}
