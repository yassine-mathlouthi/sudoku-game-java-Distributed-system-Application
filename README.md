# Sudoku Game

A client-server Sudoku game implemented in Java using RMI (Remote Method Invocation) and JavaFX for the GUI. Players can connect to a server, play Sudoku puzzles, and interact with a graphical interface.

## Features

- Multi-client support with a maximum of 10 concurrent players.
- Real-time grid updates and move validation.
- User-friendly JavaFX GUI with distinct styling for pre-filled and user-entered cells.
- Proper disconnection handling for both "Quit" button and window close events.
- Server-side grid generation with 20–29 pre-filled cells for balanced difficulty.

## Requirements

- **Java Version**: JDK 17 or later
- **JavaFX**: JavaFX SDK 17 or later
- **Build Tool**: Maven or Gradle (optional, for dependency management)
- **Operating System**: Windows, macOS, or Linux

## Dependencies

- **JavaFX**: For the graphical user interface.
  - Include JavaFX modules: `javafx.controls`, `javafx.fxml`, `javafx.graphics`.
- **Java RMI**: Built into JDK for remote communication.
- **No external libraries required** beyond JavaFX.

## Setup Instructions

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/yourusername/sudoku-game.git
   cd sudoku-game
2. **Configure JavaFX**:

   ```bash
   --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
![image](https://github.com/user-attachments/assets/05b4e77b-8211-43b5-9834-b0b3200af8ce)
![image](https://github.com/user-attachments/assets/5c038371-0bb5-4ee5-a23e-4e7d6b54b271)
