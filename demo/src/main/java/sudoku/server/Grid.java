package sudoku.server;

import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

public class Grid {
    private String[][] cells;
    private final int SIZE = 9; // 9x9 grid

    public Grid() {
        cells = new String[SIZE][SIZE];
        System.out.println("Initializing new Sudoku grid...");
        initializeGrid();
        System.out.println("Grid initialized:\n" + toString());
    }

    private void initializeGrid() {
        Random rand = new Random();
        // Initialize the grid with empty spaces
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                cells[i][j] = " ";
            }
        }
        // Fill the grid with a valid solution
        if (!fillGrid(0, 0)) {
            System.err.println("Error: Could not fill the grid with a valid solution.");
        }

        // Generate a list of all positions and shuffle
        ArrayList<int[]> positions = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                positions.add(new int[]{i, j});
            }
        }
        Collections.shuffle(positions, rand);

        // Remove cells one by one while checking solvability
        int cellsToKeep = rand.nextInt(11) + 40; // Between 40-50 clues kept
        int targetToRemove = (SIZE * SIZE) - cellsToKeep;
        int removed = 0;

        for (int[] pos : positions) {
            if (removed >= targetToRemove) break;
            int row = pos[0];
            int col = pos[1];
            String backup = cells[row][col];
            cells[row][col] = " ";

            if (!isSolvable()) {
                cells[row][col] = backup; // Revert if unsolvable
            } else {
                removed++;
            }
        }
    }

    private boolean fillGrid(int row, int col) {
        if (row == SIZE) return true;

        int nextRow = col == SIZE - 1 ? row + 1 : row;
        int nextCol = col == SIZE - 1 ? 0 : col + 1;

        if (!cells[row][col].equals(" ")) return fillGrid(nextRow, nextCol);

        int[] numbers = new int[SIZE];
        for (int i = 0; i < SIZE; i++) numbers[i] = i + 1;
        Random rand = new Random();
        for (int i = 0; i < SIZE; i++) {
            int j = rand.nextInt(SIZE);
            int temp = numbers[i];
            numbers[i] = numbers[j];
            numbers[j] = temp;
        }

        for (int num : numbers) {
            if (isValidMove(row, col, String.valueOf(num))) {
                cells[row][col] = String.valueOf(num);
                if (fillGrid(nextRow, nextCol)) return true;
                cells[row][col] = " ";
            }
        }
        return false;
    }

    private boolean isSolvable() {
        String[][] copy = new String[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                copy[i][j] = cells[i][j];
            }
        }
        return solveGrid(0, 0, copy);
    }

    private boolean solveGrid(int row, int col, String[][] grid) {
        if (row == SIZE) return true;

        int nextRow = col == SIZE - 1 ? row + 1 : row;
        int nextCol = col == SIZE - 1 ? 0 : col + 1;

        if (!grid[row][col].equals(" ")) return solveGrid(nextRow, nextCol, grid);

        for (int num = 1; num <= 9; num++) {
            if (isValidMove(row, col, String.valueOf(num), grid)) {
                grid[row][col] = String.valueOf(num);
                if (solveGrid(nextRow, nextCol, grid)) return true;
                grid[row][col] = " ";
            }
        }
        return false;
    }

    private boolean isValidMove(int row, int col, String value, String[][] grid) {
        try {
            int val = Integer.parseInt(value);
            if (!grid[row][col].equals(" ") || val < 1 || val > 9) return false;

            for (int j = 0; j < SIZE; j++) {
                if (j != col && grid[row][j].equals(value)) return false;
            }
            for (int i = 0; i < SIZE; i++) {
                if (i != row && grid[i][col].equals(value)) return false;
            }
            int subgridRowStart = (row / 3) * 3;
            int subgridColStart = (col / 3) * 3;
            for (int i = subgridRowStart; i < subgridRowStart + 3; i++) {
                for (int j = subgridColStart; j < subgridColStart + 3; j++) {
                    if ((i != row || j != col) && grid[i][j].equals(value)) return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidMove(int row, int col, String value) {
        return isValidMove(row, col, value, cells);
    }

    public void setCell(int row, int col, String value) {
        cells[row][col] = value;
    }

    public boolean isComplete() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (cells[i][j].equals(" ")) return false;
            }
        }
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                String value = cells[i][j];
                cells[i][j] = " ";
                boolean valid = isValidMove(i, j, value);
                cells[i][j] = value;
                if (!valid) return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        for (int j = 0; j < SIZE; j++) sb.append(String.format("%2d ", j));
        sb.append("\n  +");
        for (int j = 0; j < SIZE; j++) sb.append("---+");
        sb.append("\n");

        for (int i = 0; i < SIZE; i++) {
            sb.append(String.format("%2d|", i));
            for (int j = 0; j < SIZE; j++) {
                String cell = cells[i][j].equals(" ") ? "." : cells[i][j];
                sb.append(String.format(" %s |", cell));
            }
            sb.append("\n  +");
            for (int j = 0; j < SIZE; j++) sb.append("---+");
            sb.append("\n");
        }
        return sb.toString();
    }
}
