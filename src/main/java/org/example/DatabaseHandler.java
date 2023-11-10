package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {

    private static final String JSON_PATH = "target/local_storage.json";
    private Gson gson;
    private List<FileInfo> files;

    public DatabaseHandler() {
        gson = new Gson();
        loadData();
    }

    private void loadData() {
        try (FileReader reader = new FileReader(JSON_PATH)) {
            Type listType = new TypeToken<ArrayList<FileInfo>>(){}.getType();
            files = gson.fromJson(reader, listType);

            if (files == null) {
                files = new ArrayList<>();
            }
        } catch (IOException e) {
            files = new ArrayList<>();
        }
    }

    private void saveData() {
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
        for (FileInfo file : files) {
            if (type.isEmpty() || file.type.equals(type)) {
                filteredFiles.add(file);
            }
        }
        return filteredFiles;
    }

    public List<FileInfo> fetchDocuments() {
        return fetchDocuments(""); // Вызывает метод с пустой строкой
    }

}
