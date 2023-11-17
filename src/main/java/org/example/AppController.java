package org.example;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AppController {

    @FXML
    private TabPane tabPane;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label statusLabel;

    @FXML
    private Button settingsButton;

    @FXML
    private TextField inputField;

    private boolean isTaskRunning = false;

    @FXML
    private void initialize() {
        // Отключение инициализации TreeView
        addSettingsButton();
    }

    @FXML
    private void findDuplicates() {
        if (isTaskRunning) {
            return;
        }

        progressIndicator.setVisible(true);

        DuplicatesSearchService service = new DuplicatesSearchService();

        service.setOnSucceeded(event -> {
            progressIndicator.setVisible(false);
            statusLabel.setText("Сканирование завершено. Найдены дубликаты.");
            DatabaseHandlerSQL db = service.getValue();

            displayResults(db);

            isTaskRunning = false;
        });

        service.setOnRunning(event -> {
            statusLabel.setText("Идёт сканирование...");
        });

        isTaskRunning = true;
        service.start();
    }

    @FXML
    private void openSettings() {
        SettingsDialog settingsDialog = new SettingsDialog();
        settingsDialog.showSettingsDialog();
    }
    public String getTextValue(){
        return inputField.getText();
    }
    private void displayResults(DatabaseHandlerSQL db) {
        tabPane.getTabs().clear();

        if (SettingsDialog.isTextSelected()) {
            createTabWithCheckBoxes(tabPane, "Текст", db.dublicateCheck("Text",Boolean.FALSE));
        }

        if (SettingsDialog.isPhotoSelected()) {
            createTabWithCheckBoxes(tabPane, "Фото", db.dublicateCheck("Image",Boolean.FALSE));
        }

        if (SettingsDialog.isOtherSelected()) {
            createTabWithCheckBoxes(tabPane, "Другое", db.dublicateCheck("Other",Boolean.FALSE));
        }
    }

    private void createTabWithCheckBoxes(TabPane tabPane, String tabName, Map<String, Set<String>> duplicates) {
        Tab tab = new Tab(tabName);
        VBox content = new VBox();
        addCheckBoxesAndText(content, duplicates);
        tab.setContent(content);
        tabPane.getTabs().add(tab);
    }

    private void addCheckBoxesAndText(VBox content, Map<String, Set<String>> hashToFiles) {
//        CheckBoxTreeItem<String> hashItem = new CheckBoxTreeItem<>("Хэш: 123");
//        CheckBoxTreeItem<String> fileItem1 = new CheckBoxTreeItem<>("Файл:аБоба.хех");
//        CheckBoxTreeItem<String> fileItem2 = new CheckBoxTreeItem<>("Файл:аБоба.хах");
        TreeView<String> treeView = new TreeView<>();
        treeView.setRoot(new CheckBoxTreeItem<>());
        for (Map.Entry<String, Set<String>> entry : hashToFiles.entrySet()) {
            CheckBoxTreeItem<String> hashItem = new CheckBoxTreeItem<>(("Хэш " + entry.getKey() + " встречается " + entry.getValue().size() + " раз"));
            for (String filePath : entry.getValue()) {
                CheckBoxTreeItem<String> fileItem = new CheckBoxTreeItem<>("Файл: "+ filePath);
                hashItem.getChildren().add(fileItem);
            }
            treeView.getRoot().getChildren().add(hashItem);
            }
        treeView.setCellFactory(CheckBoxTreeCell.forTreeView());
        treeView.setShowRoot(false);
        content.getChildren().add(treeView);
    }

    private class DuplicatesSearchService extends Service<DatabaseHandlerSQL> {
        static int i = 0;
        @Override
        protected Task<DatabaseHandlerSQL> createTask() {
            return new Task<DatabaseHandlerSQL>() {
                @Override
                protected DatabaseHandlerSQL call() throws Exception {
                    // Simulate a delay to show progress indicator
//                    Thread.sleep(3000);
                    long startTime = System.currentTimeMillis();
                    String rootPath = getTextValue();
                    DatabaseHandlerSQL dbHandler = new DatabaseHandlerSQL();
                    dbHandler.clearDatabase();
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
                                if (i % 10000 == 0)
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
////            System.out.println(file.toString());
//                    dbHandler.dublicateCheck("Text", Boolean.FALSE);
//                    dbHandler.dublicateCheck("Image", Boolean.FALSE);
//                    dbHandler.dublicateCheck("Other", Boolean.FALSE);

                    // In a real application, perform the actual search here
                    return dbHandler;
                }
            };
        }
    }
    private void addSettingsButton() {
        settingsButton.setOnAction(event -> openSettings());
    }
}
