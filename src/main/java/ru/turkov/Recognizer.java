package ru.turkov;

import com.asprise.ocr.Ocr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Recognizer {

    private ImageReader imageReader = new ImageReader();

    public String recognize(File file) {
        Ocr.setUp();
        Ocr ocr = new Ocr();
        ocr.startEngine(Ocr.LANGUAGE_ENG, Ocr.SPEED_SLOW);
        return ocr.recognize(new File[] {file}, Ocr.RECOGNIZE_TYPE_TEXT, Ocr.OUTPUT_FORMAT_PLAINTEXT);
    }

    public String recognize(String filepath) {
        return recognize(new File(filepath));
    }

    public BufferedImage buildUp(BufferedImage image) {
        ArrayList<Coordinate> blackPixelsCoords = new ArrayList<>();
        for (int i = 1; i < image.getWidth() - 1; i++) {
            for (int j = 1; j < image.getHeight() - 1; j++) {
                if (imageReader.getPixelColor(image, i, j).equals(Color.BLACK)) {
                    blackPixelsCoords.add(new Coordinate(i, j));
                }
            }
        }

        blackPixelsCoords.forEach(coordinate -> {
            image.setRGB(coordinate.x - 1, coordinate.y - 1, Color.BLACK.getRGB());
            image.setRGB(coordinate.x - 1, coordinate.y, Color.BLACK.getRGB());
            image.setRGB(coordinate.x - 1, coordinate.y + 1, Color.BLACK.getRGB());
            image.setRGB(coordinate.x, coordinate.y - 1, Color.BLACK.getRGB());
            image.setRGB(coordinate.x, coordinate.y + 1, Color.BLACK.getRGB());
            image.setRGB(coordinate.x + 1, coordinate.y - 1, Color.BLACK.getRGB());
            image.setRGB(coordinate.x + 1, coordinate.y, Color.BLACK.getRGB());
            image.setRGB(coordinate.x + 1, coordinate.y + 1, Color.BLACK.getRGB());
        });

        return image;
    }

    public BufferedImage erosion(BufferedImage image) {
        BufferedImage result = deepCopy(image);

        for (int i = 0; i < result.getWidth(); i++) {
            for (int j  = 0; j < result.getHeight(); j++) {
                result.setRGB(i, j, Color.WHITE.getRGB());
            }
        }

        for (int i = 1; i < image.getWidth() - 1; i++) {
            for (int j = 1; j < image.getHeight() - 1; j++) {
                if (imageReader.getPixelColor(image, i, j + 1).equals(Color.BLACK) &&
                        imageReader.getPixelColor(image, i, j - 1).equals(Color.BLACK) &&
                        imageReader.getPixelColor(image, i - 1, j - 1).equals(Color.BLACK) &&
                        imageReader.getPixelColor(image, i - 1, j + 1).equals(Color.BLACK) &&
                        imageReader.getPixelColor(image, i - 1, j).equals(Color.BLACK) &&
                        imageReader.getPixelColor(image, i + 1, j).equals(Color.BLACK) &&
                        imageReader.getPixelColor(image, i + 1, j - 1).equals(Color.BLACK) &&
                        imageReader.getPixelColor(image, i + 1, j + 1).equals(Color.BLACK)) {
                    result.setRGB(i, j, Color.BLACK.getRGB());
                }
            }
        }

        return result;
    }

    public BufferedImage binary(BufferedImage image) {
        int threshold = getThreshold(image);

        for (int i = 1; i < image.getWidth() - 1; i++) {
            for (int j = 1; j < image.getHeight() - 1; j++) {
                if (imageReader.getLightness(image, i, j) > threshold) {
                    image.setRGB(i, j, Color.WHITE.getRGB());
                } else {
                    image.setRGB(i, j, Color.BLACK.getRGB());
                }
            }
        }
        return image;
    }

    private int getThreshold(BufferedImage image) {
        int min = imageReader.getLightness(image, 0, 0);
        int max = imageReader.getLightness(image, 0, 0);

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int lightness = imageReader.getLightness(image, i, j);
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

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int lightness = imageReader.getLightness(image, i, j);
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

    public static void main(String[] args) throws IOException {
        Recognizer recognizer = new Recognizer();

        BufferedImage image = recognizer.erosion(recognizer.buildUp(recognizer.binary(ImageIO.read(new File("./index.jpg")))));
        ImageIO.write(image, "jpg", new File("./new.jpg"));

        System.out.println(recognizer.recognize("./index.jpg"));
        System.out.println(recognizer.recognize("./new.jpg"));
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private class Coordinate {
        private Integer x;
        private Integer y;

        public Coordinate(Integer x, Integer y) {
            this.x = x;
            this.y = y;
        }
    }
}
