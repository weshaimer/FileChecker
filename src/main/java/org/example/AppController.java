package org.example;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Контроллер приложения для поиска дубликатов.
 */
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

    @FXML
    private Label selectedFoldersLabel;

    private boolean isTaskRunning = false;

    private List<String> selectedFolders = new ArrayList<>();

    private DatabaseHandlerSQL dbHandler;

    /**
     * Инициализация контроллера.
     */
    @FXML
    private void initialize() {
        addSettingsButton();
    }

    /**
     * Запуск процесса поиска дубликатов.
     */
    @FXML
    private void findDuplicates() {
        if (isTaskRunning) {
            return;
        }

        progressIndicator.setVisible(true);

        List<DuplicatesSearchService> services = new ArrayList<>();

            DuplicatesSearchService service = new DuplicatesSearchService(selectedFolders);
            services.add(service);

            service.setOnSucceeded(new EventHandler<>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    services.remove(service);

                    if (services.isEmpty()) {
                        progressIndicator.setVisible(false);
                        statusLabel.setText("Сканирование завершено.");
                        dbHandler = service.getValue();
                        displayResults(dbHandler);
                        isTaskRunning = false;
                    }
                }
            });

            service.setOnRunning(new EventHandler<>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    statusLabel.setText("Идёт сканирование...");
                }
            });

            isTaskRunning = true;
            service.start();
    }

    /**
     * Открытие диалога настроек.
     */
    @FXML
    private void openSettings() {
        SettingsDialog settingsDialog = new SettingsDialog();
        settingsDialog.showSettingsDialog();
    }

    /**
     * Открытие диалога выбора папок и файлов.
     */
    @FXML
    private void selectFolders() {
        // Создаем новое дерево для отображения выбранных папок и файлов
        ListView<String> selectedListView = new ListView<>();
        for(String path : selectedFolders){
            selectedListView.getItems().add(path);
        }
        // Добавляем кнопку "+" для выбора папок
        Image imgFolder = new Image("file:src/main/resources/org/example/add-folder.png");
        ImageView viewFolder = new ImageView(imgFolder);
        viewFolder.setFitHeight(20);
        viewFolder.setPreserveRatio(true);
        Button folderButton = new Button();
        folderButton.setGraphic(viewFolder);
        folderButton.setOnAction(event -> {

            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Выберите папку для сканирования");
            File selectedFolder = directoryChooser.showDialog(selectedListView.getScene().getWindow());
            if (selectedFolder != null) {
                addFileToTreeView(selectedFolder, selectedListView);
            }
        });

        // Добавляем кнопку "+" для выбора файлов
        Image imgFile = new Image("file:src/main/resources/org/example/new-document.png");
        ImageView viewFile = new ImageView(imgFile);
        viewFile.setFitHeight(20);
        viewFile.setPreserveRatio(true);
        Button fileButton = new Button();
        fileButton.setGraphic(viewFile);
        fileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите файл для сканирования");
            File selectedFile = fileChooser.showOpenDialog(selectedListView.getScene().getWindow());
            if (selectedFile != null) {
                    addFileToTreeView(selectedFile, selectedListView);
                }
        });

        // Добавляем кнопку "-", при нажатии на которую будет удален выбранный элемент
        Image imgBin = new Image("file:src/main/resources/org/example/bin.png");
        ImageView viewBin = new ImageView(imgBin);
        viewBin.setFitHeight(20);
        viewBin.setPreserveRatio(true);
        Button deleteButton = new Button();
        deleteButton.setGraphic(viewBin);
        deleteButton.setOnAction(event -> {
            String selectedItem = selectedListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                selectedListView.getItems().remove(selectedItem);
            }
        });

        HBox hbox = new HBox();
        hbox.setSpacing(2);
        hbox.getChildren().addAll(folderButton, fileButton, deleteButton);
        // Добавляем кнопку "Готово", при нажатии на которую закрывается окно и обновляется информация
        Button doneButton = new Button("Готово");
        doneButton.setOnAction(event -> {
            updateSelectedFolders(selectedListView);
            // Дополнительные действия при закрытии окна



            ((Stage) selectedListView.getScene().getWindow()).close();
        });

        // Добавляем элементы управления в окно
        VBox selectionBox = new VBox(hbox, selectedListView, doneButton);
        Stage stage = new Stage();
        stage.setScene(new Scene(selectionBox));
        stage.setTitle("Выбор папок и файлов");
        stage.setWidth(400);
        stage.show();
    }

    /**
     * Рекурсивно добавляет папку и её содержимое в дерево.
     *
     * @param folder Папка для добавления в дерево.
     * @param parent Родительский узел дерева.
     */
