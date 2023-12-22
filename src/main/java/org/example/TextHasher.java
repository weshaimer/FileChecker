package org.example;

import com.google.common.hash.Hashing;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Класс TextHasher предоставляет методы для вычисления хэшей текстовых файлов.
 */
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
    /**
     * Вычисляет SHA-256 хэш файла по его содержимому.
     *
     * @param file Файл, для которого требуется вычислить SHA-256 хэш.
     * @return SHA-256 хэш в виде строки.
     * @throws IOException              Возникает, если происходит ошибка ввода/вывода при чтении файла.
     * @throws NoSuchAlgorithmException Возникает, если алгоритм SHA-256 не поддерживается.
     */
    public static String calculateSHA256(File file) throws IOException, NoSuchAlgorithmException {
        // Чтение данных из файла с учетом кодировки UTF-8
        String content = com.google.common.io.Files.asCharSource(file, StandardCharsets.UTF_8).read();

        // Вычисление SHA-256 хэша по содержанию файла
        return Hashing.sha256().hashString(content, StandardCharsets.UTF_8).toString();
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
