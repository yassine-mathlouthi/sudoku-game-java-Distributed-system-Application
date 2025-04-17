@echo off
title Run Sudoku Game Client

echo Starting the Game Client...
echo "Game Client" 
cd client & java -cp ../../ client.GameClient
if %ERRORLEVEL% NEQ 0 (
    echo Failed to start the Game Client! Check the errors above.
    pause
    exit /b 1
)

echo Game Client is running in a separate window.
pause