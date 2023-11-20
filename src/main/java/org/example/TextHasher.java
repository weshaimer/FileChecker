package org.example;

import com.google.common.hash.Hashing;
import org.apache.commons.io.FileUtils;
import org.mozilla.universalchardet.UniversalDetector;

//import java.io.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
//import java.util.HashMap;
//import java.util.Map;

public class TextHasher {

//    public static void main(String[] args) {
//        // Массив расширений файлов, которые нужно хэшировать
//        String[] fileExtensions = {"txt", "md",
//                "html", "htm", "mhtml", "mht",
//                "css", "scss", "js", "json", "xml", "csv", "py", "java",
//                "c", "cpp", "rb", "php", "yaml", "toml", "ini",
//                "cfg", "bat", "sh", "sql", "rtf", "tex",
//                "lrc", "sub", "srt", "ass", "ssa", "vtt", "doc",
//                "docx", "docm", "dotx", "dotm", "dot", "odt",
//                "pdf", "xps"};
//        // Перебор всех указанных расширений
//        for (String fileExtension : fileExtensions) {
//            // Поиск файлов с указанным расширением в текущем каталоге
//            File[] files = new File(".").listFiles((dir, name) -> name.endsWith(fileExtension));
//
//            // Перебор найденных файлов
//            for (File file : files) {
//                try {
//                    //Вычисление SHA-256 хэша файла
//                    convertToUTF8IfNeeded(file);
//                    String hash = calculateSHA256(file);
//                    System.out.println(file.getName() + " - SHA-256: " + hash);
//                } catch (IOException | NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    public static String calculateSHA256(File file) throws IOException, NoSuchAlgorithmException {
        byte[] fileBytes = FileUtils.readFileToByteArray(file);

        // Определение кодировки файла
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(fileBytes, 0, fileBytes.length);
        detector.dataEnd();

        String detectedCharset = detector.getDetectedCharset();
        System.out.println(detectedCharset);

        // Проверка, если обнаруженная кодировка не UTF-8
        if (detectedCharset != null && !detectedCharset.equalsIgnoreCase("UTF-8")) {
            // Чтение содержимого в обнаруженной кодировке
            String content = new String(fileBytes, detectedCharset);

            // Вычисление SHA-256 хэша содержимого в UTF-8
            return Hashing.sha256().hashString(content, StandardCharsets.UTF_8).toString();
        } else {
            // Вычисление SHA-256 хэша содержимого в UTF-8 напрямую
            return Hashing.sha256().hashBytes(fileBytes).toString();
        }
    }





//    // Метод для вычисления SHA-256 хэша файла по содержанию
//    public static String calculateSHA256(File file) throws IOException, NoSuchAlgorithmException {
//        // Инициализация объекта MessageDigest для вычисления хэша
//        MessageDigest md = MessageDigest.getInstance("SHA-256");
//
//        // Создание потока для чтения файла с указанием кодировки UTF-8
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
//            String line;
//            // Чтение файла построчно
//            while ((line = reader.readLine()) != null) {
//                // Обновление хэша
//                md.update(line.getBytes(StandardCharsets.UTF_8));
//            }
//        }
//
//        // Получение байтового массива с хэшем
//        byte[] mdBytes = md.digest();
//
//        // Преобразование байтового массива в строку в шестнадцатеричном формате
//        StringBuilder hexString = new StringBuilder();
//        for (byte mdByte : mdBytes) {
//            String hex = Integer.toHexString(0xff & mdByte);
//            if (hex.length() == 1) hexString.append('0');
//            hexString.append(hex);
//        }
//
//        // Возвращение полученной строки с хэшем
//        return hexString.toString();
//    }
}
