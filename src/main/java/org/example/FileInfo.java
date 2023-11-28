package org.example;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import java.io.*;
import java.util.Arrays;

/**
 * Класс FileInfo представляет информацию о файле, включая его абсолютный путь, тип и хэш.
 * Тип файла определяется на основе его расширения, а хэш рассчитывается в зависимости от типа файла.
 * Этот класс предоставляет методы для получения информации о файле и вычисления его хэша.
 */
public final class FileInfo {
    // Константы для поддерживаемых расширений файлов
    private static final String[] IMAGE_EXTENSIONS =
        {
                "jpg","jpeg","jpe", "png","tif","tiff",
            //    "svg", //FIXME ERROR WHEN SCANNING
                "bmp",
        //        "webp",//FIXME ERROR WHEN SCANNING
                "jfif", "heic","arw",
//            "jxr",  // FIXME ПО УМОЛЧАНИЮ НЕ ЧИТАЕТСЯ
        };
    private static final String[] TEXT_EXTENSIONS = {
            "txt", "md",
            "html", "htm", "mhtml", "mht",
            "css", "scss", "js", "json", "xml", "csv", "py", "java",
            "c", "cpp", "rb", "php", "yaml", "toml", "ini",
            "cfg", "bat", "sh", "sql", "rtf", "tex",
            "lrc", "sub", "srt", "ass", "ssa", "vtt", "doc",
            "docx", "docm", "dotx", "dotm", "dot", "odt",
            "pdf", "xps"
    };
    private static final String[] AUDIO_EXTENSIONS = {};

    // Основные свойства объекта FileInfo
    private final String absolutePath; // Абсолютный путь к файлу
    private final String type; // Тип файла (Image, Text, Audio, Other)
    private String hash; // Хэш-значение файла

    /**
     * Конструктор создает объект FileInfo с указанным абсолютным путем.
     *
     * @param absolutePath Абсолютный путь к файлу.
     */
    public FileInfo(String absolutePath) {
        this.absolutePath = absolutePath;
        this.type = setType(getFileExtension(this.absolutePath));
    }

    /**
     * Конструктор создает объект FileInfo с указанным абсолютным путем, типом и хэшем.
     *
     * @param absolutePath Абсолютный путь к файлу.
     * @param type         Тип файла (Image, Text, Audio, Other).
     * @param hash         Хэш-значение файла.
     */
    public FileInfo(String absolutePath, String type, String hash) {
        this.absolutePath = absolutePath;
        this.type = type;
        this.hash = hash;
    }

    /**
     * Возвращает объект FileInfo для указанного абсолютного пути.
     *
     * @param absolutePath Абсолютный путь к файлу.
     * @return Объект FileInfo для указанного файла.
     */
    public static FileInfo get(String absolutePath) {
        return new FileInfo(absolutePath);
    }

    /**
     * Получает тип файла.
     *
     * @return Тип файла (Image, Text, Audio, Other).
     */
    public String getType(){
        return type;
    }

    /**
     * Получает абсолютный путь к файлу.
     *
     * @return Абсолютный путь к файлу.
     */
    public String getAbsolutePath(){
        return absolutePath;
    }

    /**
     * Получает хэш-значение файла.
     *
     * @return Хэш-значение файла.
     */
    public String getHash(){
        return hash;
    }

    /**
     * Получает расширение файла из указанного пути к файлу.
     *
     * @param filePath Путь к файлу.
     * @return Расширение файла в нижнем регистре или "Unknown", если расширение не найдено.
     */
    private String getFileExtension(String filePath) {
        if (filePath != null && filePath.lastIndexOf(".") != -1) {
            return filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
        } else {
            return "Unknown";
        }
    }

    /**
     * Рассчитывает хэш-значение файла в зависимости от его типа.
     * Для текстовых файлов рассчитывается SHA256 с использованием TextHasher.
     * Для изображений рассчитывается PHash с использованием PhotoHashing.
     * Для других файлов рассчитывается SHA256 с указанным количеством байт.
     */
    public void calcHash() {
        switch (this.type) {
            case "Text" -> {
                try {
                    this.hash = TextHasher.calculateSHA256(new File(this.absolutePath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case "Image" -> {
                try {
                    this.hash = PhotoHashing.calculatePHashPhoto(new File(this.absolutePath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            default -> this.hash = getSHA256(this.absolutePath, 32768);
        }
    }

    /**
     * Устанавливает тип файла на основе его расширения.
     *
     * @param extension Расширение файла.
     * @return Тип файла (Image, Text, Audio, Other).
     */
    private String setType(String extension) {
        if (Arrays.asList(IMAGE_EXTENSIONS).contains(extension)) {
            return "Image";
        } else if (Arrays.asList(TEXT_EXTENSIONS).contains(extension)) {
            return "Text";
        } else if (Arrays.asList(AUDIO_EXTENSIONS).contains(extension)) {
            return "Audio";
        } else {
            return "Other"; // Если расширение не соответствует ни одной из заданных категорий
        }
    }

    /**
     * Рассчитывает SHA256-хэш файла для указанного количества байт.
     *
     * @param filePath  Путь к файлу.
     * @param byteCount Количество байт для вычисления хэша.
     * @return SHA256-хэш файла.
     */
    private String getSHA256(String filePath, Integer byteCount){
        ByteSource byteSource = Files.asByteSource(new File(filePath)).slice(0, byteCount);
        try {
            HashCode hashCode = byteSource.hash(Hashing.sha256());
            return hashCode.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "0";
        }
    }

    /**
     * Возвращает строковое представление объекта FileInfo.
     *
     * @return Строковое представление объекта FileInfo.
     */
    @Override
    public String toString() {
        return "FileInfo{" +
/*
                "id=" + id +
*/
                "filePath='" + absolutePath + '\'' +
                ", fileType='" + type + '\'' +
                ", fileHash='" + hash + '\'' +
                '}';
    }
}
