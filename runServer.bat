@echo off
title Run Sudoku Game Server

echo Compiling Java files...
javac ./server/server/*.java client/client/*.java common/common/*.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed! Check the errors above.
    pause
    exit /b 1
)

echo Starting the Game Server...
echo "Game Server"
cd server & java -cp ../../ server.GameServer
if %ERRORLEVEL% NEQ 0 (
    echo Failed to start the Game Server! Check the errors above.
    pause
    exit /b 1
)

echo Game Server is running in a separate window.
pause