package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.util.Arrays;


public final class FileInfo {
//    private int id;
    private static final String[] IMAGE_EXTENSIONS =
        {
                "jpg","jpeg","jpe", "png","tif","tiff","svg","bmp","webp", "jfif", "heic","arw"
//            "jxr",  // ПО УМОЛЧАНИЮ НЕ ЧИТАЕТСЯ
        };
    private static final String[] TEXT_EXTENSIONS = {
            "txt", "md",
            "html", "htm", "mhtml", "mht",
            "css", "js", "json", "xml", "csv", "py", "java",
            "c", "cpp", "rb", "php", "yaml", "toml", "ini",
            "cfg", "log", "bat", "sh", "sql", "rtf", "tex",
            "lrc", "sub", "srt", "ass", "ssa", "vtt", "doc",
            "docx", "docm", "dotx", "dotm", "dot", "odt",
            "pdf", "xps"
    };
    private static final String[] AUDIO_EXTENSIONS = {};

    public final String absolutePath;
    public final String type;
    public String hash;

    // Конструктор

    public FileInfo(String absolutePath) {
        this.absolutePath = absolutePath;
        this.type = getType(getFileExtension(this.absolutePath));
    }
    public FileInfo(String absolutePath, String type, String hash) {
        this.absolutePath = absolutePath;
        this.type = type;
        this.hash = hash;
    }
    public static FileInfo get(String absolutePath) {

        return new FileInfo(absolutePath);
    }

    public String getFileExtension(String filePath) {
        if (filePath != null && filePath.lastIndexOf(".") != -1) {
            return filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
        } else {
            return "Unknown";
        }
    }

    public void calcHash() {
        switch (this.type) {
//            case "Image":
//                this.hash = calculatePHashImage(this.absolutePath);
//                break;
            case "Text":
                try {
                    this.hash = TextHasher.calculateSHA256(new File(this.absolutePath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "Image":
                try {
                    this.hash = PhotoHashing.calculatePHashPhoto(new File(this.absolutePath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                this.hash = getSHA256v2(this.absolutePath);
                break;
        }

    }

    public String getType(String extension) {
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
//
//
//    private String getSHA256(String filePath){
//        try {
//            File file = new File(filePath);
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            FileInputStream fis = new FileInputStream(file);
//            DigestInputStream dis = new DigestInputStream(fis, md);
//
//            byte[] buffer = new byte[4096];
//            while (dis.read(buffer) != -1) {
//                // Read the file content while updating the digest
//            }
//
//            byte[] hash = md.digest();
//
//            // Convert the byte array to a hexadecimal string
//            StringBuilder hexString = new StringBuilder();
//            for (byte b : hash) {
//                hexString.append(String.format("%02x", b));
//            }
//
//            dis.close();
//            return hexString.toString();
//        } catch (IOException | NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//    private  String getMD5(String filePath){ try {
//        File file = new File(filePath);
//        MessageDigest md = MessageDigest.getInstance("MD5");
//        FileInputStream fis = new FileInputStream(file);
//        DigestInputStream dis = new DigestInputStream(fis, md);
//
//        byte[] buffer = new byte[65536];
//        while (dis.read(buffer) != -1) {
//            // Read the file content while updating the digest
//        }
//
//        byte[] hash = md.digest();
//
//        // Convert the byte array to a hexadecimal string
//        StringBuilder hexString = new StringBuilder();
//        for (byte b : hash) {
//            hexString.append(String.format("%02x", b));
//        }
//
//        dis.close();
//        return hexString.toString();
//    } catch (IOException | NoSuchAlgorithmException e) {
//        e.printStackTrace();
//    }
//        return "";}
    private String getSHA256v2(String filePath){

        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[32768]; // Чтение 32 первых килобайт
            int bytesRead = fileInputStream.read(buffer);

            if (bytesRead > 0) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(buffer, 0, bytesRead);
                byte[] hash = digest.digest();

                // Преобразование хэша в шестнадцатеричную строку
                StringBuilder hexHash = new StringBuilder();
                for (byte b : hash) {
                    String hex = String.format("%02x", b);
                    hexHash.append(hex);
                }

                return hexHash.toString();
            }
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }
//    private String getSHA256v3(String filePath){
//        try {
//            File file = new File(filePath);
//            FileChannel fileChannel = new FileInputStream(file).getChannel();
//            ByteBuffer buffer = ByteBuffer.allocate(4096); // Увеличение размера буфера для более эффективного чтения
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            while (fileChannel.read(buffer) != -1) {
//                buffer.flip();
//                digest.update(buffer);
//                buffer.clear();
//            }
//            byte[] hash = digest.digest();
//            StringBuilder hexHash = new StringBuilder();
//            for (byte b : hash) {
//                hexHash.append(String.format("%02x", b));
//            }
//            fileChannel.close();
//            return hexHash.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "0";
//    }

//    public String calculatePHashImage(String imagePath) {
//        try {
//            BufferedImage image = ImageIO.read(new File(imagePath));
//            int size = 8; // Размер блока
//            int width = image.getWidth();
//            int height = image.getHeight();
//
//            // Шаг 2: Преобразование в черно-белое изображение
//            BufferedImage grayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
//            grayscaleImage.getGraphics().drawImage(image, 0, 0, null);
//
//            // Шаг 3: Уменьшение размера
//            BufferedImage resizedImage = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
//            resizedImage.getGraphics().drawImage(grayscaleImage, 0, 0, size, size, null);
//
//            // Шаг 4: Разбиение на блоки и вычисление среднего
//            long avgValue = 0;
//            long hash = 0;
//            for (int y = 0; y < size; y++) {
//                for (int x = 0; x < size; x++) {
//                    int pixel = resizedImage.getRGB(x, y);
//                    avgValue += (pixel & 0xff);
//                }
//            }
//            avgValue /= size * size;
//
//            // Шаг 6 и 7: Сравнение и создание pHash
//            for (int y = 0; y < size; y++) {
//                for (int x = 0; x < size; x++) {
//                    int pixel = resizedImage.getRGB(x, y);
//                    long bit = (pixel & 0xff) >= avgValue ? 1 : 0;
//                    hash = (hash << 1) | bit;
//                }
//            }
//
//            return Long.toHexString(hash);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "0";
//        }
//    }

    // Дополнительно: Метод toString() для удобного представления информации об объекте
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
