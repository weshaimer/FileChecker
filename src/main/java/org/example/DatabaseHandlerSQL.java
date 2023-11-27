package org.example;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


/**
 * Класс DatabaseHandlerSQL управляет взаимодействием с базой данных SQLite,
 * предоставляя методы для вставки, обновления, удаления и извлечения информации о файлах.
 */
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

    /**
     * Инициализирует соединение с базой данных и создает таблицу files_info, если она еще не существует.
     * @throws SQLException в случае ошибки SQL.
     * @throws ClassNotFoundException если класс драйвера JDBC не найден.
     */
    public void initDB() throws SQLException, ClassNotFoundException {
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

    /**
     * Очищает все данные из таблицы files_info.
     */
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

    /**
     * Вставляет информацию о файле в таблицу files_info.
     * @param filePath Путь к файлу.
     * @param fileType Тип файла.
     * @param fileHash Хеш файла.
     */
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
    //FIXME Link FileInfo
    /**
     * Вставляет список информации о файлах в базу данных. Этот метод использует пакетную вставку
     * для улучшения производительности при добавлении большого количества записей.
     *
     * @param files Список объектов {@link FileInfo}, содержащих информацию о файлах для вставки.
     *              Каждый объект FileInfo должен содержать абсолютный путь к файлу, его тип и хеш.
     */
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
    /**
     * Рекурсивно сканирует файловую систему, начиная с указанного пути, и собирает информацию о файлах.
     * Пропускает скрытые, системные файлы и файлы с определенными расширениями.
     *
     * @param path Путь в файловой системе, с которого начинается сканирование.
     * @return Список объектов {@link FileInfo}, представляющих файлы в сканируемом пути.
     *         Каждый объект {@link FileInfo} содержит абсолютный путь к файлу и другие сведения.
     */
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
    /**
     * Выполняет проверку на наличие дубликатов файлов заданного типа в базе данных.
     * Метод может провести полную или частичную проверку в зависимости от указанного флага.
     *
     * @param type Тип файла для поиска дубликатов (например, "Image", "Text").
     * @param isPartlyCheck Флаг, указывающий на необходимость частичной проверки.
     *                      Если true, выполняется частичная проверка; если false - полная проверка.
     * @return Map, где ключом является хеш файла, а значением - набор путей файлов, имеющих этот хеш.
     *         Это позволяет идентифицировать дубликаты файлов на основе их хешей.
     */
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
    /**
     * Осуществляет полную проверку на дубликаты файлов определенного типа в базе данных.
     * Метод выполняет запрос к базе данных для поиска файлов заданного типа с одинаковым хешем,
     * указывая на возможные дубликаты.
     *
     * @param type Тип файла для поиска дубликатов (например, "Image", "Text").
     * @return Map, где ключом является хеш файла, а значением - набор путей файлов, имеющих этот хеш.
     *         Это позволяет идентифицировать дубликаты файлов на основе их хешей.
     */
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
    /**
     * Извлекает и возвращает список объектов {@link FileInfo}, соответствующих указанным критериям.
     * Выполняет SQL-запрос к базе данных для извлечения информации о файлах, соответствующих заданным
     * типу и/или пути файла.
     *
     * @param type Тип файла для фильтрации результатов запроса. Если строка пуста, фильтрация по ТИПУ не происходит.
     * @param filePath Абсолютный путь файла для фильтрации результатов. Если строка пуста или null, фильтрация по пути не происходит.
     * @return Список объектов {@link FileInfo}, содержащих данные о файлах из базы данных.
     *         Каждый объект {@link FileInfo} включает абсолютный путь, тип файла и его хеш.
     */
    public List<FileInfo> fetchDocuments(String type, String filePath) {
        System.out.println("Начинаю рисовать базу");
        String fetchDataQuery = "SELECT * FROM files_info";
        List<FileInfo> documents = new ArrayList<>();

        List<String> conditions = new ArrayList<>();
        if (!type.isEmpty()) {
            conditions.add("file_type = '" + type + "'");
        }
        if (filePath != null && !filePath.isEmpty()) {
            conditions.add("file_path = '" + filePath + "'");
        }

        if (!conditions.isEmpty()) {
            fetchDataQuery += " WHERE " + String.join(" AND ", conditions);
        }

        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement(fetchDataQuery)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                documents.add(new FileInfo(
                        rs.getString("file_path"),
                        rs.getString("file_type"),
                        rs.getString("file_hash")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Заканчиваю");
        return documents; // Возвращаем список документов
    }
    /**
     * Сохраняет указанный путь сканирования в файл.
     * Пишет путь к последней сканированной директории в файл last_scan_path.txt для последующего использования.
     *
     * @param path Строка пути, которую нужно сохранить в файл.
     */
    public void saveScanPath(String path) {
        try (FileWriter writer = new FileWriter("last_scan_path.txt")) {
            writer.write(path);
        } catch (IOException e) {
            System.err.println("Ошибка при записи пути сканирования: " + e.getMessage());
        }
    }
    /**
     * Читает и возвращает последний сохраненный путь сканирования из файла.
     * Пытается открыть и прочитать файл last_scan_path.txt, который содержит путь к последней сканированной директории.
     *
     * @return Строка, содержащая последний путь сканирования, или null, если произошла ошибка при чтении файла.
     */
    public String readScanPath() {
        try (BufferedReader reader = new BufferedReader(new FileReader("last_scan_path.txt"))) {
            return reader.readLine();
        } catch (IOException e) {
            System.err.println("Ошибка при чтении пути сканирования: " + e.getMessage());
            return null;
        }
    }
    /**
     * Обновляет информацию о файле в базе данных. Метод изменяет тип и хеш файла в таблице
     * files_info, основываясь на предоставленном объекте FileInfo.
     *
     * @param fileInfo Объект FileInfo, содержащий обновленные данные о файле. Должен включать
     *                 абсолютный путь к файлу, его новый тип и хеш.
     */

    public void updateFileInfo(FileInfo fileInfo) {
        String updateQuery = "UPDATE files_info SET file_type = ?, file_hash = ? WHERE file_path = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setString(1, fileInfo.getType());
            pstmt.setString(2, fileInfo.getHash());
            pstmt.setString(3, fileInfo.getAbsolutePath());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Удаляет информацию о файле из базы данных по заданному пути файла.
     * Использует SQL запрос для удаления записи в таблице files_info, где путь файла соответствует
     * указанному аргументу.
     *
     * @param filePath Абсолютный путь к файлу, информация о котором должна быть удалена из базы данных.
     */
    public void deleteFileInfo(String filePath) {
        String deleteQuery = "DELETE FROM files_info WHERE file_path = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {

            pstmt.setString(1, filePath);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Обновляет базу данных на основе текущего состояния файловой системы, начиная с указанного пути.
     * Выполняет операции обновления, вставки и удаления, чтобы синхронизировать базу данных с
     * текущим состоянием файлов в директории.
     *
     * @param currentPath Путь к директории, которая будет использоваться для сканирования и обновления данных.
     *                    Если путь отличается от последнего сохраненного пути сканирования, метод очищает
     *                    базу данных и завершает выполнение.
     */
    public void updateDatabase(String currentPath) {
        String lastScannedPath = readScanPath();
        if (!currentPath.equals(lastScannedPath)) {
            // Путь изменился, можно очистить базу данных или прервать операцию
            clearDatabase();
            return;
        }

        List<FileInfo> currentFiles = checkFileSystem(currentPath);
        List<FileInfo> existingFiles = fetchDocuments("", null);

        Set<String> existingPaths = new HashSet<>();
        for (FileInfo existingFile : existingFiles) {
            existingPaths.add(existingFile.getAbsolutePath());
        }

        // Update и Insert
        for (FileInfo currentFile : currentFiles) {
            if (existingPaths.contains(currentFile.getAbsolutePath())) {
                // Update, если хеш изменился
                List<FileInfo> dbFileList = fetchDocuments("", currentFile.getAbsolutePath());
                if (!dbFileList.isEmpty()) {
                    FileInfo dbFile = dbFileList.get(0);
                    if (!dbFile.getHash().equals(currentFile.getHash())) {
                        updateFileInfo(currentFile);
                    }
                }
            } else {
                // Insert, если файл новый
                insertData(currentFile.getAbsolutePath(), currentFile.getType(), currentFile.getHash());
            }
        }

        // Delete устаревших файлов
        for (FileInfo existingFile : existingFiles) {
            if (currentFiles.stream().noneMatch(f -> f.getAbsolutePath().equals(existingFile.getAbsolutePath()))) {
                deleteFileInfo(existingFile.getAbsolutePath());
            }
        }
    }
}
