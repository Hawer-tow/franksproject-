@echo off
rem ===========================================================================
rem  FrankMpesa: start the server the phone app uses, plus the ngrok tunnel.
rem  Double-click this file. Two windows open: "server" and "tunnel".
rem  To stop: close those two windows (or press Ctrl+C in each).
rem ===========================================================================
title FrankMpesa launcher
setlocal
set "ROOT=%~dp0"

echo.
echo  FrankMpesa launcher
echo  ------------------

rem 1. MySQL must be running (it is set to start manually)
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo  MySQL is not running. Starting it - Windows will ask for permission...
    powershell -NoProfile -Command "Start-Process powershell -Verb RunAs -Wait -ArgumentList '-NoProfile','-Command','Start-Service MySQL80'"
    sc query MySQL80 | find "RUNNING" >nul
    if errorlevel 1 (
        echo  MySQL did not start. Start the MySQL80 service and run this file again.
        pause
        exit /b 1
    )
)
echo  [ok] MySQL is running

rem 2. Port 8080 must be free (your other project may be using it)
netstat -ano | findstr /R /C:":8080 .*LISTENING" >nul
if not errorlevel 1 (
    echo.
    echo  Port 8080 is already in use by another program, maybe your other project
    echo  or a FrankMpesa server that is already running. Stop it, then try again.
    pause
    exit /b 1
)
echo  [ok] Port 8080 is free

rem 3. The server (Spring Boot) in its own window
start "FrankMpesa server (port 8080)" cmd /k "cd /d "%ROOT%server" && set "JAVA_HOME=C:\Program Files\Java\jdk-17" && gradlew.bat bootRun"
echo  [..] Server starting in its own window

rem 4. The ngrok tunnel the phone connects through (only one may run at a time)
tasklist /FI "IMAGENAME eq ngrok.exe" | find /I "ngrok.exe" >nul
if errorlevel 1 (
    start "FrankMpesa tunnel (ngrok)" cmd /k "ngrok http 8080"
    echo  [..] Tunnel starting in its own window
) else (
    echo  [ok] Tunnel already running - keeping it
)

echo.
echo  Wait until the server window shows "Started MobileDbApplication"
echo  (about a minute), then pull down the profile list in the app.
echo  If it says "Port 8080 was already in use", another program took the port
echo  while it was starting: close the extra windows and run this once more.
echo.
pause
