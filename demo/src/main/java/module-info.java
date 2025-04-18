module sudoku {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    opens sudoku to javafx.fxml;
    exports sudoku;
    exports sudoku.server;
    exports sudoku.client;
    exports common;
}
