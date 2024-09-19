package ru.vsu.cs.course1.game;

import java.awt.*;
import java.util.*;
import java.util.List;


public class Game {

    private final Random rnd = new Random();

    public static final int FIGURE_SIZE = 4;

    public static int scores = 0;

    // максимальный результат
    public static int scoresMax = 0;

    //состояние игры
    public enum GameState {
        NOT_STARTED,
        PLAYING,
        LOSED;
    }

    // статус игры
    public static GameState state = GameState.NOT_STARTED;

    private static final String[] FIGURE_DESCRIPTIONS = {
            String.join("", Arrays.asList(
                    " *  |****| *  |****|",
                    " *  |    | *  |    |",
                    " *  |    | *  |    |",
                    " *  |    | *  |    |"
            )),

            String.join("", Arrays.asList(
                    " ** | ** | ** | ** |",
                    " ** | ** | ** | ** |",
                    "    |    |    |    |",
                    "    |    |    |    |"
            )),

            String.join("", Arrays.asList(
                    "**  |  * |**  |  * |",
                    " ** | ** | ** | ** |",
                    "    | *  |    | *  |",
                    "    |    |    |    |"
            )),

            String.join("", Arrays.asList(
                    "  **| *  |  **| *  |",
                    " ** | ** | ** | ** |",
                    "    |  * |    |  * |",
                    "    |    |    |    |"
            )),

            String.join("", Arrays.asList(
                    " *  | ***| ** |   *|",
                    " *  | *  |  * | ***|",
                    " ** |    |  * |    |",
                    "    |    |    |    |"
            )),

            String.join("", Arrays.asList(
                    "  * | *  | ** | ***|",
                    "  * | ***| *  |   *|",
                    " ** |    | *  |    |",
                    "    |    |    |    |"
            )),

            String.join("", Arrays.asList(
                    " ***|  * |  * | *  |",
                    "  * | ** | ***| ** |",
                    "    |  * |    | *  |",
                    "    |    |    |    |"
            )),
    };

    private static class FigureState {
        public final boolean[][] field;

        private FigureState(boolean[][] field) {
            this.field = field;
        }
    }

    private static final FigureState[][] figures;

    static {
        int n = FIGURE_DESCRIPTIONS.length;
        figures = new FigureState[n][FIGURE_SIZE];

        for (int f = 0; f < n; f++) {
            for (int s = 0; s < 4; s++) {
                figures[f][s] = new FigureState(new boolean[FIGURE_SIZE][FIGURE_SIZE]);
                for (int r = 0; r < FIGURE_SIZE; r++) {
                    for (int c = 0; c < FIGURE_SIZE; c++) {
                        figures[f][s].field[r][c] = FIGURE_DESCRIPTIONS[f].charAt((FIGURE_SIZE + 1) * FIGURE_SIZE * r + (FIGURE_SIZE + 1) * s + c) == '*';
                    }
                }
            }
        }
    }

    private class CurrFigure {
        public int figIndex;
        public int rotIndex;
        public int row;
        public int col;
        public int color;

        public CurrFigure(int figIndex, int rotIndex, int row, int col, int color) {
            this.figIndex = figIndex;
            this.rotIndex = rotIndex;
            this.row = row;
            this.col = col;
            this.color = color;
        }
    }

    private int[][] field;
    private CurrFigure curr;
    private CurrFigure next;
    private int colorCount;


    public Game(int colorCount) {
        this.colorCount = colorCount;
    }

    // !!!!!
    public int getCell(int row, int col) {
        if (state == GameState.NOT_STARTED) {
            return 0;
        }
        if (field[row][col] > 0) {
            return field[row][col];
        } else if (curr.row <= row && row < curr.row + FIGURE_SIZE && curr.col <= col && col < curr.col + FIGURE_SIZE) {
            boolean[][] fig = figures[curr.figIndex][curr.rotIndex].field;
            return fig[row - curr.row][col - curr.col] ? curr.color : 0;
        }
        return 0;
    }

    public int getCellТNewFigure(int row, int col) {
        if (state == GameState.NOT_STARTED) {
            return 0;
        }
        boolean[][] fig = figures[next.figIndex][next.rotIndex].field;
        //return fig[row][col] ? next.color : 0;
        if (next.figIndex == 0 && next.rotIndex % 2 == 0){
            return fig[row][col] ? next.color : 0;
        }
        else if (row == 0){
            return 0;
        }
        else {
            return fig[row - 1][col] ? next.color : 0;
        }
    }

    public void currFigureToField() {
        for (int r = 0; r < FIGURE_SIZE; r++) {
            for (int c = 0; c < FIGURE_SIZE; c++) {
                if (figures[curr.figIndex][curr.rotIndex].field[r][c]) {
                    field[curr.row + r][curr.col + c] = curr.color;
                }
            }
        }
    }

