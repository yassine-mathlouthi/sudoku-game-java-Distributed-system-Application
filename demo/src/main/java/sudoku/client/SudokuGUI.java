package sudoku.client;

import common.CallbackInterface;
import common.GameInterface;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SudokuGUI {
    private GameInterface server;
    private CallbackInterface callback; // This is the client itself
    private TextField[][] gridFields;
    private Label messageLabel;
    private Button submitButton;
    private Button replayButton;
    private Button quitButton;
    private Stage primaryStage;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private Set<String> userEnteredCells; // Tracks user-entered cells as "row,col"

    public SudokuGUI() {
        System.out.println("SudokuGUI instance created: " + this);
        userEnteredCells = new HashSet<>();
    }

    public void setDependencies(GameInterface server, CallbackInterface callback) {
        System.out.println("Setting dependencies: server=" + server + ", callback=" + callback);
        this.server = server;
        this.callback = callback;
    }

    public void start(Stage stage) {
        System.out.println("Starting SudokuGUI: " + this + ", server=" + server + ", callback=" + callback);
        if (server == null || callback == null) {
            System.err.println("Server or callback is null in start method");
            throw new IllegalStateException("Server and callback must be set before starting the application");
        }

        this.primaryStage = stage;
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        GridPane gridPane = createSudokuGrid();
        root.setCenter(gridPane);

        messageLabel = new Label("Welcome to Sudoku!");
        messageLabel.setFont(new Font("Arial", 18));
        messageLabel.setTextFill(Color.BLACK);
        messageLabel.setStyle("-fx-font-weight: bold;");
        root.setBottom(messageLabel);

        HBox controlBox = new HBox(15);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(10, 0, 10, 0));

        submitButton = new Button("Submit Move");
        replayButton = new Button("Replay");
        quitButton = new Button("Quit");

        styleButton(submitButton);
        styleButton(replayButton);
        styleButton(quitButton);

        controlBox.getChildren().addAll(submitButton, replayButton, quitButton);
        root.setTop(controlBox);

        submitButton.setOnAction(e -> submitMove());
        replayButton.setOnAction(e -> {
            try {
                server.resetGame(callback);
                userEnteredCells.clear(); // Clear user-entered cells on reset
                messageLabel.setText("Game reset! New puzzle started.");
            } catch (RemoteException ex) {
                messageLabel.setText("Error resetting game: " + ex.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });
        quitButton.setOnAction(e -> Platform.exit());

        Scene scene = new Scene(root, 650, 650);
        primaryStage.setTitle("Sudoku Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void styleButton(Button button) {
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5; -fx-padding: 10 20;");
        button.setEffect(new DropShadow(10, Color.GRAY));
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5; -fx-padding: 10 20;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5; -fx-padding: 10 20;"));
    }

    private GridPane createSudokuGrid() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(10));

        gridFields = new TextField[9][9];

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                TextField field = new TextField();
                field.setPrefSize(50, 50);
                field.setAlignment(Pos.CENTER);
                field.setFont(new Font(18));

                int finalRow = row;
                int finalCol = col;
                field.setOnMouseClicked(e -> {
                    selectedRow = finalRow;
                    selectedCol = finalCol;
                    System.out.println("Selected cell [" + finalRow + "," + finalCol + "], editable=" + field.isEditable());
                });

                field.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal.matches("[1-9]?") && !newVal.isEmpty()) {
                        field.setText(oldVal);
                    }
                });

                if ((row / 3 + col / 3) % 2 == 0) {
                    field.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #B0B0B0;");
                }

                gridFields[row][col] = field;
                gridPane.add(field, col, row);
            }
        }

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                TextField field = gridFields[row][col];
                StringBuilder style = new StringBuilder();
                if (row % 3 == 0) style.append("-fx-border-width: 3 0 0 0; ");
                if (col % 3 == 0) style.append("-fx-border-width: 0 0 0 3; ");
                if (row % 3 == 2) style.append("-fx-border-width: 0 0 3 0; ");
                if (col % 3 == 2) style.append("-fx-border-width: 0 3 0 0; ");
                style.append("-fx-border-color: black;");
                field.setStyle(field.getStyle() + style);
            }
        }

        return gridPane;
    }

    private void submitMove() {
        try {
            if (selectedRow != -1 && selectedCol != -1) {
                TextField selectedField = gridFields[selectedRow][selectedCol];
                System.out.println("Submitting move for cell [" + selectedRow + "," + selectedCol + "], editable=" + selectedField.isEditable() + ", value=" + selectedField.getText());
                if (!selectedField.isEditable()) {
                    messageLabel.setText("Cannot modify pre-filled cell.");
                    messageLabel.setTextFill(Color.RED);
                    return;
                }
                String value = selectedField.getText();
                if (!value.isEmpty()) {
                    System.out.println("Submitting move: row=" + selectedRow + ", col=" + selectedCol + ", value=" + value);
                    String response = server.makeMove(callback, selectedRow, selectedCol, value);
                    System.out.println("Server response: " + response);
                    Platform.runLater(() -> {
                        messageLabel.setText(response);
                        messageLabel.setTextFill(response.startsWith("ERROR") ? Color.RED : Color.GREEN);
                        if (response.startsWith("SUCCESS")) {
                            // Mark as user-entered cell
                            userEnteredCells.add(selectedRow + "," + selectedCol);
                            // Update cell as user-entered: editable, light green
                            selectedField.setText(value.toLowerCase());
                            selectedField.setEditable(true);
                            selectedField.setStyle("-fx-background-color: #e0ffe0;" + getBaseStyle(selectedRow, selectedCol));
                            System.out.println("Cell [" + selectedRow + "," + selectedCol + "] updated: value=" + value.toLowerCase() + ", editable=" + selectedField.isEditable() + ", userEnteredCells=" + userEnteredCells);
                            try {
                                if (server.isGameOver(callback)) {
                                    messageLabel.setText("Congratulations! Puzzle completed.");
                                    showGameOverDialog();
                                }
                            } catch (RemoteException e) {
                                messageLabel.setText("Error checking game status: " + e.getMessage());
                                messageLabel.setTextFill(Color.RED);
                            }
                        }
                    });
                    return;
                }
                messageLabel.setText("Please enter a value in the selected cell.");
                messageLabel.setTextFill(Color.RED);
            } else {
                messageLabel.setText("Please select a cell.");
                messageLabel.setTextFill(Color.RED);
            }
        } catch (RemoteException e) {
            messageLabel.setText("Error submitting move: " + e.getMessage());
            messageLabel.setTextFill(Color.RED);
        }
    }

    private String getBaseStyle(int row, int col) {
        StringBuilder style = new StringBuilder();
        if ((row / 3 + col / 3) % 2 == 0) {
            style.append("-fx-background-color: #f0f0f0; -fx-border-color: #B0B0B0;");
        }
        if (row % 3 == 0) style.append("-fx-border-width: 3 0 0 0; ");
        if (col % 3 == 0) style.append("-fx-border-width: 0 0 0 3; ");
        if (row % 3 == 2) style.append("-fx-border-width: 0 0 3 0; ");
        if (col % 3 == 2) style.append("-fx-border-width: 0 3 0 0; ");
        style.append("-fx-border-color: black;");
        return style.toString();
    }

    private void showGameOverDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Congratulations! You completed the puzzle.");
        alert.setContentText("Would you like to play again?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);
        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                try {
                    server.resetGame(callback);
                    userEnteredCells.clear(); // Clear user-entered cells on reset
                } catch (RemoteException e) {
                    messageLabel.setText("Error resetting game: " + e.getMessage());
                    messageLabel.setTextFill(Color.RED);
                }
            } else {
                Platform.exit();
            }
        });
    }

    public void updateGrid(String gridString) {
        System.out.println("Updating GUI with grid string (length: " + (gridString != null ? gridString.length() : "null") + "):\n" + (gridString != null ? gridString : "null"));
        if (gridString == null || gridString.trim().isEmpty()) {
            System.err.println("Error: Grid string is null or empty");
            Platform.runLater(() -> {
                messageLabel.setText("Error: Received empty grid from server.");
                messageLabel.setTextFill(Color.RED);
            });
            return;
        }

        Platform.runLater(() -> {
            try {
                String[] lines = gridString.split("\n");
                System.out.println("Grid string split into " + lines.length + " lines");

                List<String> dataLines = new ArrayList<>();
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("+") && trimmed.contains("|")) {
                        dataLines.add(trimmed);
                    }
                }

                if (dataLines.size() != 9) {
                    throw new IllegalArgumentException("Expected 9 data rows, found: " + dataLines.size());
                }

                int preFilledCount = 0;
                int userEnteredCount = 0;

                for (int row = 0; row < 9; row++) {
                    String line = dataLines.get(row);
                    System.out.println("Parsing row " + row + ": " + line);
                    String[] cells = line.substring(line.indexOf('|') + 1).split("\\|");

                    if (cells.length < 9) {
                        throw new IllegalArgumentException("Row " + row + " has too few cells: " + line);
                    }

                    for (int col = 0; col < 9; col++) {
                        String cell = cells[col].trim();
                        System.out.println("Cell [" + row + "," + col + "] = " + cell);
                        TextField field = gridFields[row][col];
                        String cellKey = row + "," + col;

                        if (cell.equals(".") || cell.isEmpty()) {
                            field.setText("");
                            field.setEditable(true);
                            field.setStyle("-fx-background-color: white;" + getBaseStyle(row, col));
                            userEnteredCells.remove(cellKey); // Not user-entered anymore
                            System.out.println("Cell [" + row + "," + col + "] set: empty, editable=true, userEnteredCells=" + userEnteredCells);
                        } else if (cell.matches("[1-9]")) {
                            field.setText(cell.toLowerCase()); // Normalize to lowercase for display
                            if (userEnteredCells.contains(cellKey)) {
                                // User-entered cell
                                field.setEditable(true);
                                field.setStyle("-fx-background-color: #e0ffe0;" + getBaseStyle(row, col));
                                userEnteredCount++;
                                System.out.println("Cell [" + row + "," + col + "] set: user-entered, value=" + cell.toLowerCase() + ", editable=true, userEnteredCells=" + userEnteredCells);
                            } else {
                                // Pre-filled cell
                                field.setEditable(false);
                                field.setStyle("-fx-background-color: #d3d3d3;" + getBaseStyle(row, col));
                                preFilledCount++;
                                System.out.println("Cell [" + row + "," + col + "] set: pre-filled, value=" + cell.toLowerCase() + ", editable=false, userEnteredCells=" + userEnteredCells);
                            }
                        } else {
                            System.err.println("Invalid cell value at [" + row + "," + col + "]: " + cell);
                            field.setText("");
                            field.setEditable(true);
                            field.setStyle("-fx-background-color: white;" + getBaseStyle(row, col));
                            userEnteredCells.remove(cellKey);
                            System.out.println("Cell [" + row + "," + col + "] set: invalid, cleared, editable=true, userEnteredCells=" + userEnteredCells);
                        }
                    }
                }

                System.out.println("Grid rendering completed. Parsed pre-filled cells: " + preFilledCount + ", user-entered cells: " + userEnteredCount);
                if (preFilledCount == 0) {
                    System.err.println("Error: No pre-filled cells parsed. Initial grid may be empty.");
                    messageLabel.setText("Error: No pre-filled cells in initial grid.");
                    messageLabel.setTextFill(Color.RED);
                } else if (preFilledCount < 20 || preFilledCount > 29) {
                    System.err.println("Warning: Unexpected number of pre-filled cells: " + preFilledCount);
                }
            } catch (Exception e) {
                System.err.println("Error rendering grid: " + e.getMessage());
                e.printStackTrace();
                messageLabel.setText("Error rendering grid: " + e.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });
    }

    public void showMessage(String message) {
        System.out.println("Showing message in GUI: " + message);
        Platform.runLater(() -> {
            messageLabel.setText(message);
            messageLabel.setTextFill(message.startsWith("ERROR") ? Color.RED : Color.BLACK);
        });
    }
}