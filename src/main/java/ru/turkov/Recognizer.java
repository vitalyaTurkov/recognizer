package ru.turkov;

import com.asprise.ocr.Ocr;

import javax.imageio.ImageIO;
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
        RecognizeImage image = new RecognizeImage(ImageIO.read(new File("./index.png")));

        image.binary();
        ImageIO.write(image, "jpg", new File("./new.jpg"));

        System.out.println(recognizer.recognize("./index.png"));
        System.out.println(recognizer.recognize("./new.jpg"));
    }
}
