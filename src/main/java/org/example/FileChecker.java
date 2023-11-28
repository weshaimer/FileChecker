package org.example;

import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Этот класс представляет собой инструмент FileChecker для сканирования файловой системы,
 * вычисления хэш-значений для файлов и выполнения операций с базой данных, связанных с информацией о файлах.
 */
public class FileChecker {

    /**
     * Глобальный счетчик обработанных файлов.
     */
    public static Integer i = 0;

    /**
     * Главный метод для запуска процесса проверки файлов.
     *
     * @param args Аргументы командной строки (не используются).
     */
    public static void main(String[] args) {
        // Записываем время начала процесса.
        long startTime = System.currentTimeMillis();

        // Задаем корневой путь для файловой системы.
        String rootPath = "";

        // Инициализируем обработчик базы данных для SQL-операций.
        DatabaseHandlerSQL dbHandler = new DatabaseHandlerSQL();
        dbHandler.clearDatabase();

        // Записываем время перед проверкой файловой системы.
        long checkTime = System.currentTimeMillis();

        // Определяем количество доступных процессоров для создания пула потоков.
        int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors(); // Количество потоков в пуле

        // Инициализируем ExecutorService для управления потоками в пуле потоков.
        ExecutorService executorService;
        if (THREAD_POOL_SIZE > 4) executorService = Executors.newScheduledThreadPool(4);
        else executorService = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

        // Получаем список объектов FileInfo из базы данных на основе проверки файловой системы.
        List<FileInfo> fetchDocuments = DatabaseHandlerSQL.checkFileSystem(rootPath);

        // Итерируемся по каждому объекту FileInfo и выполняем вычисление хэша в отдельном потоке.
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

        // Завершаем работу пула потоков и ожидаем завершения всех потоков.
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Записываем время после вычисления хэша.
        long hashTime = System.currentTimeMillis();

        // Вставляем информацию о файлах в базу данных.
        dbHandler.insertData(fetchDocuments);

        // Записываем время окончания процесса.
        long endTime = System.currentTimeMillis();

        // Вычисляем и выводим время, затраченное на каждую фазу процесса.
        long timeCheck = checkTime - startTime;
        long timeHash = hashTime - checkTime;
        long timeElapsed = endTime - hashTime;
        System.out.println("Folders scan: " + timeCheck / 1000 + " Hashing: " + timeHash / 1000 + " SQL: " + timeElapsed / 1000);
        // Раскомментируйте следующие строки, если хотите вывести объекты FileInfo из базы данных.
        // for(FileInfo file : dbHandler.fetchDocuments(""))
        // System.out.println(file.toString());

        // Выполняем проверку дубликатов для различных типов файлов в базе данных.
        dbHandler.dublicateCheck("Text", Boolean.FALSE);
        dbHandler.dublicateCheck("Image", Boolean.FALSE);
        dbHandler.dublicateCheck("Other", Boolean.FALSE);
    }
}

