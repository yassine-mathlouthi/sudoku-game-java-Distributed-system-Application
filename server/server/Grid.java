package server;

import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

public class Grid {
    private String[][] cells;
    private final int SIZE = 9; // 9x9 grid

    public Grid() {
        cells = new String[SIZE][SIZE];
        initializeGrid();
    }

    private void initializeGrid() {
        Random rand = new Random();
        // Step 1: Generate a full valid Sudoku grid
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                cells[i][j] = " ";
            }
        }
        fillGrid(0, 0);

        // Step 2: Count total cells and decide how many to keep (40 to 50)
        int totalCells = SIZE * SIZE; // 81 cells in a 9x9 grid
        int cellsToKeep = rand.nextInt(11) + 40; // Randomly keep 40 to 50 cells
        int cellsToRemove = totalCells - cellsToKeep;

        // Step 3: Create a list of all cell positions
        ArrayList<int[]> positions = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                positions.add(new int[]{i, j});
            }
        }
        // Shuffle positions to remove cells randomly
        Collections.shuffle(positions, rand);

        // Step 4: Remove cells while ensuring solvability
        int removed = 0;
        for (int[] pos : positions) {
            if (removed >= cellsToRemove) {
                break;
            }

            int row = pos[0];
            int col = pos[1];
            String value = cells[row][col];

            // Temporarily remove the cell
            cells[row][col] = " ";
            // Check if the grid is still solvable with this cell removed
            if (!isSolvable()) {
                // If not solvable, restore the cell
                cells[row][col] = value;
            } else {
                removed++;
            }
        }

        // Step 5: If we couldn't remove enough cells, try to remove more
        while (removed < cellsToRemove) {
            for (int[] pos : positions) {
                int row = pos[0];
                int col = pos[1];
                if (!cells[row][col].equals(" ") && removed < cellsToRemove) {
                    cells[row][col] = " ";
                    removed++;
                }
            }
        }
    }

    // Helper method to fill a full valid Sudoku grid using backtracking
    private boolean fillGrid(int row, int col) {
        if (row == SIZE) {
            return true; // Grid is filled
        }

        int nextRow = col == SIZE - 1 ? row + 1 : row;
        int nextCol = col == SIZE - 1 ? 0 : col + 1;

        if (!cells[row][col].equals(" ")) {
            return fillGrid(nextRow, nextCol);
        }

        int[] numbers = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            numbers[i] = i + 1;
        }
        // Shuffle numbers to randomize placement
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
                if (fillGrid(nextRow, nextCol)) {
                    return true;
                }
                cells[row][col] = " "; // Backtrack
            }
        }
        return false;
    }

    // Helper method to check if the grid is solvable
    private boolean isSolvable() {
        // Create a copy of the grid
        String[][] copy = new String[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                copy[i][j] = cells[i][j];
            }
        }

        // Try to solve the grid
        return solveGrid(0, 0, copy);
    }

    // Helper method to solve the grid using backtracking
    private boolean solveGrid(int row, int col, String[][] grid) {
        if (row == SIZE) {
            return true;
        }

        int nextRow = col == SIZE - 1 ? row + 1 : row;
        int nextCol = col == SIZE - 1 ? 0 : col + 1;

        if (!grid[row][col].equals(" ")) {
            return solveGrid(nextRow, nextCol, grid);
        }

        for (int num = 1; num <= 9; num++) {
            if (isValidMove(row, col, String.valueOf(num), grid)) {
                grid[row][col] = String.valueOf(num);
                if (solveGrid(nextRow, nextCol, grid)) {
                    return true;
                }
                grid[row][col] = " "; // Backtrack
            }
        }
        return false;
    }

    // Helper method to check if a move is valid on a given grid
    private boolean isValidMove(int row, int col, String value, String[][] grid) {
        try {
            int val = Integer.parseInt(value);
            if (!grid[row][col].equals(" ") || val < 1 || val > 9) {
                return false;
            }

            // Check row for duplicates
            for (int j = 0; j < SIZE; j++) {
                if (j != col && grid[row][j].equals(value)) {
                    return false;
                }
            }

            // Check column for duplicates
            for (int i = 0; i < SIZE; i++) {
                if (i != row && grid[i][col].equals(value)) {
                    return false;
                }
            }

            // Check 3x3 subgrid for duplicates
            int subgridRowStart = (row / 3) * 3;
            int subgridColStart = (col / 3) * 3;
            for (int i = subgridRowStart; i < subgridRowStart + 3; i++) {
                for (int j = subgridColStart; j < subgridColStart + 3; j++) {
                    if (i != row && j != col && grid[i][j].equals(value)) {
                        return false;
                    }
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
                if (cells[i][j].equals(" ")) {
                    return false;
                }
            }
        }
        // Verify the solution is valid
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                String value = cells[i][j];
                cells[i][j] = " ";
                boolean valid = isValidMove(i, j, value);
                cells[i][j] = value;
                if (!valid) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        for (int j = 0; j < SIZE; j++) {
            sb.append(String.format("%2d ", j));
        }
        sb.append("\n");

        sb.append("  +");
        for (int j = 0; j < SIZE; j++) {
            sb.append(j % 3 == 2 ? "====+" : "---+");
        }
        sb.append("\n");

        for (int i = 0; i < SIZE; i++) {
            sb.append(String.format("%2d|", i));
            for (int j = 0; j < SIZE; j++) {
                String cell = cells[i][j].equals(" ") ? "." : cells[i][j];
                sb.append(String.format(" %1s %s", cell, j % 3 == 2 ? "|" : "|"));
            }
            sb.append("\n");

            sb.append("  +");
            for (int j = 0; j < SIZE; j++) {
                sb.append(i % 3 == 2 && j % 3 == 2 ? "====+" : i % 3 == 2 ? "===+" : j % 3 == 2 ? "---+": "---+");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}