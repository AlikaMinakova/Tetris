package ru.vsu.cs.course1.game;

/**
 * Класс для хранения параметров игры
 */
public class GameParams {
    private int rowCount;
    private int colCount;

    public GameParams(int rowCount, int colCount, int colorCount) {
        this.rowCount = rowCount;
        this.colCount = colCount;
    }

    public GameParams() {
        this(7, 7, 7);
    }

    /**
     * @return the colCount
     */
    public int getColCount() {
        return colCount;
    }

    /**
     * @param colCount the colCount to set
     */
    public void setColCount(int colCount) {
        this.colCount = colCount;
    }

    /**
     * @return the rowCount
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * @param rowCount the rowCount to set
     */
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }
}
