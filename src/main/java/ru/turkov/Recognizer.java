package ru.turkov;

import com.asprise.ocr.Ocr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class Recognizer {

    public String recognize(File file) {
        Ocr.setUp();
        Ocr ocr = new Ocr();
        ocr.startEngine(Ocr.LANGUAGE_ENG, Ocr.SPEED_SLOW);
        return ocr.recognize(new File[] {file}, Ocr.RECOGNIZE_TYPE_TEXT, Ocr.OUTPUT_FORMAT_PLAINTEXT);
    }

    public String recognize(String filepath) {
        return recognize(new File(filepath));
    }

    public static void main(String[] args) throws IOException {
        Recognizer recognizer = new Recognizer();
        RecognizeImage image = new RecognizeImage(ImageIO.read(new File("./index.jpg")));

        image.buildUp();
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
