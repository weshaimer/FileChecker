package org.example;

import net.coobird.thumbnailator.Thumbnails;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import java.io.File;
import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;

/**
 * Класс PhotoHashing предоставляет методы для вычисления хэшей изображений.
 */
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
    /**
     * Вычисляет хэш изображения в формате pHash.
     *
     * @param imageFile Файл изображения, для которого требуется вычислить хэш.
     * @return Хэш изображения в формате pHash в виде строки.
     * @throws IOException Возникает, если происходит ошибка ввода/вывода при обработке изображения.
     */
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
    /**
     * Изменяет размер изображения до 32x32 пикселей.
     *
     * @param originalImage Исходное изображение.
     * @return Файл с измененным размером изображения.
     * @throws IOException Возникает, если происходит ошибка ввода/вывода при изменении размера изображения.
     */
    private static File resizeImage(File originalImage) throws IOException {
        File resizedImage = new File("resized_" + originalImage.getName() + ".png");
        Thumbnails.of(originalImage).size(32, 32).outputFormat("png").toFile(resizedImage);
        return resizedImage;
    }
    /**
     * Вычисляет среднее значение DCT изображения.
     *
     * @param image Изображение, для которого требуется вычислить среднее значение DCT.
     * @return Среднее значение DCT.
     */
    private static double computeAverageDCT(MarvinImage image) {
        double sum = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                sum += image.getIntComponent0(x, y);
            }
        }
        return sum / (image.getWidth() * image.getHeight());
    }
    /**
     * Сокращает DCT изображения.
     *
     * @param image      Изображение, для которого требуется сократить DCT.
     * @param averageDCT Среднее значение DCT.
     */
    private static void reduceDCT(MarvinImage image, double averageDCT) {
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
    /**
     * Построение хэша для изображения.
     *
     * @param image Изображение, для которого требуется построить хэш.
     * @return Хэш в бинарном формате в виде строки.
     */
    private static String buildHash(MarvinImage image) {
        StringBuilder hash = new StringBuilder();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                hash.append(image.getIntComponent0(x, y) > 0 ? "1" : "0");
            }
        }
        return hash.toString();
    }
    /**
     * Преобразует бинарный хэш в шестнадцатеричный формат.
     *
     * @param binaryHash Бинарный хэш в виде строки.
     * @return Шестнадцатеричный хэш в виде строки.
     */
    private static String convertToHex(String binaryHash) {
        StringBuilder hexHash = new StringBuilder();
        for (int i = 0; i < binaryHash.length(); i += 4) {
            String nibble = binaryHash.substring(i, i + 4);
            int decimal = Integer.parseInt(nibble, 2);
            hexHash.append(Integer.toHexString(decimal));
        }
        return hexHash.toString();
    }
}
