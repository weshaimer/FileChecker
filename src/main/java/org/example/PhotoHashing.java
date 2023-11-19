package org.example;

import net.coobird.thumbnailator.Thumbnails;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import java.io.File;
import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;

public class PhotoHashing {

//    public static void main(String[] args) {
//        // Путь к директории с фотографиями
//        String directoryPath = "";
//
//        // Получаем список файлов в директории
//        List<File> imageFiles = getSupportedImageFiles(directoryPath);
//
//        // Вычисляем и выводим pHash для каждой фотографии
//        for (File imageFile : imageFiles) {
//            try {
//                String pHash = calculatePHashPhoto(imageFile);
//                System.out.println("File: " + imageFile.getName() + ", pHash: " + pHash);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private static List<File> getSupportedImageFiles(String directoryPath) {
//        // Получаем список поддерживаемых изображений из директории
//        List<File> imageFiles = new ArrayList<>();
//        File directory = new File(directoryPath);
//
//        if (directory.isDirectory()) {
//            for (File file : directory.listFiles()) {
//                if (file.isFile() && isSupportedImage(file)) {
//                    imageFiles.add(file);
//                }
//            }
//        }
//
//        return imageFiles;
//    }
//
//    private static boolean isSupportedImage(File file) {
//        // Проверяем, является ли файл поддерживаемым изображением
//        String fileName = file.getName().toLowerCase();
//        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".jpe") ||
//                fileName.endsWith(".png") || fileName.endsWith(".tif") || fileName.endsWith(".tiff") ||
//                fileName.endsWith(".svg") || fileName.endsWith(".bmp") || fileName.endsWith(".webp") ||
//                fileName.endsWith(".jfif") || fileName.endsWith(".heic") || fileName.endsWith(".arw") ||
//                fileName.endsWith(".jxr");
//    }

    public static String calculatePHashPhoto(File imageFile) throws IOException {
        // Изменяем размер изображения
        File resizedImage = resizeImage(imageFile);

        // Загружаем изображение
        MarvinImage image = MarvinImageIO.loadImage(resizedImage.getPath());

        // Выполняем дискретное косинусное преобразование (DCT)
        MarvinImagePlugin dct = new DCT();
        dct.process(image, image);

        // Вычисляем среднее значение DCT
        double averageDCT = computeAverageDCT(image);

        // Ещё сокращаем DCT
        reduceDCT(image, averageDCT);

        // Построим хэш
        String pHash = buildHash(image);

        // Удаляем временные файлы
        resizedImage.delete();

        // Преобразуем хэш в шестнадцатеричную систему счисления
        return convertToHex(pHash);

    }

    private static File resizeImage(File originalImage) throws IOException {
        // Изменяем размер изображения до 32x32 пикселей
        File resizedImage = new File("resized_" + originalImage.getName() + ".png");
        Thumbnails.of(originalImage).size(32, 32).outputFormat("png").toFile(resizedImage);
        return resizedImage;
    }

    private static double computeAverageDCT(MarvinImage image) {
        // Вычисляем среднее значение DCT
        double sum = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                sum += image.getIntComponent0(x, y);
            }
        }
        return sum / (image.getWidth() * image.getHeight());
    }

    private static void reduceDCT(MarvinImage image, double averageDCT) {
        // Сокращаем DCT
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getIntComponent0(x, y) > averageDCT) {
                    image.setIntColor(x, y, 255, 255, 255);
                } else {
                    image.setIntColor(x, y, 0, 0, 0);
                }
            }
        }
    }

    private static String buildHash(MarvinImage image) {
        // Построим хэш, представляющий изображение
        StringBuilder hash = new StringBuilder();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                hash.append(image.getIntComponent0(x, y) > 0 ? "1" : "0");
            }
        }
        return hash.toString();
    }
    private static String convertToHex(String binaryHash) {
        // Преобразуем бинарный хэш в шестнадцатеричный формат
        StringBuilder hexHash = new StringBuilder();
        for (int i = 0; i < binaryHash.length(); i += 4) {
            String nibble = binaryHash.substring(i, i + 4);
            int decimal = Integer.parseInt(nibble, 2);
            hexHash.append(Integer.toHexString(decimal));
        }
        return hexHash.toString();
    }
}
