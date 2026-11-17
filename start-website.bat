@echo off
rem ===========================================================================
rem  FrankMpesa customer website: starts the portal server (port 3000) and
rem  opens http://localhost:3000/customers.html in your browser.
rem  Double-click this file. To stop the website, close its window.
rem  "start-website.bat --check" only runs the checks and starts nothing.
rem ===========================================================================
title FrankMpesa website launcher
setlocal
set "ROOT=%~dp0"
set "BACKEND=%ROOT%customer-portal\backend"

echo.
echo  FrankMpesa website launcher
echo  ---------------------------

where node >nul 2>nul
if errorlevel 1 (
    echo  Node.js is not installed. Install it from nodejs.org, then run this again.
    pause
    exit /b 1
)
echo  [ok] Node.js found

sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    if "%~1"=="--check" (
        echo  [!!] MySQL is not running
        exit /b 1
    )
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

netstat -ano | findstr /R /C:":3000 .*LISTENING" >nul
if not errorlevel 1 (
    echo.
    echo  Port 3000 is already in use by another program, maybe your other project
    echo  or the FrankMpesa website that is already running. Stop it, then try again.
    pause
    exit /b 1
)
echo  [ok] Port 3000 is free

if not exist "%BACKEND%\node_modules\express" (
    if "%~1"=="--check" (
        echo  [..] Website packages not installed yet - they are installed on first start
        exit /b 0
    )
    echo  First start: installing the website packages, this needs internet...
    pushd "%BACKEND%"
    call npm install
    popd
    if not exist "%BACKEND%\node_modules\express" (
        echo  Installing the packages failed. Check your internet connection and try again.
        pause
        exit /b 1
    )
)
echo  [ok] Website packages installed
if "%~1"=="--check" exit /b 0

start "FrankMpesa website (port 3000)" cmd /k "cd /d "%BACKEND%" && node server.js"
echo  [..] Website starting in its own window
timeout /t 4 /nobreak >nul
start "" "http://localhost:3000/customers.html"

echo.
echo  Customer website: http://localhost:3000/customers.html
echo  Help Center:      %ROOT%website\owner-help\helpcenter.html
echo.
pause
