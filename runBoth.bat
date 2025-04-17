@echo off
title Run Sudoku Game - Server and Client

echo Compiling Java files...
javac server/*.java client/*.java common/*.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed! Check the errors above.
    pause
    exit /b 1
)

echo Starting the Game Server...
start "Game Server" cmd /k "cd server & java -cp ../ server.GameServer"
if %ERRORLEVEL% NEQ 0 (
    echo Failed to start the Game Server! Check the errors above.
    pause
    exit /b 1
)

echo Waiting for the server to start (5 seconds)...
timeout /t 5 /nobreak >nul

echo Starting the Game Client...
start "Game Client" cmd /k "cd client & java -cp ../ client.GameClient"
if %ERRORLEVEL% NEQ 0 (
    echo Failed to start the Game Client! Check the errors above.
    pause
    exit /b 1
)

echo Server and Client are running in separate windows.
pause