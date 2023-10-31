package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler {

    private static final String DB_PATH = "jdbc:sqlite:target/local_storage.db";

    public DatabaseHandler() {
        initDB();
    }

    private void initDB() {
        try (Connection conn = DriverManager.getConnection(DB_PATH)) {
            if (conn != null) {
                String createTableQuery = "CREATE TABLE IF NOT EXISTS files_info (" +
                        "id INTEGER PRIMARY KEY," +
                        "file_path TEXT NOT NULL," +
                        "file_type TEXT NOT NULL," +
                        "file_hash TEXT NOT NULL" +
                        ")";
                conn.createStatement().execute(createTableQuery);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertData(String filePath, String fileType, String fileHash) {
        String insertDataQuery = "INSERT INTO files_info(file_path, file_type, file_hash) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement(insertDataQuery)) {

            pstmt.setString(1, filePath);
            pstmt.setString(2, fileType);
            pstmt.setString(3, fileHash);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearDatabase() {
        String clearDBQuery = "DELETE FROM files_info";
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement(clearDBQuery)) {

            int deletedRows = pstmt.executeUpdate();
            System.out.println(deletedRows + " rows deleted successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<FileInfo> fetchDocuments(String type) {
        String fetchDataQuery = "SELECT * FROM files_info";
        List<FileInfo> documents = new ArrayList<>();

        if (!type.isEmpty()) {
            fetchDataQuery += String.format(" WHERE file_type =\"%s\" ",type); }
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement(fetchDataQuery)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                documents.add(new FileInfo(
//                        rs.getInt("id"),
                        rs.getString("file_path"),
                        rs.getString("file_type"),
                        rs.getString("file_hash")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return documents; // Возвращаем список документов
    }
}
