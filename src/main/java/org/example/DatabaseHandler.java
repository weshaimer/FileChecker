package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.io.File;


public class DatabaseHandler {
    private static final String[] EXCLUDED_EXTENSIONS = {".sys", ".log", ".tmp", ".temp"}; // Укажите расширения, которые нужно исключить
    private static final String JSON_PATH = "target/local_storage.json";
    private Gson gson;
    public List<FileInfo> files;

    public DatabaseHandler(String rootPath) {
        gson = new Gson();
        loadData(rootPath);
    }
    private void loadData(String rootPath) {
        File jsonFile = new File(JSON_PATH);

        // Проверяем, существует ли файл
        if (jsonFile.exists()) {
            // Если файл существует, читаем данные из него
            try (FileReader reader = new FileReader(jsonFile)) {
                Type listType = new TypeToken<ArrayList<FileInfo>>(){}.getType();
                files = gson.fromJson(reader, listType);

                if (files == null) {
                    files = checkFileSystem(rootPath);
                }
            } catch (IOException e) {
                files = checkFileSystem(rootPath);
                e.printStackTrace();
            }
        } else {
            // Если файл не существует, инициализируем пустой список
            files = checkFileSystem(rootPath);
            // Здесь можно добавить дополнительные действия, например, создание файла
//            saveData(); // Это создаст пустой JSON файл
        }
    }
    public void saveData() {
        try (FileWriter writer = new FileWriter(JSON_PATH)) {
            gson.toJson(files, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertData(String filePath, String fileType, String fileHash) {
        FileInfo fileInfo = new FileInfo(filePath, fileType, fileHash);
        files.add(fileInfo);
        saveData();
    }

    public void clearDatabase() {
        files.clear();
        saveData();
    }

    public List<FileInfo> fetchDocuments(String type) {
        List<FileInfo> filteredFiles = new ArrayList<>();
        if (type.isEmpty()) {return filteredFiles;}
        for (FileInfo file : files) {
            if(file.type.equals(type)) {
                filteredFiles.add(file);
            }
        }
        return filteredFiles;
    }

    public List<FileInfo> fetchDocuments() {
        return fetchDocuments(""); // Вызывает метод с пустой строкой
    }

    private static Integer j = 0;
    private static List<FileInfo> checkFileSystem(String path) {
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
                FileInfos.add(FileInfo.get(file.getAbsolutePath()));
            }
        } else {
//            System.out.println("Path does not exist: " + file.getAbsolutePath());
        }
        return FileInfos;
    }

    private static boolean isEditable(File file) {
        return file.canWrite();
    }
    private static boolean isHidden(File file, String path) {
        return file.isHidden() && (file.getParentFile() != null);
    }
    private static boolean hasExcludedExtension(String fileName) {
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
    public void dublicateCheck(String type, Boolean isPartlyCheck) {
        Map<String, Set<String>> hashToFiles = new HashMap<>();
        String line;
        String currentPath, currentType, currentHash;
        List<FileInfo> localList = fetchDocuments(type);
                    if(!isPartlyCheck){
                        for (FileInfo file : localList) {
                            if (hashToFiles.containsKey(file.hash)) {
                                hashToFiles.get(file.hash).add(file.absolutePath);
                            } else {
                                Set<String> files = new HashSet<>();
                                files.add(file.absolutePath);
                                hashToFiles.put(file.hash, files);
                            }
                        }
                    } else {

                    }
        try (FileWriter writer = new FileWriter("target\\"+type+".txt")) {
            for (Map.Entry<String, Set<String>> entry : hashToFiles.entrySet()) {
                if (entry.getValue().size() > 1) {
                    writer.write("Хэш " + entry.getKey() + " встречается " + entry.getValue().size() + " раз в:" + System.lineSeparator());
                    for (String filePath : entry.getValue()) {
                        writer.write(filePath + System.lineSeparator());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
//                    break;
//                case "Image":
//                    if(!isPartlyCheck){
//                        for (FileInfo file : localList) {
//
//                        }
//                    } else {
//
//                    }
//                    break;
//
//                default:
//                    if (hashToFiles.containsKey(currentHash)) {
//                        hashToFiles.get(currentHash).add(currentPath);
//                    } else {
//                        Set<String> files = new HashSet<>();
//                        files.add(currentPath);
//                        hashToFiles.put(currentHash, files);
//                    }
//                    break;
            }




    }

