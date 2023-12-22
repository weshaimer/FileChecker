// Путь к файлу: src/main/java/com/example/imageprocessing/DCT.java

package org.example;

import marvin.gui.MarvinAttributesPanel;
import marvin.gui.MarvinImagePanel;
import marvin.image.MarvinImage;
import marvin.image.MarvinImageMask;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinAttributes;

import java.util.List;

/**
 * Класс DCT реализует интерфейс MarvinImagePlugin и предоставляет функциональность
 * для применения дискретного косинусного преобразования (DCT) к изображению.
 */
public class DCT implements MarvinImagePlugin {
    /**
     * Применяет дискретное косинусное преобразование (DCT) к заданной области изображения.
     *
     * @param imageIn   Входное изображение.
     * @param imageOut  Выходное изображение, в которое записывается результат DCT.
     * @param x         Координата x верхнего левого угла области изображения.
     * @param y         Координата y верхнего левого угла области изображения.
     * @param width     Ширина области изображения.
     * @param height    Высота области изображения.
     */
    public void process(MarvinImage imageIn, MarvinImage imageOut, int x, int y, int width, int height) {
        int[][] pixels = new int[width][height];

        // Заполняем массив значениями пикселей изображения
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixels[i][j] = imageIn.getIntComponent0(x + i, y + j);
            }
        }

        // Выполняем дискретное косинусное преобразование
        double[][] dctResult = applyDCT(pixels);

        // Записываем результат обратно в изображение
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                imageOut.setIntColor(x + i, y + j, (int) dctResult[i][j], (int) dctResult[i][j], (int) dctResult[i][j]);
            }
        }
    }
    /**
     * Применяет дискретное косинусное преобразование (DCT) к двумерному массиву пикселей.
     *
     * @param pixels Двумерный массив пикселей.
     * @return Результат DCT в виде двумерного массива.
     */
    private double[][] applyDCT(int[][] pixels) {
        int width = pixels.length;
        int height = pixels[0].length;

        double[][] result = new double[width][height];

        for (int u = 0; u < width; u++) {
            for (int v = 0; v < height; v++) {
                double sum = 0.0;

                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        sum += Math.cos((2.0 * i + 1.0) / (2.0 * width) * u * Math.PI) *
                                Math.cos((2.0 * j + 1.0) / (2.0 * height) * v * Math.PI) *
                                (pixels[i][j] - 128); // Предварительно вычитаем 128 для центрирования
                    }
                }

                double cu = (u == 0) ? 1.0 / Math.sqrt(2) : 1.0;
                double cv = (v == 0) ? 1.0 / Math.sqrt(2) : 1.0;

                result[u][v] = 0.25 * cu * cv * sum;
            }
        }

        return result;
    }
    // Реализации методов из интерфейса MarvinImagePlugin, которые не используются в данном контексте
    @Override
    public MarvinAttributesPanel getAttributesPanel() {
        return null;
    }

    @Override
    public void process(MarvinImage marvinImage, MarvinImage marvinImage1, MarvinAttributes marvinAttributes, MarvinImageMask marvinImageMask, boolean b) {

    }

    @Override
    public void process(MarvinImage marvinImage, MarvinImage marvinImage1, MarvinImageMask marvinImageMask) {

    }

    @Override
    public void process(MarvinImage marvinImage, MarvinImage marvinImage1, MarvinAttributes marvinAttributes) {

    }

    @Override
    public void process(MarvinImage marvinImage, MarvinImage marvinImage1) {

    }

    @Override
    public void process(List<MarvinImage> list, MarvinImage marvinImage) {

    }

    @Override
    public void setImagePanel(MarvinImagePanel marvinImagePanel) {

    }

    @Override
    public MarvinImagePanel getImagePanel() {
        return null;
    }

    @Override
    public void load() {

    }

    @Override
    public void validate() {

    }

    @Override
    public void invalidate() {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public MarvinAttributes getAttributes() {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void setAttributes(Object... objects) {

    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }
}
