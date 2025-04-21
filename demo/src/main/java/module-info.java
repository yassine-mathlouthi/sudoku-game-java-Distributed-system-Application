module sudoku {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.rmi;
    opens sudoku to javafx.fxml;
    
    exports sudoku.server;
    exports sudoku.client;
    exports common;
}
