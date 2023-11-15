package org.example;

import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileChecker {
    public static Integer i = 0;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String rootPath = "";
        DatabaseHandlerSQL dbHandler = new DatabaseHandlerSQL();
        // Замените на путь к корневому каталогу вашей файловой системы
        long checkTime = System.currentTimeMillis();
        int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors(); // Количество потоков в пуле
        ExecutorService executorService;
        if (THREAD_POOL_SIZE > 4) executorService = Executors.newScheduledThreadPool(4);
        else executorService = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
        List<FileInfo> fetchDocuments = DatabaseHandlerSQL.checkFileSystem(rootPath);
        for (FileInfo file : fetchDocuments) {
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
        long hashTime = System.currentTimeMillis();
//         Сохраняем пути в файл
//        savePathsToFile(paths, "fileSystemPaths1.txt");
//        dbHandler.saveData();
        dbHandler.insertData(fetchDocuments);
        long endTime = System.currentTimeMillis();

        long timeCheck = checkTime - startTime;
        long timeHash = hashTime - checkTime;
        long timeElapsed = endTime - hashTime;
        System.out.println("Folders scan: " + timeCheck / 1000 + " Hashing: " + timeHash / 1000 + " SQL: " + timeElapsed / 1000);
//        for(FileInfo file : dbHandler.fetchDocuments(""))
//            System.out.println(file.toString());
        dbHandler.dublicateCheck("Text", Boolean.FALSE);
        dbHandler.dublicateCheck("Image", Boolean.FALSE);
        dbHandler.dublicateCheck("Other", Boolean.FALSE);
    }


//     public static void dublicateCheck(String fileNameFrom, String fileNameTo) {
//        Map<String, Set<String>> hashToFiles = new HashMap<>();
////        Map<String, String> imageHashes = new HashMap<>();
//        try (BufferedReader reader = new BufferedReader(new FileReader(fileNameFrom))) {
//            String line;
//            String currentPath, currentType, currentHash;
//            while ((line = reader.readLine()) != null) {
//                currentPath = line.split("PATH: ")[1].split(" TYPE: ")[0];
//                currentType = line.split(" TYPE: ")[1].split(" HASH: ")[0];
//                currentHash = line.split(" HASH: ")[1];
//                switch (currentType) {
//                    case "Image":
////                        imageHashes.put(currentPath, currentHash);
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

