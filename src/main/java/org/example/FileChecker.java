package org.example;

import java.io.*;
import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileChecker {
    private static final String[] EXCLUDED_EXTENSIONS = {".sys", ".log", ".tmp", ".temp"}; // Укажите расширения, которые нужно исключить

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String rootPath = ""; // Замените на путь к корневому каталогу вашей файловой системы
        List<FileInfo> paths = checkFileSystem(rootPath);
        long midTime = System.currentTimeMillis();
        int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors(); // Количество потоков в пуле
        ExecutorService executorService;
        if (THREAD_POOL_SIZE > 4) executorService = Executors.newScheduledThreadPool(4);
        else executorService = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

        for (FileInfo file : paths) {
            executorService.execute(() -> {
                synchronized (file) {
                    file.calcHash();
                    if (i % 1000 == 0)
                        System.out.println(i);
                    i++;
                }
            });
        }
        // Завершаем пул потоков после завершения задач
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Сохраняем пути в файл
//        savePathsToFile(paths, "fileSystemPaths1.txt");
        long endTime = System.currentTimeMillis();

        long timeMidElapse = midTime - startTime;
        long timeElapsed = endTime - midTime;
        System.out.println("Folders scan: " + timeMidElapse / 1000 + " Hashing: " + timeElapsed / 1000);
//        dublicateCheck("fileSystemPaths1.txt", "dublicateList.txt");
    }

    public static Integer i = 0;
    public static Integer j = 0;

    public static List<FileInfo> checkFileSystem(String path) {
        List<FileInfo> FileInfos = new ArrayList<>();
        File file = new File(path);
        if (
                file.exists()
                        && !file.getName().startsWith(".")
                        && !file.getName().startsWith("_")
                        && isEditable(file)
                        && !hasExcludedExtension(file.getName())
                        && !isWindowsSystemDirectory(file)
        ) {
            if (file.isDirectory()
                    && !isHidden(file, path)
            ) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File subFile : files) {
                        FileInfos.addAll(checkFileSystem(subFile.getAbsolutePath()));
                    }
                }
            } else if (file.isFile()
                    && isEditable(file)
            ) {
                if (j % 10000 == 0)
                    System.out.println(j);
                j++;
//                System.out.println(file.getAbsolutePath());
                FileInfos.add(FileInfo.get(file.getAbsolutePath()));
            }
        } else {
//            System.out.println("Path does not exist: " + file.getAbsolutePath());
        }
        return FileInfos;
    }

    public static boolean isEditable(File file) {
        return file.canWrite();
    }

    //
    public static boolean isHidden(File file, String path) {
//        return false;
        return file.isHidden() && (file.getParentFile() != null);
    }

    public static boolean hasExcludedExtension(String fileName) {
        for (String extension : EXCLUDED_EXTENSIONS) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWindowsSystemDirectory(File file) {
        return file.getAbsolutePath().equalsIgnoreCase("C:\\Windows");
    }

//    public static void savePathsToFile(List<FileInfo> fis, String fileName) {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
//            for (FileInfo fi : fis) {
//                writer.write(
//                        "PATH: " + fi.absolutePath + " TYPE: " + fi.type + " HASH: " +
//                                fi.hashSHA256 + System.lineSeparator());
//            }
//        } catch (IOException e) {
//            System.err.println("An error occurred while writing to the file: " + e.getMessage());
//        }
//    }

//    public static void dublicateCheck(String fileNameFrom, String fileNameTo) {
//        Map<String, Set<String>> hashToFiles = new HashMap<>();
//        Map<String, String> imageHashes = new HashMap<>();
//        try (BufferedReader reader = new BufferedReader(new FileReader(fileNameFrom))) {
//            String line;
//            String currentPath, currentType, currentHash;
//            while ((line = reader.readLine()) != null) {
//                currentPath = line.split("PATH: ")[1].split(" TYPE: ")[0];
//                currentType = line.split(" TYPE: ")[1].split(" HASH: ")[0];
//                currentHash = line.split(" HASH: ")[1];
//                switch (currentType) {
//                    case "Image":
//                        imageHashes.put(currentPath, currentHash);
//                        break;
//                    case "Text":
//                        // Обработка типа "Text"
//                        // Добавьте свой код для этого типа
//                        break;
//                    case "Audio":
//                        // Обработка типа "Audio"
//                        // Добавьте свой код для этого типа
//                        break;
//                    default:
//                        // Проверяем, есть ли такой хэш в хэш-карте
//                        if (hashToFiles.containsKey(currentHash)) {
//                            hashToFiles.get(currentHash).add(currentPath);
//                        } else {
//                            Set<String> files = new HashSet<>();
//                            files.add(currentPath);
//                            hashToFiles.put(currentHash, files);
//                        }
//                        break;
//                }
//
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (Map.Entry<String, String> entry1 : imageHashes.entrySet()) {
//            String path1 = entry1.getKey();
//            String hash1 = entry1.getValue();
//            for (Map.Entry<String, String> entry2 : imageHashes.entrySet()) {
//                String path2 = entry2.getKey();
//                String hash2 = entry2.getValue();
//                if (!path1.equals(path2)) {
//                    int hammingDistance = calculateHammingDistance(hash1, hash2);
//
//                    System.out.println("Расстояние Хэмминга между " + path1 + " и " + path2 + ": " + hammingDistance);
//                    // Выводите результат на экран или сохраняйте в другую структуру данных, если нужно
//                }
//            }
//        }
//
//
//        try (FileWriter writer = new FileWriter(fileNameTo)) {
//            for (Map.Entry<String, Set<String>> entry : hashToFiles.entrySet()) {
//                if (entry.getValue().size() > 1) {
//                    writer.write("Хэш " + entry.getKey() + " встречается " + entry.getValue().size() + " раз в:" + System.lineSeparator());
//                    for (String filePath : entry.getValue()) {
//                        writer.write(filePath + System.lineSeparator());
//                    }
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("An error occurred while writing to the file: " + e.getMessage());
//        }
//
//
//    }
//
//    private static int calculateHammingDistance(String hash1, String hash2) {
//        if (hash1.length() != hash2.length()) {
//            return 100;
//        }
//
//        int distance = 0;
//        for (int i = 0; i < hash1.length(); i++) {
//            if (hash1.charAt(i) != hash2.charAt(i)) {
//                distance++;
//            }
//        }
//        return distance;
//    }
}

