package org.example;

import java.io.*;
import java.security.*;

public class TextHasher {
//    public static void main(String[] args) {
//        // Массив расширений файлов, которые нужно хэшировать
//        String[] fileExtensions = {".txt", ".md", ".html", ".css", ".js", ".json", ".xml", ".csv", ".log", ".py", ".java", ".c", ".cpp", ".rb", ".php", ".yaml", ".toml", ".ini", ".cfg", ".log", ".bat", ".sh", ".sql", ".rtf", ".tex", ".lrc", ".sub", ".srt", ".ass", ".ssa", ".vtt"};
//
//        // Перебор всех указанных расширений
//        for (String fileExtension : fileExtensions) {
//            // Поиск файлов с указанным расширением в текущем каталоге
//            File[] files = new File(".").listFiles((dir, name) -> name.endsWith(fileExtension));
//
//            // Перебор найденных файлов
//            for (File file : files) {
//                try {
//                    // Вычисление SHA-256 хэша файла
//                    String hash = calculateSHA256(file);
//                    System.out.println(file.getName() + " - SHA-256: " + hash);
//                } catch (IOException | NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    // Метод для вычисления SHA-256 хэша файла
    public static String calculateSHA256(File file) throws IOException, NoSuchAlgorithmException {
        // Инициализация объекта MessageDigest для вычисления хэша
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // Получение размера файла
        long fileSize = file.length();

        // Создание буфера для чтения данных из файла с размером, равным размеру файла
        byte[] dataBytes = new byte[(int) fileSize];

        // Создание потока для чтения файла
        FileInputStream fis = new FileInputStream(file);

        // Чтение данных из файла
        int bytesRead = fis.read(dataBytes);

        // Проверка, что количество прочитанных байт соответствует размеру файла
        if (bytesRead != fileSize) {
            throw new IOException("Ошибка чтения файла");
        }

        // Обновление хэша
        md.update(dataBytes);

        // Получение байтового массива с хэшем
        byte[] mdBytes = md.digest();

        // Преобразование байтового массива в строку в шестнадцатеричном формате
        StringBuilder hexString = new StringBuilder();
        for (byte mdByte : mdBytes) {
            String hex = Integer.toHexString(0xff & mdByte);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        // Возвращение полученной строки с хэшем
        return hexString.toString();
    }
}