//    private void addFolderToTreeView(File folder, CheckBoxTreeItem<String> parent) {
//        CheckBoxTreeItem<String> folderItem = new CheckBoxTreeItem<>(folder.getAbsolutePath());
//        parent.getChildren().add(folderItem);
//
//        File[] files = folder.listFiles();
//        if (files != null) {
//            for (File file : files) {
//                if (file.isDirectory()) {
//                    addFolderToTreeView(file, folderItem);
//                } else {
//                    addFileToTreeView(file, folderItem);
//                }
//            }
//        }
//    }
    /**
     * Добавляет файл в дерево.
     *
     * @param file   Файл для добавления.
     */
    private void addFileToTreeView(File file, ListView root) {
        String fileItem = file.getAbsolutePath();
        root.getItems().add(fileItem);
    }

    /**
     * Рекурсивно обновляет список выбранных папок и файлов на основе дерева.
     *
     * @param root Узел дерева для обработки.
     */
    private void updateSelectedFolders(ListView root) {
        selectedFolders=new ArrayList<>();
        for (Object item : root.getItems()) {
            selectedFolders.add(item.toString());
        }
        System.out.println(selectedFolders);
        selectedFoldersLabel.setText("Выбранные папки и файлы: " + selectedFolders.toString());
    }

    /**
     * Отображение результатов сканирования во вкладках.
     *
     * @param db Объект для работы с базой данных.
     */
    private void displayResults(DatabaseHandlerSQL db) {
        tabPane.getTabs().clear();

        if (SettingsDialog.isTextSelected()) {
            createTabWithCheckBoxes(tabPane, "Текст", db.dublicateCheck("Text", Boolean.FALSE));
        }

        if (SettingsDialog.isPhotoSelected()) {
            createTabWithCheckBoxes(tabPane, "Фото", db.dublicateCheck("Image", Boolean.FALSE));
        }

        if (SettingsDialog.isOtherSelected()) {
            createTabWithCheckBoxes(tabPane, "Другое", db.dublicateCheck("Other", Boolean.FALSE));
        }
    }

    /**
     * Создание вкладки с чекбоксами для файлов.
     *
     * @param tabPane   Контейнер вкладок.
     * @param tabName   Название вкладки.
     * @param duplicates Маппинг хэшей к спискам файлов.
     */
    private void createTabWithCheckBoxes(TabPane tabPane, String tabName, Map<String, Set<String>> duplicates) {
        Tab tab = new Tab(tabName);
        VBox content = new VBox();
        addCheckBoxesAndText(content, duplicates);
        tab.setContent(content);
        tabPane.getTabs().add(tab);
    }

    /**
     * Добавление чекбоксов и текста на вкладку.
     *
     * @param content    Контейнер вкладки.
     * @param hashToFiles Маппинг хэшей к спискам файлов.
     */
    private void addCheckBoxesAndText(VBox content, Map<String, Set<String>> hashToFiles) {
        TreeView<String> treeView = new TreeView<>();
        treeView.setRoot(new CheckBoxTreeItem<>());
        for (Map.Entry<String, Set<String>> entry : hashToFiles.entrySet()) {
            CheckBoxTreeItem<String> hashItem = new CheckBoxTreeItem<>("Хэш " + entry.getKey() + " встречается " + entry.getValue().size() + " раз");
            for (String filePath : entry.getValue()) {
                CheckBoxTreeItem<String> fileItem = new CheckBoxTreeItem<>("Файл: " + filePath);
                hashItem.getChildren().add(fileItem);
            }
            treeView.getRoot().getChildren().add(hashItem);
        }
        treeView.setCellFactory(CheckBoxTreeCell.forTreeView());
        treeView.setShowRoot(false);
        content.getChildren().add(treeView);
    }

    /**
     * Сервис для выполнения поиска дубликатов в фоновом режиме.
     */
    private class DuplicatesSearchService extends Service<DatabaseHandlerSQL> {
        private List<String> folders;

        /**
         * Конструктор с параметрами.
         *
         * @param folders Список выбранных пользователем папок для сканирования.
         */
        public DuplicatesSearchService(List<String> folders) {
            this.folders = folders;
        }

        private int i = 0;

        @Override
        protected Task<DatabaseHandlerSQL> createTask() {
            return new Task<DatabaseHandlerSQL>() {
                @Override
                protected DatabaseHandlerSQL call() throws Exception {
                    long startTime = System.currentTimeMillis();
                    DatabaseHandlerSQL dbHandler = new DatabaseHandlerSQL();
                    dbHandler.clearDatabase();
                    long checkTime = System.currentTimeMillis();
                    int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
                    ExecutorService executorService;
                    if (THREAD_POOL_SIZE > 4) executorService = Executors.newScheduledThreadPool(4);
                    else executorService = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

                    long hashTime = 0;
                    List<FileInfo> fetchDocuments= new ArrayList<>();
                    for(String folder : folders){
                       fetchDocuments.addAll(DatabaseHandlerSQL.checkFileSystem(folder));}
//                    fetchDocuments = fetchDocuments.stream().distinct().collect(Collectors.toList());
                    fetchDocuments= fetchDocuments.stream()
                            .collect(Collectors.collectingAndThen(
                                    Collectors.toMap(
                                            FileInfo::getAbsolutePath,
                                            Function.identity(),
                                            (existing, replacement) -> existing),
                                    map -> new ArrayList<>(map.values())
                            ));
                    System.out.println(fetchDocuments);
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
                    executorService.shutdown();
                    try {
                        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                        dbHandler = new DatabaseHandlerSQL();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    hashTime = System.currentTimeMillis();
                    dbHandler.insertData(fetchDocuments);

                    long endTime = System.currentTimeMillis();
                    long timeCheck = checkTime - startTime;
                    long timeHash = hashTime - checkTime;
                    long timeElapsed = endTime - hashTime;
                    System.out.println("Folders scan: " + timeCheck / 1000 + " Hashing: " + timeHash / 1000 + " SQL: " + timeElapsed / 1000);
                    return dbHandler;
                }

            };
        }
    }

    /**
     * Добавление обработчика события для кнопки настроек.
     */
    private void addSettingsButton() {
        settingsButton.setOnAction(event -> openSettings());
    }
}
