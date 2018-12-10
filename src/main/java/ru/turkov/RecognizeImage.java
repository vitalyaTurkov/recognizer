package ru.turkov;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Hashtable;

public class RecognizeImage extends BufferedImage {
    public RecognizeImage(int width, int height, int imageType) {
        super(width, height, imageType);
    }

    public RecognizeImage(int width, int height, int imageType, IndexColorModel cm) {
        super(width, height, imageType, cm);
    }

    public RecognizeImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
        super(cm, raster, isRasterPremultiplied, properties);
    }

    public RecognizeImage(BufferedImage image) {
        super(image.getColorModel(), image.getRaster(), true, new Hashtable<>());
    }

    public Color getPixelColor(int x, int y) {
        return new Color(getRGB(x, y));
    }
    public Integer getLightness(int x, int y) {
        Color color = new Color(getRGB(x, y));
        Double lightness = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
        return lightness.intValue();
    }

    public RecognizeImage erosion() {
        RecognizeImage result = deepCopy();

        for (int i = 0; i < result.getWidth(); i++) {
            for (int j  = 0; j < result.getHeight(); j++) {
                setRGB(i, j, Color.WHITE.getRGB());
            }
        }

        for (int i = 1; i < this.getWidth() - 1; i++) {
            for (int j = 1; j < this.getHeight() - 1; j++) {
                if (result.getPixelColor(i, j + 1).equals(Color.BLACK) &&
                        result.getPixelColor(i, j - 1).equals(Color.BLACK) &&
                        result.getPixelColor(i - 1, j - 1).equals(Color.BLACK) &&
                        result.getPixelColor(i - 1, j + 1).equals(Color.BLACK) &&
                        result.getPixelColor(i - 1, j).equals(Color.BLACK) &&
                        result.getPixelColor(i + 1, j).equals(Color.BLACK) &&
                        result.getPixelColor(i + 1, j - 1).equals(Color.BLACK) &&
                        result.getPixelColor(i + 1, j + 1).equals(Color.BLACK)) {
                    setRGB(i, j, Color.BLACK.getRGB());
                }
            }
        }
        return this;
    }

    public RecognizeImage buildUp() {
        ArrayList<Coordinate> blackPixelsCoords = new ArrayList<>();
        for (int i = 1; i < getWidth() - 1; i++) {
            for (int j = 1; j < getHeight() - 1; j++) {
                if (getPixelColor(i, j).equals(Color.BLACK)) {
                    blackPixelsCoords.add(new Coordinate(i, j));
                }
            }
        }

        blackPixelsCoords.forEach(coordinate -> {
            this.setRGB(coordinate.x - 1, coordinate.y - 1, Color.BLACK.getRGB());
            this.setRGB(coordinate.x - 1, coordinate.y, Color.BLACK.getRGB());
            this.setRGB(coordinate.x - 1, coordinate.y + 1, Color.BLACK.getRGB());
            this.setRGB(coordinate.x, coordinate.y - 1, Color.BLACK.getRGB());
            this.setRGB(coordinate.x, coordinate.y + 1, Color.BLACK.getRGB());
            this.setRGB(coordinate.x + 1, coordinate.y - 1, Color.BLACK.getRGB());
            this.setRGB(coordinate.x + 1, coordinate.y, Color.BLACK.getRGB());
            this.setRGB(coordinate.x + 1, coordinate.y + 1, Color.BLACK.getRGB());
        });
        return this;
    }

    public RecognizeImage binary() {
        int threshold = getThreshold();

        for (int i = 1; i < getWidth() - 1; i++) {
            for (int j = 1; j < getHeight() - 1; j++) {
                if (getLightness(i, j) > threshold) {
                    setRGB(i, j, Color.WHITE.getRGB());
                } else {
                    setRGB(i, j, Color.BLACK.getRGB());
                }
            }
        }
        return this;
    }

    private class Coordinate {
        private Integer x;
        private Integer y;

        public Coordinate(Integer x, Integer y) {
            this.x = x;
            this.y = y;
        }
    }

    private RecognizeImage deepCopy() {
        ColorModel cm = this.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = this.copyData(null);
        return new RecognizeImage(cm, raster, isAlphaPremultiplied, null);
    }

    private int getThreshold() {
        int min = getLightness(0, 0);
        int max = getLightness(0, 0);

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                int lightness = getLightness(i, j);
                if (lightness < min) {
                    min = lightness;
                }
                if (lightness > max) {
                    max = lightness;
                }
            }
        }
        int histSize = max - min + 1;
        int[] hist = new int[histSize];

        for (int t = 0; t < histSize; t++)
            hist[t] = 0;

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                int lightness = getLightness(i, j);
                hist[lightness - min] += 1;
            }
        }

        int m = 0;
        int n = 0;
        for (int t = 0; t <= max - min; t++)
        {
            m += t * hist[t];
            n += hist[t];
        }

        float maxSigma = -1;
        int threshold = 0;

        int alpha1 = 0;
        int beta1 = 0;

        for (int t = 0; t < max - min; t++)
        {
            alpha1 += t * hist[t];
            beta1 += hist[t];

            float w1 = (float)beta1 / n;

            float a = (float)alpha1 / beta1 - (float)(m - alpha1) / (n - beta1);

            float sigma = w1 * (1 - w1) * a * a;

            if (sigma > maxSigma)
            {
                maxSigma = sigma;
                threshold = t;
            }
        }

        threshold += min;

        return threshold;
    }
}
