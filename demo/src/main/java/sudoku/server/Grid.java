package sudoku.server;

import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

public class Grid {
    private String[][] cells; // Current game grid (with empty cells)
    private String[][] solution; // Full solution grid (for reference, not enforced)
    private boolean[][] preFilled; // Tracks pre-filled cells
    private final int SIZE = 9; // 9x9 grid

    public Grid() {
        cells = new String[SIZE][SIZE];
        solution = new String[SIZE][SIZE];
        preFilled = new boolean[SIZE][SIZE];
        System.out.println("Initializing new Sudoku grid...");
        initializeGrid();
        System.out.println("Grid initialized:\n" + toString());
        System.out.println("Solution:\n" + solutionToString());
        int preFilledCount = countPreFilledCells();
        System.out.println("Number of pre-filled cells: " + preFilledCount);
        if (preFilledCount < 20 || preFilledCount > 29) {
            System.err.println("Error: Expected 20-29 pre-filled cells, got " + preFilledCount);
        }
    }

    private int countPreFilledCells() {
        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (preFilled[i][j]) count++;
            }
        }
        return count;
    }

    private void initializeGrid() {
        Random rand = new Random();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                cells[i][j] = " ";
                solution[i][j] = " ";
                preFilled[i][j] = false;
            }
        }
        System.out.println("Grid after initialization (empty):\n" + toString());

        if (!fillGrid(0, 0)) {
            System.err.println("Error: Failed to fill grid with a valid solution.");
            return;
        }
        System.out.println("Grid after filling solution:\n" + toString());

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                solution[i][j] = cells[i][j];
            }
        }

        int cellsToKeep = rand.nextInt(9) + 70; // 20-29 clues
        System.out.println("Target cells to keep: " + cellsToKeep);

        ArrayList<int[]> positions = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                positions.add(new int[]{i, j});
            }
        }
        Collections.shuffle(positions, rand);

        for (int i = 0; i < cellsToKeep; i++) {
            int[] pos = positions.get(i);
            preFilled[pos[0]][pos[1]] = true;
        }

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (!preFilled[i][j]) {
                    cells[i][j] = " ";
                }
            }
        }

        System.out.println("Grid after setting pre-filled cells:\n" + toString());
        int finalPreFilled = countPreFilledCells();
        System.out.println("Final pre-filled cells: " + finalPreFilled);
        if (finalPreFilled != cellsToKeep) {
            System.err.println("Error: Final pre-filled count (" + finalPreFilled + ") does not match target (" + cellsToKeep + ")");
        }
    }

    private boolean fillGrid(int row, int col) {
        if (row == SIZE) {
            System.out.println("fillGrid completed successfully");
            return true;
        }

        int nextRow = col == SIZE - 1 ? row + 1 : row;
        int nextCol = col == SIZE - 1 ? 0 : col + 1;

        if (!cells[row][col].equals(" ")) return fillGrid(nextRow, nextCol);

        int[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        Random rand = new Random();
        for (int i = 0; i < numbers.length; i++) {
            int j = rand.nextInt(numbers.length);
            int temp = numbers[i];
            numbers[i] = numbers[j];
            numbers[j] = temp;
        }

        for (int num : numbers) {
            String value = String.valueOf(num);
            if (isValidMove(row, col, value, cells, false)) {
                cells[row][col] = value;
                if (fillGrid(nextRow, nextCol)) return true;
                cells[row][col] = " ";
            }
        }
        System.out.println("fillGrid backtracking at row=" + row + ", col=" + col);
        return false;
    }

    private boolean isValidMove(int row, int col, String value, String[][] grid, boolean checkPreFilled) {
        try {
            int val = Integer.parseInt(value);
            if (val < 1 || val > 9) {
                System.out.println("Invalid move: value " + value + " not in range 1-9 at [" + row + "," + col + "]");
                return false;
            }
            if (checkPreFilled && preFilled[row][col]) {
                System.out.println("Invalid move: cell [" + row + "," + col + "] is pre-filled");
                return false;
            }
            for (int j = 0; j < SIZE; j++) {
                if (j != col && grid[row][j].equalsIgnoreCase(value)) {
                    System.out.println("Invalid move: conflict in row " + row + " at col " + j + " with value " + grid[row][j]);
                    return false;
                }
            }
            for (int i = 0; i < SIZE; i++) {
                if (i != row && grid[i][col].equalsIgnoreCase(value)) {
                    System.out.println("Invalid move: conflict in col " + col + " at row " + i + " with value " + grid[i][col]);
                    return false;
                }
            }
            int subgridRowStart = (row / 3) * 3;
            int subgridColStart = (col / 3) * 3;
            for (int i = subgridRowStart; i < subgridRowStart + 3; i++) {
                for (int j = subgridColStart; j < subgridColStart + 3; j++) {
                    if ((i != row || j != col) && grid[i][j].equalsIgnoreCase(value)) {
                        System.out.println("Invalid move: conflict in subgrid at [" + i + "," + j + "] with value " + grid[i][j]);
                        return false;
                    }
                }
            }
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Invalid move: value " + value + " is not a number at [" + row + "," + col + "]");
            return false;
        }
    }

    public boolean isValidMove(int row, int col, String value) {
        if (preFilled[row][col]) {
            System.out.println("Move rejected: cell [" + row + "," + col + "] is pre-filled");
            return false;
        }
        return isValidMove(row, col, value, cells, true);
    }

    public void setCell(int row, int col, String value) {
        cells[row][col] = value.toLowerCase(); // Store user-entered cells as lowercase
        preFilled[row][col] = false;
    }

    public boolean isComplete() {
        System.out.println("Checking if grid is complete...");
        // Check if all cells are filled
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (cells[i][j].equals(" ") || cells[i][j].isEmpty()) {
                    System.out.println("Grid not complete: empty cell at [" + i + "," + j + "]");
                    return false;
                }
            }
        }
        // Verify the grid is valid
        /* for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                String value = cells[i][j];
                String temp = cells[i][j];
                cells[i][j] = " ";
                boolean valid = isValidMove(i, j, value, cells, true);
                cells[i][j] = temp;
                if (!valid) {
                    System.out.println("Grid invalid at [" + i + "," + j + "] with value " + value);
                    return false;
                }
            }
        } */
        System.out.println("Grid is complete and valid");
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
                String cell;
                if (cells[i][j].equals(" ") || cells[i][j].isEmpty()) {
                    cell = ".";
                } else if (preFilled[i][j]) {
                    cell = cells[i][j].toUpperCase(); // Pre-filled cells in uppercase
                } else {
                    cell = cells[i][j]; // User-entered cells in lowercase
                }
                sb.append(String.format(" %s |", cell));
            }
            sb.append("\n  +");
            for (int j = 0; j < SIZE; j++) sb.append("---+");
            sb.append("\n");
        }
        return sb.toString();
    }

    private String solutionToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        for (int j = 0; j < SIZE; j++) sb.append(String.format("%2d ", j));
        sb.append("\n  +");
        for (int j = 0; j < SIZE; j++) sb.append("---+");
        sb.append("\n");

        for (int i = 0; i < SIZE; i++) {
            sb.append(String.format("%2d|", i));
            for (int j = 0; j < SIZE; j++) {
                String cell = solution[i][j].equals(" ") ? "." : solution[i][j];
                sb.append(String.format(" %s |", cell));
            }
            sb.append("\n  +");
            for (int j = 0; j < SIZE; j++) sb.append("---+");
            sb.append("\n");
        }
        return sb.toString();
    }
}