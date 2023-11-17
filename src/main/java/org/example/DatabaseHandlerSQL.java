package org.example;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class DatabaseHandlerSQL {
    private static final String[] EXCLUDED_EXTENSIONS = {".sys", ".log", ".tmp", ".temp"};
    private static final String DB_PATH = "jdbc:sqlite:target/local_storage.db";
    private static Connection conn;
//    public static Statement statmt;

    public DatabaseHandlerSQL() {
        try  {
        initDB();
    } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initDB() throws SQLException, ClassNotFoundException {
            Class.forName("org.sqlite.JDBC");
            conn = connect();
            Statement statmt = conn.createStatement();
            System.out.println("База Подключена!");
            if (conn != null) {
                String createTableQuery = "CREATE TABLE IF NOT EXISTS files_info (" +
                        "id INTEGER PRIMARY KEY," +
                        "file_path TEXT NOT NULL," +
                        "file_type TEXT NOT NULL," +
                        "file_hash TEXT NOT NULL" +
                        ")";
                statmt.execute(createTableQuery);
            }
            conn.close();
            statmt.close();
            }
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_PATH);
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
        //return FileInfos;
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
    public void insertData(List<FileInfo> files) {
        String insertDataQuery = "INSERT INTO files_info(file_path, file_type, file_hash) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement(insertDataQuery)) {
            conn.setAutoCommit(false);
            int he = 0;
            for(FileInfo file: files){
                he++;
                if(he%10000==0) System.out.println(he);
            pstmt.setString(1, file.getAbsolutePath());
            pstmt.setString(2, file.getType());
            pstmt.setString(3, file.getHash());
            pstmt.addBatch();

            }
            pstmt.executeBatch();
            conn.commit();
            System.out.println("ХИЛИМСЯ, ЖИВЕМ!!!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        System.out.println("help");
    }
    private static Integer j = 0;
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
                    && !isHidden(file)
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
    private static boolean isHidden(File file) {
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
        return file.getAbsolutePath().equalsIgnoreCase(System.getenv("windir"));
    }
    public Map<String, Set<String>> dublicateCheck(String type, Boolean isPartlyCheck) {
        Map<String, Set<String>> hashToFiles = new HashMap<>();
//        List<FileInfo> localList = fetchDocuments(type);
        if(!isPartlyCheck){
            hashToFiles = dublicateCheckFull(type);
//            System.out.println(hashToFiles);
        } else {
            //TODO Partly cases
        }
        for (Map.Entry<String, Set<String>> entry : hashToFiles.entrySet()) {
            String fileHash = entry.getKey();
            Set<String> filePaths = entry.getValue();
            System.out.println("file_hash: " + fileHash);
            System.out.println("file_paths: " + filePaths);
            System.out.println();
        }
        return hashToFiles;
}
    private Map<String, Set<String>> dublicateCheckFull(String type){
        Map<String, Set<String>> fileHashToPathsMap = new HashMap<>();

        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
//            connection.setAutoCommit(false);
            String sql = "SELECT file_path, file_hash FROM files_info " +
                    "WHERE file_type = ? " +
                    "AND file_hash IN (" +
                    "    SELECT file_hash " +
                    "    FROM files_info " +
                    "    WHERE file_type = ? " +
                    "    GROUP BY file_hash " +
                    "    HAVING COUNT(*) > 1" +
                    ")";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, type);
                preparedStatement.setString(2, type);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String filePath = resultSet.getString("file_path");
                        String fileHash = resultSet.getString("file_hash");

                        // Проверяем, есть ли уже такой file_hash в Map
                        if (fileHashToPathsMap.containsKey(fileHash)) {
                            fileHashToPathsMap.get(fileHash).add(filePath);
                        } else {
                            // Если нет, создаем новую запись
                            Set<String> filePaths = new HashSet<>();
                            filePaths.add(filePath);
                            fileHashToPathsMap.put(fileHash, filePaths);
                        }
                    }
                }
            }
//        connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Теперь fileHashToPathsMap содержит соответствие между file_hash и списком file_path

        return fileHashToPathsMap;
    }
public List<FileInfo> fetchDocuments(String type) {
    System.out.println("Начинаю рисовать базу");
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
            ));  }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    System.out.println("Заканчиваю");
    return documents; // Возвращаем список документов
}
}