    public CurrFigure getNextFigure() {
        CurrFigure cf = new CurrFigure(rnd.nextInt(figures.length), rnd.nextInt(4), 0,
                getColCount() / 2 - 2, rnd.nextInt(colorCount) + 1);
        int height = 0;
        boolean[][] fig = figures[cf.figIndex][cf.rotIndex].field;
        for (int r = 0; r < FIGURE_SIZE; r++) {
            for (int c = 0; c < FIGURE_SIZE; c++) {
                if (fig[r][c]) {
                    height--;
                    break;
                }
            }
        }
        cf.row = height + 1;
        return cf;
    }

    // есть ли строки для удаления
    public void deleteRows() {
        int count = 0; // подсчитывает очки

        while (true) {
            int r = checkDelete();
            if (r != -1) {
                count++;
                for (int c = getColCount() - 1; c >= 0; c--) {
                    field[r][c] = 0;
                }
                sdvig(r);
            } else {
                scores += (count == 1) ? 100 : (count == 2) ? 300 : (count == 3) ? 700 : (count == 4) ? 1500 : 0;
                scoresMax = Math.max(scores, scoresMax);
                break;
            }
        }
    }

    // есть ли строки для удаления, если да, то вернуть какую
    public int checkDelete() {
        int count = 0;
        for (int r = getRowCount() - 1; r >= 0; r--) {
            for (int c = getColCount() - 1; c >= 0; c--) {
                if (field[r][c] == 0) {
                    break;
                } else {
                    count++;
                }
            }
            if (count == getColCount()) {
                return r;
            }
            count = 0;
        }
        return -1;
    }

    // сдвигает элементы !!!!!!
    public void sdvig(int row) {
        for (int r = row - 1; r >= 0; r--) {
            for (int c = getColCount() - 1; c >= 0; c--) {
                field[r + 1][c] = field[r][c];
                field[r][c] = 0;
            }
        }
    }


    public void newGame(int rowCount, int colCount) {
        scores = 0;
        field = new int[rowCount][colCount];
        curr = getNextFigure();
        next = getNextFigure();

    }


    private boolean isCrossWithField(CurrFigure curr) {
        int rowCount = getRowCount();
        int colCount = getColCount();
        boolean[][] fig = figures[curr.figIndex][curr.rotIndex].field;
        for (int r = 0; r < FIGURE_SIZE; r++) {
            for (int c = 0; c < FIGURE_SIZE; c++) {
                if (0 <= curr.row + r && curr.row + r < rowCount && 0 <= curr.col + c && curr.col + c < colCount) {
                    if (field[curr.row + r][curr.col + c] > 0 && fig[r][c]) {
                        return true;
                    }
                } else if (fig[r][c]) {
                    if (curr.row + r < 0) {
                        continue;
                    }
                    // если слева не вместился
                    if (curr.col + c < 0) {
                        curr.col = 0;
                        return isCrossWithField(curr);
                    }
                    // если справа не вместился
                    if (curr.col + c >= colCount) {
                        curr.col -= 4 - c;
                        return isCrossWithField(curr);
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean moveAndRotate(int dRow, int dCol, int dRotIndex) {
        if (state != GameState.PLAYING) {
            return false;
        }
        // чтоб не отскакивало при нажатии на стрелки
        if (curr.col == -1 && dCol == -1 || dCol == 1 && (curr.col >= 7 && curr.figIndex != 0 || curr.figIndex == 0 && curr.col == 8)) {
            return false;
        }


        CurrFigure cf = new CurrFigure(curr.figIndex, (curr.rotIndex + dRotIndex) % 4, curr.row + dRow, curr.col + dCol, curr.color);
        if (!isCrossWithField(cf)) {
            curr.col = cf.col;
            curr.row = cf.row;
            curr.rotIndex = cf.rotIndex;
            return true;
        }
        return false;
    }

    public boolean right() {
        return moveAndRotate(0, 1, 0);
    }

    public boolean left() {
        return moveAndRotate(0, -1, 0);
    }

    // спускает фигуру
    public boolean down() {
        if (state != GameState.PLAYING) {
            return false;
        }
        if (!moveAndRotate(1, 0, 0)) {
            if (curr.row >= 0) {
                currFigureToField();
                deleteRows();
                curr = next;
                if (isCrossWithField(curr)) {
                    state = GameState.LOSED;
                    return true;
                }
                next = getNextFigure();
            } else {
                state = GameState.LOSED;
                return true;
            }
        }
        return true;
    }

    public boolean rotate() {
        return moveAndRotate(0, 0, 1);
    }

    public int getRowCount() {
        return field == null ? 0 : field.length;
    }

    public int getColCount() {
        return field == null ? 0 : field[0].length;
    }
}
